import React, { useState } from "react";

/**
 * Component for poker action controls (fold, check, call, raise)
 *
 * @param {Object} props Component props
 * @param {number} props.chips Current player chips
 * @param {boolean} props.isLoading Loading state for actions
 * @param {string} props.actionStatus Current action status message
 * @param {function} props.onAction Function to handle player actions
 * @returns {JSX.Element} ActionPanel component
 */
const ActionPanel = ({ chips, isLoading, actionStatus, onAction }) => {
  const [showRaiseInput, setShowRaiseInput] = useState(false);
  const [raiseAmount, setRaiseAmount] = useState("");

  // Button handlers for poker actions
  const handleRaiseClick = () => {
    setShowRaiseInput((prev) => !prev);
  };

  const handleConfirmRaise = () => {
    if (raiseAmount) {
      onAction("raise", raiseAmount);
      setShowRaiseInput(false);
      setRaiseAmount("");
    }
  };

  return (
    <div className="action-panel">
      <h2>Player Actions</h2>
      <div className="player-info">
        <div className="chips-display">
          <span className="chips-label">Table Chips:</span>
          <span className="chips-value">{chips}</span>
        </div>
      </div>

      <div className="action-buttons">
        <button
          className="action-button raise"
          onClick={handleRaiseClick}
          disabled={isLoading}
        >
          <span className="button-icon">♦</span>
          <span>Raise</span>
        </button>
        <button
          className="action-button call"
          onClick={() => {
            setShowRaiseInput(false);
            onAction("call");
          }}
          disabled={isLoading}
        >
          <span className="button-icon">♣</span>
          <span>Call</span>
        </button>
        <button
          className="action-button check"
          onClick={() => {
            setShowRaiseInput(false);
            onAction("check");
          }}
          disabled={isLoading}
        >
          <span className="button-icon">♥</span>
          <span>Check</span>
        </button>
        <button
          className="action-button fold"
          onClick={() => {
            setShowRaiseInput(false);
            onAction("fold");
          }}
          disabled={isLoading}
        >
          <span className="button-icon">♠</span>
          <span>Fold</span>
        </button>
      </div>

      {showRaiseInput && (
        <div className="raise-input-container">
          <input
            type="number"
            placeholder="Enter raise amount"
            value={raiseAmount}
            onChange={(e) => setRaiseAmount(e.target.value)}
          />
          <button onClick={handleConfirmRaise} disabled={isLoading}>
            Confirm
          </button>
        </div>
      )}

      {actionStatus && (
        <div className="action-status">
          {isLoading && <div className="loading-spinner"></div>}
          <p>{actionStatus}</p>
        </div>
      )}
    </div>
  );
};

export default ActionPanel;