import React from 'react';
import { Row, Col, Card, Badge } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faNewspaper, 
  faClock, 
  faServer,
  faCalendarAlt
} from '@fortawesome/free-solid-svg-icons';
import StatsCards from './StatsCards';
import NewsGroup from './NewsGroup';

const SearchResults = ({ data }) => {
  const {
    keyword,
    intervalValue,
    intervalUnit,
    searchTimestamp,
    fromCache,
    totalArticles,
    intervalGroups,
    message
  } = data;

  const groupEntries = Object.entries(intervalGroups || {});

  return (
    <div className="search-results slide-up">
      {/* Search Summary */}
      <Card className="custom-card mb-4">
        <Card.Body>
          <Row className="align-items-center">
            <Col md={8}>
              <h4 className="mb-2">
                <FontAwesomeIcon icon={faNewspaper} className="me-2 text-primary" />
                Search Results for "{keyword}"
              </h4>
              <div className="d-flex flex-wrap gap-2 mb-2">
                <Badge bg="primary">
                  <FontAwesomeIcon icon={faClock} className="me-1" />
                  {intervalValue} {intervalUnit}
                </Badge>
                <Badge bg={fromCache ? 'warning' : 'success'}>
                  <FontAwesomeIcon icon={faServer} className="me-1" />
                  {fromCache ? 'Offline Mode' : 'Online Mode'}
                </Badge>
                <Badge bg="info">
                  <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                  {new Date(searchTimestamp).toLocaleString()}
                </Badge>
              </div>
              <p className="text-muted mb-0">{message}</p>
            </Col>
            <Col md={4} className="text-end">
              <div className="fs-2 fw-bold text-primary">{totalArticles}</div>
              <div className="text-muted">Total Articles</div>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Statistics Cards */}
      <StatsCards 
        totalArticles={totalArticles}
        totalGroups={groupEntries.length}
        intervalValue={intervalValue}
        intervalUnit={intervalUnit}
        fromCache={fromCache}
      />

      {/* News Groups */}
      {groupEntries.length > 0 ? (
        <div className="news-groups">
          {groupEntries.map(([groupKey, group], index) => (
            <NewsGroup 
              key={groupKey} 
              group={group} 
              index={index}
            />
          ))}
        </div>
      ) : (
        <Card className="custom-card text-center py-5">
          <Card.Body>
            <FontAwesomeIcon icon={faNewspaper} size="3x" className="text-muted mb-3" />
            <h4 className="text-muted">No Articles in Groups</h4>
            <p className="text-muted">
              Articles were found but couldn't be grouped by the specified time intervals.
            </p>
          </Card.Body>
        </Card>
      )}
    </div>
  );
};

export default SearchResults;
