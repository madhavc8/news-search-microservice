import React from 'react';
import { Card, Row, Col, Badge, ListGroup } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faInfoCircle,
  faRocket,
  faCogs,
  faShield,
  faCloud,
  faCode,
  faUsers
} from '@fortawesome/free-solid-svg-icons';

const About = () => {
  const features = [
    {
      icon: faRocket,
      title: 'Time-based Grouping',
      description: 'Intelligent grouping of news articles by customizable time intervals (minutes, hours, days, weeks, months, years)'
    },
    {
      icon: faCloud,
      title: 'Offline Mode',
      description: 'Smart caching system that provides cached results when external APIs are unavailable'
    },
    {
      icon: faShield,
      title: 'Production Ready',
      description: 'Comprehensive security, monitoring, error handling, and performance optimizations'
    },
    {
      icon: faCogs,
      title: 'Modern Architecture',
      description: 'Built with SOLID principles, 12-Factor App methodology, and HATEOAS compliance'
    }
  ];

  const technologies = [
    { name: 'Backend', items: ['Java 17', 'Spring Boot 3.2', 'Spring WebFlux', 'Spring Security', 'Maven'] },
    { name: 'Frontend', items: ['React 18', 'Bootstrap 5', 'React Query', 'React Hook Form', 'Font Awesome'] },
    { name: 'Infrastructure', items: ['Docker', 'Jenkins CI/CD', 'Nginx', 'Prometheus', 'Grafana'] },
    { name: 'Testing', items: ['JUnit 5', 'Mockito', 'Cucumber BDD', 'Spring Boot Test', 'JaCoCo'] }
  ];

  const apiEndpoints = [
    { method: 'GET', path: '/api/v1/news/search', description: 'Search news articles with time grouping' },
    { method: 'POST', path: '/api/v1/news/search', description: 'Search news with JSON request body' },
    { method: 'GET', path: '/api/v1/news/health', description: 'Service health check and status' },
    { method: 'GET', path: '/api/v1/news/info', description: 'API information and documentation' },
    { method: 'GET', path: '/api/v1/news/cache/stats', description: 'Cache statistics and metrics' },
    { method: 'DELETE', path: '/api/v1/news/cache', description: 'Clear all cached data' }
  ];

  return (
    <div className="about-page fade-in">
      {/* Header */}
      <div className="text-center mb-5">
        <h1 className="header-title display-4">
          <FontAwesomeIcon icon={faInfoCircle} className="me-3" />
          About
        </h1>
        <p className="lead text-muted">
          Learn more about the News Search Microservice architecture, features, and capabilities
        </p>
      </div>

      {/* Overview */}
      <Card className="custom-card mb-4">
        <Card.Body>
          <h3 className="mb-3">
            <FontAwesomeIcon icon={faRocket} className="me-2 text-primary" />
            Project Overview
          </h3>
          <p className="mb-3">
            The News Search Microservice is a production-ready application that provides intelligent news article 
            search and grouping capabilities. Built with modern technologies and best practices, it offers a 
            comprehensive solution for news aggregation with time-based categorization.
          </p>
          <p className="mb-0">
            The service integrates with NewsAPI.org to fetch real-time news data and provides advanced features 
            like offline mode, caching, and intelligent time-based grouping to enhance user experience and 
            ensure service reliability.
          </p>
        </Card.Body>
      </Card>

      {/* Key Features */}
      <Card className="custom-card mb-4">
        <Card.Header>
          <h4 className="mb-0">
            <FontAwesomeIcon icon={faCogs} className="me-2" />
            Key Features
          </h4>
        </Card.Header>
        <Card.Body>
          <Row>
            {features.map((feature, index) => (
              <Col key={index} md={6} className="mb-3">
                <div className="d-flex">
                  <div className="me-3">
                    <FontAwesomeIcon 
                      icon={feature.icon} 
                      size="2x" 
                      className="text-primary"
                    />
                  </div>
                  <div>
                    <h6 className="fw-bold">{feature.title}</h6>
                    <p className="text-muted mb-0">{feature.description}</p>
                  </div>
                </div>
              </Col>
            ))}
          </Row>
        </Card.Body>
      </Card>

      {/* Technology Stack */}
      <Card className="custom-card mb-4">
        <Card.Header>
          <h4 className="mb-0">
            <FontAwesomeIcon icon={faCode} className="me-2" />
            Technology Stack
          </h4>
        </Card.Header>
        <Card.Body>
          <Row>
            {technologies.map((category, index) => (
              <Col key={index} md={6} lg={3} className="mb-3">
                <h6 className="fw-bold text-primary mb-2">{category.name}</h6>
                <div className="d-flex flex-wrap gap-1">
                  {category.items.map((item, itemIndex) => (
                    <Badge key={itemIndex} bg="secondary" className="mb-1">
                      {item}
                    </Badge>
                  ))}
                </div>
              </Col>
            ))}
          </Row>
        </Card.Body>
      </Card>

      {/* API Endpoints */}
      <Card className="custom-card mb-4">
        <Card.Header>
          <h4 className="mb-0">
            <FontAwesomeIcon icon={faCloud} className="me-2" />
            API Endpoints
          </h4>
        </Card.Header>
        <Card.Body>
          <ListGroup variant="flush">
            {apiEndpoints.map((endpoint, index) => (
              <ListGroup.Item key={index} className="d-flex justify-content-between align-items-start">
                <div>
                  <div className="fw-bold">
                    <Badge 
                      bg={endpoint.method === 'GET' ? 'success' : endpoint.method === 'POST' ? 'primary' : 'danger'}
                      className="me-2"
                    >
                      {endpoint.method}
                    </Badge>
                    <code>{endpoint.path}</code>
                  </div>
                  <small className="text-muted">{endpoint.description}</small>
                </div>
              </ListGroup.Item>
            ))}
          </ListGroup>
        </Card.Body>
      </Card>

      {/* Architecture Principles */}
      <Row className="mb-4">
        <Col md={6}>
          <Card className="custom-card h-100">
            <Card.Header>
              <h5 className="mb-0">
                <FontAwesomeIcon icon={faShield} className="me-2" />
                Architecture Principles
              </h5>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroup.Item><strong>SOLID Principles:</strong> Single responsibility, dependency injection</ListGroup.Item>
                <ListGroup.Item><strong>12-Factor App:</strong> Configuration, stateless processes, port binding</ListGroup.Item>
                <ListGroup.Item><strong>HATEOAS:</strong> Hypermedia controls and discoverable APIs</ListGroup.Item>
                <ListGroup.Item><strong>Microservices:</strong> Independent, scalable service architecture</ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>
        
        <Col md={6}>
          <Card className="custom-card h-100">
            <Card.Header>
              <h5 className="mb-0">
                <FontAwesomeIcon icon={faUsers} className="me-2" />
                Quality Assurance
              </h5>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroup.Item><strong>Testing:</strong> Unit, Integration, and BDD tests with high coverage</ListGroup.Item>
                <ListGroup.Item><strong>Security:</strong> API key protection, security headers, input validation</ListGroup.Item>
                <ListGroup.Item><strong>Monitoring:</strong> Health checks, metrics, comprehensive logging</ListGroup.Item>
                <ListGroup.Item><strong>Performance:</strong> Caching, connection pooling, reactive programming</ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Project Information */}
      <Card className="custom-card">
        <Card.Body className="text-center">
          <h5 className="mb-3">
            <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
            Project Information
          </h5>
          <p className="mb-3">
            This project demonstrates modern microservice development with production-ready features 
            and comprehensive testing. It showcases best practices in software architecture, 
            security, and user experience design.
          </p>
          <div className="d-flex justify-content-center gap-3">
            <Badge bg="primary" className="fs-6 px-3 py-2">Version 1.0.0</Badge>
            <Badge bg="success" className="fs-6 px-3 py-2">Production Ready</Badge>
            <Badge bg="info" className="fs-6 px-3 py-2">Open Source</Badge>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default About;
