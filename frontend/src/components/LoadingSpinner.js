import React from 'react';
import { Card } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSpinner, faNewspaper } from '@fortawesome/free-solid-svg-icons';

const LoadingSpinner = ({ message = 'Searching for news articles...' }) => {
  return (
    <Card className="custom-card">
      <Card.Body>
        <div className="loading-container">
          <div className="mb-4">
            <FontAwesomeIcon 
              icon={faSpinner} 
              spin 
              size="3x" 
              className="text-primary"
            />
          </div>
          <h5 className="text-primary mb-2">
            <FontAwesomeIcon icon={faNewspaper} className="me-2" />
            Loading News
          </h5>
          <p className="text-muted mb-0">{message}</p>
          
          {/* Progress indicators */}
          <div className="mt-4">
            <div className="d-flex justify-content-center">
              <div className="spinner-grow spinner-grow-sm text-primary me-2" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
              <div className="spinner-grow spinner-grow-sm text-primary me-2" role="status" style={{ animationDelay: '0.2s' }}>
                <span className="visually-hidden">Loading...</span>
              </div>
              <div className="spinner-grow spinner-grow-sm text-primary" role="status" style={{ animationDelay: '0.4s' }}>
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          </div>
        </div>
      </Card.Body>
    </Card>
  );
};

export default LoadingSpinner;
