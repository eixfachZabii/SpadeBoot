import React, { useState, useEffect } from "react";
import { FaTrash, FaSignOutAlt, FaShieldAlt } from "react-icons/fa";
import ApiService from "../services/ApiService";
import ConfirmationModal from "../components/common/ConfirmationModal";
import LobbySystem from "./LobbySystem";
import CardScanner from "../components/game/CardScanner";
import ActionPanel from "../components/game/ActionPanel";
import PokerTable from "../components/game/PokerTable";
// Import WebSocketService
import WebSocketService from "../services/WebSocketService";



/**
 * Home page component that displays either the lobby or the poker table UI
 *
 * @param {Object} props Component props
 * @param {Object} props.socket Socket.io connection
 * @param {boolean} props.socketConnected Socket connection status
 * @param {boolean} props.darkMode Dark mode state
 * @param {Object} props.user Current user data
 * @param {Function} props.onTableStatusChange Callback when table status changes
 * @param {Function} props.onBalanceUpdate Callback to update user balance in parent component
 * @returns {JSX.Element} HomePage component
 */
function HomePage({ socket, socketConnected, darkMode, user, onTableStatusChange, onBalanceUpdate }) {
  // State for camera and scanning
  const [isLoading, setIsLoading] = useState(false);
  const [actionStatus, setActionStatus] = useState("");

  // State for current table
  const [currentTable, setCurrentTable] = useState(null);
  const [atTable, setAtTable] = useState(false);
  const [checkingTableStatus, setCheckingTableStatus] = useState(false);
  const [currChips, setCurrChips] = useState(0);

  // Add these state variables in your HomePage component
  const [wsSubscriptionId, setWsSubscriptionId] = useState(null);
  const [tableMessages, setTableMessages] = useState([]);

  // Error state
  const [error, setError] = useState("");

  // Delete table confirmation state
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);

  // Check if the user is at a table when the component mounts or user changes
  useEffect(() => {
    if (user) {
      checkTableStatus();
      updateChips();
    }
  }, [user]);

  // Update chips
  const updateChips = async () => {
    if (!user) return;

    try {
      const chips = await ApiService.getCurrChips();
      setCurrChips(chips);
    } catch (error) {
      console.error("Error getting chips:", error);
    }
  };

  // Get the updated user balance from the server
  const fetchAndUpdateUserBalance = async () => {
    if (!user) return;

    try {
      const userData = await ApiService.getCurrentUser();
      if (userData && onBalanceUpdate) {
        onBalanceUpdate(userData.balance);
      }
    } catch (error) {
      console.error("Error fetching updated user balance:", error);
    }
  };


  // Add this function to handle WebSocket messages
  const handleTableMessage = (message) => {
    console.log("WebSocket message received:", message);
    
    // Add the message to our state
    setTableMessages(prevMessages => [...prevMessages, message]);
    
    // Handle different message types
    switch (message.type) {
      case "PLAYER_CONNECTED":
        setActionStatus(`Player ${message.playerName} connected`);
        setTimeout(() => setActionStatus(""), 3000);
        break;
      case "PLAYER_DISCONNECTED":
        setActionStatus(`Player ${message.playerName} disconnected`);
        setTimeout(() => setActionStatus(""), 3000);
        break;
      // Add more message types as needed
      default:
        // Other message types
        break;
    }
  };

  // Add this function to connect to WebSocket
  const connectToTableWebSocket = async (tableId) => {
    try {
      // Initialize WebSocket service
      WebSocketService.init("http://localhost:8080/ws");
      
      // Connect to WebSocket server
      await WebSocketService.connect();
      
      // Subscribe to table
      const subscriptionId = WebSocketService.subscribeToTable(tableId, handleTableMessage);
      setWsSubscriptionId(subscriptionId);
      
      // Announce presence
      WebSocketService.sendToTable(tableId, {
        type: "PLAYER_JOINED",
        playerName: user.username
      });
      
      console.log(`Connected to table ${tableId} WebSocket`);
    } catch (error) {
      console.error("WebSocket connection error:", error);
      setError("Failed to connect to table WebSocket: " + error.message);
    }
  };

  // Add this function to disconnect from WebSocket
  const disconnectFromTableWebSocket = () => {
    if (wsSubscriptionId) {
      if (currentTable) {
        // Announce leaving
        WebSocketService.sendToTable(currentTable.id, {
          type: "PLAYER_LEFT",
          playerName: user.username
        });
      }
      
      // Unsubscribe
      WebSocketService.unsubscribe(wsSubscriptionId);
      setWsSubscriptionId(null);
      setTableMessages([]);
      console.log("Disconnected from table WebSocket");
    }
  };

  // Function to check table status using the dedicated endpoint
  const checkTableStatus = async () => {
    if (!user) {
      setAtTable(false);
      setCurrentTable(null);
      setCheckingTableStatus(false);
      return;
    }

    setCheckingTableStatus(true);

    try {
      // Call the dedicated endpoint to get table status
      const tableStatus = await ApiService.getCurrentTable();

      if (tableStatus.isAtTable && tableStatus.tableId) {
        setAtTable(true);
        setCurrentTable(tableStatus.table || await ApiService.getTableById(tableStatus.tableId));
      } else {
        setAtTable(false);
        setCurrentTable(null);
      }

      // Notify parent component about table status change
      if (onTableStatusChange) {
        onTableStatusChange();
      }
    } catch (error) {
      console.error("Error checking table status:", error);
      setAtTable(false);
      setCurrentTable(null);
    } finally {
      setCheckingTableStatus(false);
    }
  };

  // Function to join a table
 // Update handleJoinTable function to connect to WebSocket
  const handleJoinTable = async (tableId, buyIn) => {
    if (!user) {
      setError("You must be logged in to join a table");
      setTimeout(() => setError(""), 3000);
      return;
    }

    setIsLoading(true);
    setError("");

    try {
      // Join table via API
      const joinResponse = await ApiService.joinTable(tableId, buyIn);
      console.log("Join table response:", joinResponse);

      // Connect to WebSocket
      await connectToTableWebSocket(joinResponse.tableId);

      // Update UI
      await checkTableStatus();
      await updateChips();
    } catch (error) {
      setError("Failed to join table: " + (error.message || "Unknown error"));
    } finally {
      setIsLoading(false);
    }
  };

  // Function to leave a table
  // Update handleLeaveTable function to disconnect from WebSocket
  const handleLeaveTable = async () => {
    if (!currentTable) return;

    setIsLoading(true);
    setError("");

    try {
      // Disconnect from WebSocket
      disconnectFromTableWebSocket();
      
      // Leave table via API
      await ApiService.leaveTable(currentTable.id);

      // Update UI
      await checkTableStatus();
      await updateChips();
    } catch (error) {
      setError("Failed to leave table: " + (error.message || "Unknown error"));
    } finally {
      setIsLoading(false);
    }
  };

  // Function to delete a table (owner only)
  const handleDeleteTable = async () => {
    if (!currentTable) return;

    setIsLoading(true);
    setError("");
    setShowDeleteConfirmation(false);

    try {
      await ApiService.deleteTable(currentTable.id);

      // Get the updated balance after table deletion
      await fetchAndUpdateUserBalance();

      // Show success message
      setActionStatus("Table successfully deleted");

      // Check table status to update UI (user will no longer be at a table)
      await checkTableStatus();

      // Clear success message after delay
      setTimeout(() => setActionStatus(""), 3000);
    } catch (error) {
      setError("Failed to delete table: " + (error.message || "Unknown error"));
      setTimeout(() => setError(""), 3000);
    } finally {
      setIsLoading(false);
    }
  };

  // Function to send poker actions
  const handleAction = async (action, amount = null) => {
    setIsLoading(true);
    setActionStatus(`Processing: ${action}...`);

    try {
      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 500));
      setActionStatus(`Action "${action}" completed.`);

      // In a real implementation, you'd update the user balance here
      // await fetchAndUpdateUserBalance();

      // Clear status after 2 seconds
      setTimeout(() => setActionStatus(""), 2000);
    } finally {
      setIsLoading(false);
    }
  };

  // Add cleanup effect for WebSocket
  useEffect(() => {
    return () => {
      disconnectFromTableWebSocket();
    };
  }, []);

  // Show loading state while checking table status
  if (checkingTableStatus) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading table status...</p>
      </div>
    );
  }

  // Conditionally render either the lobby system or the poker table
  return (
      <div className={`main-content ${!atTable ? "lobby-layout" : ""}`}>
        {error && <div className="error-message global-error">{error}</div>}

        {!atTable ? (
            // Lobby System when not at a table
            <LobbySystem
                user={user}
                onJoinTable={handleJoinTable}
                currentTable={currentTable}
                darkMode={darkMode}
                onBalanceUpdate={onBalanceUpdate}
            />
        ) : (
            // Poker Table UI when at a table - using grid layout
            <div className="poker-table-layout">
              {/* Header area with table name and controls */}
              <div className="table-header" style={{ gridArea: "header", width: "100%" }}>
                <h2>Table: {currentTable?.name}</h2>
                <div className="table-info">
                  {currentTable?.ownerId === user?.id && (
                      <>
                  <span className="owner-badge" title="You are the owner of this table">
                    <FaShieldAlt className="owner-icon" />
                  </span>
                        <button
                            className="delete-table-button"
                            onClick={() => setShowDeleteConfirmation(true)}
                            disabled={isLoading}
                            title="Delete Table"
                        >
                          <FaTrash />
                        </button>
                      </>
                  )}
                  <button
                      className="leave-table-button"
                      onClick={handleLeaveTable}
                      disabled={isLoading}
                      title="Leave Table"
                  >
                    <FaSignOutAlt />
                  </button>
                </div>
              </div>

              {/* Scanner area */}
              <div style={{ gridArea: "scanner", width: "100%" }}>
                <CardScanner
                    socketConnected={socketConnected}
                    socket={socket}
                    actionStatus={actionStatus}
                    isLoading={isLoading}
                />
              </div>

              {/* Action panel area */}
              <div style={{ gridArea: "actions", width: "100%" }}>
                <ActionPanel
                    chips={currChips}
                    isLoading={isLoading}
                    actionStatus={actionStatus}
                    onAction={handleAction}
                />
              </div>

              {/* Confirmation Modal for deleting table */}
              <ConfirmationModal
                show={showDeleteConfirmation}
                title="Delete Table"
                message="Are you sure you want to delete this table? All players will be removed."
                confirmText="Delete Table"
                cancelText="Cancel"
                confirmButtonClass="danger"
                onConfirm={handleDeleteTable}
                onCancel={() => setShowDeleteConfirmation(false)}
              />
            </div>
        )}
      </div>
  );
}

export default HomePage;