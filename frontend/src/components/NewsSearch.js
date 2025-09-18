import React, { useState } from 'react';
import { Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { useQuery } from 'react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faSearch, 
  faClock, 
  faCalendar, 
  faToggleOn, 
  faNewspaper,
  faLayerGroup,
  faServer
} from '@fortawesome/free-solid-svg-icons';
import debounce from 'lodash.debounce';
import SearchForm from './SearchForm';
import SearchResults from './SearchResults';
import LoadingSpinner from './LoadingSpinner';
import { newsApi } from '../services/api';

const NewsSearch = () => {
  const [searchParams, setSearchParams] = useState(null);
  const [isSearching, setIsSearching] = useState(false);

  // React Query for search results
  const {
    data: searchResults,
    isLoading,
    error,
    refetch
  } = useQuery(
    ['newsSearch', searchParams],
    () => newsApi.searchNews(searchParams),
    {
      enabled: !!searchParams,
      onSuccess: () => {
        setIsSearching(false);
        toast.success('News articles loaded successfully!');
      },
      onError: (error) => {
        setIsSearching(false);
        toast.error(`Search failed: ${error.message}`);
      }
    }
  );

  const handleSearch = async (formData) => {
    setIsSearching(true);
    setSearchParams(formData);
  };

  const handleRetry = () => {
    if (searchParams) {
      refetch();
    }
  };

  return (
    <div className="news-search fade-in">
      {/* Header Section */}
      <div className="text-center mb-5">
        <h1 className="header-title display-4">
          <FontAwesomeIcon icon={faNewspaper} className="me-3" />
          News Search
        </h1>
        <p className="lead text-muted">
          Search and explore news articles with intelligent time-based grouping
        </p>
      </div>

      {/* Search Form */}
      <Card className="custom-card mb-4">
        <Card.Body className="p-4">
          <SearchForm onSearch={handleSearch} isLoading={isLoading || isSearching} />
        </Card.Body>
      </Card>

      {/* Loading State */}
      {(isLoading || isSearching) && <LoadingSpinner />}

      {/* Error State */}
      {error && !isLoading && (
        <Alert variant="danger" className="mb-4">
          <FontAwesomeIcon icon={faServer} className="me-2" />
          <strong>Search Error:</strong> {error.message}
          <Button 
            variant="outline-danger" 
            size="sm" 
            className="ms-3"
            onClick={handleRetry}
          >
            Retry
          </Button>
        </Alert>
      )}

      {/* Search Results */}
      {searchResults && !isLoading && !error && (
        <SearchResults data={searchResults} />
      )}

      {/* No Results State */}
      {searchParams && !isLoading && !error && !searchResults?.totalArticles && (
        <Card className="custom-card text-center py-5">
          <Card.Body>
            <FontAwesomeIcon icon={faNewspaper} size="3x" className="text-muted mb-3" />
            <h4 className="text-muted">No News Articles Found</h4>
            <p className="text-muted">
              Try adjusting your search criteria or check your internet connection.
            </p>
            <Button variant="primary" onClick={handleRetry}>
              Try Again
            </Button>
          </Card.Body>
        </Card>
      )}
    </div>
  );
};

export default NewsSearch;
