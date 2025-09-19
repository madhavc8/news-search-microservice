import React from 'react';
import { Row, Col, Card } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faNewspaper, 
  faLayerGroup, 
  faClock, 
  faWifi,
  faExclamationTriangle
} from '@fortawesome/free-solid-svg-icons';

const StatsCards = ({ totalArticles, totalGroups, intervalValue, intervalUnit, fromCache }) => {
  const statsData = [
    {
      icon: faNewspaper,
      value: totalArticles,
      label: 'Total Articles',
      color: 'primary',
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      icon: faLayerGroup,
      value: totalGroups,
      label: 'Time Groups',
      color: 'success',
      gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)'
    },
    {
      icon: faClock,
      value: `${intervalValue} ${intervalUnit}`,
      label: 'Search Interval',
      color: 'info',
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      icon: fromCache ? faExclamationTriangle : faWifi,
      value: fromCache ? 'Offline' : 'Online',
      label: 'Mode Status',
      color: fromCache ? 'warning' : 'success',
      gradient: fromCache 
        ? 'linear-gradient(135deg, #ff7675 0%, #fd79a8 100%)'
        : 'linear-gradient(135deg, #00b894 0%, #00cec9 100%)'
    }
  ];

  return (
    <Row className="mb-4">
      {statsData.map((stat, index) => (
        <Col key={index} lg={3} md={6} className="mb-3">
          <Card 
            className="stats-card h-100"
            style={{ background: stat.gradient }}
          >
            <Card.Body className="text-center">
              <FontAwesomeIcon 
                icon={stat.icon} 
                size="2x" 
                className="mb-3 opacity-75" 
              />
              <h3 className="mb-2">{stat.value}</h3>
              <p className="mb-0 fw-semibold">{stat.label}</p>
            </Card.Body>
          </Card>
        </Col>
      ))}
    </Row>
  );
};

export default StatsCards;
