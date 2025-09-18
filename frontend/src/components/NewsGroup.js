import React, { useState } from 'react';
import { Card, Row, Col, Badge, Collapse, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faClock, 
  faChevronDown, 
  faChevronUp,
  faNewspaper
} from '@fortawesome/free-solid-svg-icons';
import { format } from 'date-fns';
import NewsCard from './NewsCard';

const NewsGroup = ({ group, index }) => {
  const [isExpanded, setIsExpanded] = useState(index < 2); // Auto-expand first 2 groups

  const {
    intervalLabel,
    startTime,
    endTime,
    count,
    articles = []
  } = group;

  const handleToggle = () => {
    setIsExpanded(!isExpanded);
  };

  const formatDate = (dateString) => {
    try {
      return format(new Date(dateString), 'MMM dd, HH:mm');
    } catch (error) {
      return dateString;
    }
  };

  return (
    <div className="interval-group mb-4">
      <Card className="border-0 shadow-sm">
        <Card.Header 
          className="bg-light border-0 cursor-pointer"
          onClick={handleToggle}
          style={{ cursor: 'pointer' }}
        >
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h4 className="mb-1 text-primary">
                <FontAwesomeIcon icon={faClock} className="me-2" />
                {intervalLabel}
              </h4>
              <small className="text-muted">
                {formatDate(startTime)} - {formatDate(endTime)}
              </small>
            </div>
            <div className="d-flex align-items-center gap-3">
              <Badge bg="primary" className="fs-6">
                <FontAwesomeIcon icon={faNewspaper} className="me-1" />
                {count} articles
              </Badge>
              <FontAwesomeIcon 
                icon={isExpanded ? faChevronUp : faChevronDown}
                className="text-muted"
              />
            </div>
          </div>
        </Card.Header>

        <Collapse in={isExpanded}>
          <div>
            <Card.Body className="pt-3">
              {articles.length > 0 ? (
                <Row>
                  {articles.map((article, articleIndex) => (
                    <Col key={articleIndex} lg={4} md={6} className="mb-4">
                      <NewsCard article={article} />
                    </Col>
                  ))}
                </Row>
              ) : (
                <div className="text-center py-4">
                  <FontAwesomeIcon 
                    icon={faNewspaper} 
                    size="2x" 
                    className="text-muted mb-3" 
                  />
                  <p className="text-muted">No articles available in this time group</p>
                </div>
              )}
            </Card.Body>
          </div>
        </Collapse>
      </Card>
    </div>
  );
};

export default NewsGroup;
