import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Simple WebSocket service to manage STOMP connections for the poker app
 */
class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = {};
    this.connectionCallbacks = {
      onConnect: [],
      onDisconnect: []
    };
  }

  /**
   * Initialize the STOMP client
   * @param {string} serverUrl - WebSocket server URL (ws://localhost:8080/ws)
   */
  init(serverUrl) {
    this.serverUrl = serverUrl;
  }

  /**
   * Connect to the WebSocket server
   * @returns {Promise} Promise that resolves when connected
   */
  connect() {
    return new Promise((resolve, reject) => {
      // Create a new STOMP client
      this.client = new Client({
        webSocketFactory: () => new SockJS(this.serverUrl),
        connectHeaders: {
          // Add any headers needed for authentication
        },
        debug: function(str) {
          console.log('STOMP: ' + str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      });

      // Set up connect listener
      this.client.onConnect = (frame) => {
        this.connected = true;
        console.log('Connected to WebSocket');
        resolve(frame);
        
        // Call all registered connect callbacks
        this.connectionCallbacks.onConnect.forEach(callback => {
          try {
            callback(frame);
          } catch (error) {
            console.error('Error in connect callback:', error);
          }
        });
      };

      // Set up disconnect listener
      this.client.onStompError = (frame) => {
        console.error('STOMP error:', frame);
        reject(frame);
      };

      // Connect
      this.client.activate();
    });
  }

  /**
   * Disconnect from the WebSocket server
   */
  disconnect() {
    if (this.client && this.connected) {
      // Unsubscribe from all topics
      Object.values(this.subscriptions).forEach(subscription => {
        if (subscription) {
          subscription.unsubscribe();
        }
      });
      this.subscriptions = {};

      // Deactivate client
      this.client.deactivate();
      this.connected = false;
      
      // Call all disconnect callbacks
      this.connectionCallbacks.onDisconnect.forEach(callback => {
        try {
          callback();
        } catch (error) {
          console.error('Error in disconnect callback:', error);
        }
      });
    }
  }

  /**
   * Subscribe to a table
   * @param {number} tableId - Table ID
   * @param {function} callback - Message handler function
   * @returns {string} Subscription ID for later unsubscribing
   */
  subscribeToTable(tableId, callback) {
    if (!this.connected || !this.client) {
      console.error('Not connected to WebSocket server');
      return null;
    }

    const destination = `/topic/tables/${tableId}`;
    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (error) {
        console.error('Error parsing message:', error);
        callback(message.body);
      }
    });

    const subId = `table-${tableId}`;
    this.subscriptions[subId] = subscription;
    return subId;
  }

  /**
   * Send a message to a table
   * @param {number} tableId - Table ID
   * @param {Object} message - Message to send
   */
  sendToTable(tableId, message) {
    if (!this.connected || !this.client) {
      console.error('Not connected to WebSocket server');
      return;
    }

    const destination = `/app/tables/${tableId}`;
    this.client.publish({
      destination: destination,
      body: JSON.stringify(message)
    });
  }

  /**
   * Send a request and wait for a response
   * @param {number} tableId - Table ID
   * @param {Object} message - Message to send
   * @param {number} timeoutSeconds - Timeout in seconds
   * @returns {Promise} Promise that resolves with the response
   */
  async sendRequestWithResponse(tableId, message, timeoutSeconds = 10) {
    return new Promise((resolve, reject) => {
      if (!this.connected || !this.client) {
        reject(new Error('Not connected to WebSocket server'));
        return;
      }

      // Generate a unique request ID
      const requestId = `req-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      message.requestId = requestId;
      
      // Set up a temporary handler for the response
      const responseHandler = (response) => {
        if (response.responseId === requestId) {
          resolve(response);
          return true; // Signal that we've handled this message
        }
        return false; // Not our response, let normal handler process it
      };
      
      // Store original callback
      const subId = `table-${tableId}`;
      const originalCallback = this.subscriptions[subId]?._callback;
      
      // Replace with our handler that checks for the response
      if (originalCallback) {
        this.subscriptions[subId]._callback = (message) => {
          try {
            const body = JSON.parse(message.body);
            if (!responseHandler(body) && originalCallback) {
              originalCallback(message);
            }
          } catch (error) {
            console.error('Error in response handler:', error);
            if (originalCallback) {
              originalCallback(message);
            }
          }
        };
      }
      
      // Set timeout
      const timeoutId = setTimeout(() => {
        // Restore original callback
        if (this.subscriptions[subId]) {
          this.subscriptions[subId]._callback = originalCallback;
        }
        reject(new Error(`Request timed out after ${timeoutSeconds} seconds`));
      }, timeoutSeconds * 1000);
      
      // Send the request
      this.sendToTable(tableId, message);
    });
  }

  /**
   * Unsubscribe from a subscription
   * @param {string} subscriptionId - ID returned from subscribe
   */
  unsubscribe(subscriptionId) {
    const subscription = this.subscriptions[subscriptionId];
    if (subscription) {
      subscription.unsubscribe();
      delete this.subscriptions[subscriptionId];
    }
  }

  /**
   * Check if connected to WebSocket server
   * @returns {boolean} Connection status
   */
  isConnected() {
    return this.connected;
  }
  
  /**
   * Register a callback for connect/disconnect events
   * @param {string} event - 'connect' or 'disconnect'
   * @param {function} callback - Callback to execute 
   */
  on(event, callback) {
    if (event === 'connect') {
      this.connectionCallbacks.onConnect.push(callback);
      
      // If already connected, call immediately
      if (this.connected) {
        callback();
      }
    } else if (event === 'disconnect') {
      this.connectionCallbacks.onDisconnect.push(callback);
    }
  }
}

// Singleton instance
const websocketService = new WebSocketService();
export default websocketService;