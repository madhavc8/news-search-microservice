import React from 'react';
import { Card, Row, Col, Alert, Button, Badge } from 'react-bootstrap';
import { useQuery } from 'react-query';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faHeartbeat, 
  faCheckCircle, 
  faExclamationTriangle,
  faServer,
  faToggleOn,
  faInfoCircle,
  faRefresh,
  faSpinner
} from '@fortawesome/free-solid-svg-icons';
import { toast } from 'react-hot-toast';
import LoadingSpinner from './LoadingSpinner';
import { newsApi } from '../services/api';

const HealthCheck = () => {
  const {
    data: healthData,
    isLoading,
    error,
    refetch,
    isFetching
  } = useQuery(
    'healthCheck',
    newsApi.getHealth,
    {
      refetchInterval: 30000, // Refresh every 30 seconds
      onSuccess: () => {
        toast.success('Health status updated');
      },
      onError: (error) => {
        toast.error(`Health check failed: ${error.message}`);
      }
    }
  );

  const handleRefresh = () => {
    refetch();
  };

  if (isLoading) {
    return <LoadingSpinner message="Checking service health..." />;
  }

  if (error) {
    return (
      <Alert variant="danger">
        <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
        <strong>Health Check Failed:</strong> {error.message}
        <Button 
          variant="outline-danger" 
          size="sm" 
          className="ms-3"
          onClick={handleRefresh}
        >
          <FontAwesomeIcon icon={faRefresh} className="me-1" />
          Retry
        </Button>
      </Alert>
    );
  }

  const {
    status = 'UNKNOWN',
    newsApiAvailable = false,
    offlineModeEnabled = false,
    timestamp
  } = healthData || {};

  const getStatusVariant = (status) => {
    switch (status?.toUpperCase()) {
      case 'UP':
      case 'HEALTHY':
        return 'success';
      case 'DOWN':
      case 'UNHEALTHY':
        return 'danger';
      default:
        return 'warning';
    }
  };

  const getStatusIcon = (status) => {
    switch (status?.toUpperCase()) {
      case 'UP':
      case 'HEALTHY':
        return faCheckCircle;
      case 'DOWN':
      case 'UNHEALTHY':
        return faExclamationTriangle;
      default:
        return faInfoCircle;
    }
  };

  return (
    <div className="health-check fade-in">
      {/* Header */}
      <div className="text-center mb-4">
        <h1 className="header-title display-4">
          <FontAwesomeIcon icon={faHeartbeat} className="me-3" />
          Service Health
        </h1>
        <p className="lead text-muted">
          Monitor the status of the News Search Microservice and its dependencies
        </p>
      </div>

      {/* Refresh Button */}
      <div className="text-end mb-3">
        <Button
          variant="outline-primary"
          onClick={handleRefresh}
          disabled={isFetching}
        >
          {isFetching ? (
            <FontAwesomeIcon icon={faSpinner} spin className="me-2" />
          ) : (
            <FontAwesomeIcon icon={faRefresh} className="me-2" />
          )}
          Refresh Status
        </Button>
      </div>

      {/* Overall Status */}
      <Card className="custom-card mb-4">
        <Card.Body>
          <Row className="align-items-center">
            <Col md={8}>
              <h4 className="mb-2">
                <FontAwesomeIcon 
                  icon={getStatusIcon(status)} 
                  className={`me-2 text-${getStatusVariant(status)}`} 
                />
                Overall Service Status
              </h4>
              <p className="text-muted mb-0">
                Last updated: {timestamp ? new Date(timestamp).toLocaleString() : 'Unknown'}
              </p>
            </Col>
            <Col md={4} className="text-end">
              <Badge 
                bg={getStatusVariant(status)} 
                className="fs-4 px-3 py-2"
              >
                {status}
              </Badge>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Component Status Cards */}
      <Row className="mb-4">
        {/* NewsAPI Status */}
        <Col md={6} className="mb-3">
          <Card className={`border-${newsApiAvailable ? 'success' : 'warning'} h-100`}>
            <Card.Header className={`bg-${newsApiAvailable ? 'success' : 'warning'} text-white`}>
              <FontAwesomeIcon 
                icon={newsApiAvailable ? faCheckCircle : faExclamationTriangle} 
                className="me-2" 
              />
              NewsAPI Status
            </Card.Header>
            <Card.Body>
              <h5 className={`text-${newsApiAvailable ? 'success' : 'warning'} mb-3`}>
                {newsApiAvailable ? 'Available' : 'Unavailable'}
              </h5>
              <p className="mb-0">
                External NewsAPI service is {newsApiAvailable ? 'responding normally' : 'currently unavailable'}.
                {!newsApiAvailable && ' The service will use offline mode with cached data.'}
              </p>
            </Card.Body>
          </Card>
        </Col>

        {/* Offline Mode Status */}
        <Col md={6} className="mb-3">
          <Card className="border-info h-100">
            <Card.Header className="bg-info text-white">
              <FontAwesomeIcon icon={faToggleOn} className="me-2" />
              Offline Mode
            </Card.Header>
            <Card.Body>
              <h5 className="text-info mb-3">
                {offlineModeEnabled ? 'Enabled' : 'Disabled'}
              </h5>
              <p className="mb-0">
                Offline mode provides cached results when external API is unavailable.
                This ensures service continuity even during external service outages.
              </p>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* System Information */}
      <Card className="custom-card">
        <Card.Header>
          <h5 className="mb-0">
            <FontAwesomeIcon icon={faServer} className="me-2" />
            System Information
          </h5>
        </Card.Header>
        <Card.Body>
          <Row>
            <Col md={6}>
              <dl className="row">
                <dt className="col-sm-6">Service Name:</dt>
                <dd className="col-sm-6">News Search Microservice</dd>
                
                <dt className="col-sm-6">Version:</dt>
                <dd className="col-sm-6">1.0.0</dd>
                
                <dt className="col-sm-6">Environment:</dt>
                <dd className="col-sm-6">
                  <Badge bg="secondary">Development</Badge>
                </dd>
                
                <dt className="col-sm-6">Port:</dt>
                <dd className="col-sm-6">8080</dd>
              </dl>
            </Col>
            <Col md={6}>
              <dl className="row">
                <dt className="col-sm-6">API Status:</dt>
                <dd className="col-sm-6">
                  <Badge bg={getStatusVariant(status)}>
                    {status}
                  </Badge>
                </dd>
                
                <dt className="col-sm-6">NewsAPI:</dt>
                <dd className="col-sm-6">
                  <Badge bg={newsApiAvailable ? 'success' : 'warning'}>
                    {newsApiAvailable ? 'Connected' : 'Disconnected'}
                  </Badge>
                </dd>
                
                <dt className="col-sm-6">Cache:</dt>
                <dd className="col-sm-6">
                  <Badge bg={offlineModeEnabled ? 'success' : 'secondary'}>
                    {offlineModeEnabled ? 'Active' : 'Inactive'}
                  </Badge>
                </dd>
                
                <dt className="col-sm-6">Last Check:</dt>
                <dd className="col-sm-6">
                  {timestamp ? new Date(timestamp).toLocaleTimeString() : 'N/A'}
                </dd>
              </dl>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Health Tips */}
      <Alert variant="info" className="mt-4">
        <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
        <strong>Health Monitoring Tips:</strong>
        <ul className="mb-0 mt-2">
          <li>The service automatically refreshes health status every 30 seconds</li>
          <li>If NewsAPI is unavailable, the service will use offline mode with cached data</li>
          <li>Check this page regularly to monitor service performance</li>
          <li>Contact support if the service status remains unhealthy for extended periods</li>
        </ul>
      </Alert>
    </div>
  );
};

export default HealthCheck;
