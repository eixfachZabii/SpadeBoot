/*const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  // Create a proxy specifically for Socket.IO
  const socketProxy = createProxyMiddleware({
    target: 'wss://localhost:5001',
    changeOrigin: true,
    secure: false,  // Ignore SSL certificate issues
    ws: true,       // Enable WebSocket support
    logLevel: 'debug',
    pathRewrite: {
      '^/socket.io': '/socket.io'  // Keep path as is
    },
    onError: (err, req, res) => {
      console.error('Proxy error:', err);
    }
  });

  // Apply the proxy to the socket.io path first, before any other middleware
  app.use('/socket.io', socketProxy);
  
  // Add logging middleware for non-socket.io requests to avoid interference
  app.use(/^(?!\/socket\.io).*$/, (req, res, next) => {
    console.log(`[Proxy Debug] ${req.method} ${req.url}`);
    next();
  });
};
*/