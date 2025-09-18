import React from 'react';
import { Row, Col, Form, Button, InputGroup } from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faSearch, 
  faClock, 
  faCalendar, 
  faToggleOn,
  faSpinner
} from '@fortawesome/free-solid-svg-icons';

const SearchForm = ({ onSearch, isLoading }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    reset
  } = useForm({
    defaultValues: {
      keyword: '',
      intervalValue: 12,
      intervalUnit: 'hours',
      offlineMode: false
    }
  });

  const watchedKeyword = watch('keyword');

  const onSubmit = (data) => {
    // Clean up the data
    const searchData = {
      keyword: data.keyword.trim(),
      intervalValue: data.intervalValue || 12,
      intervalUnit: data.intervalUnit || 'hours',
      offlineMode: data.offlineMode || false
    };
    
    onSearch(searchData);
  };

  const handleClear = () => {
    reset();
  };

  const intervalUnits = [
    { value: 'minutes', label: 'Minutes' },
    { value: 'hours', label: 'Hours' },
    { value: 'days', label: 'Days' },
    { value: 'weeks', label: 'Weeks' },
    { value: 'months', label: 'Months' },
    { value: 'years', label: 'Years' }
  ];

  return (
    <Form onSubmit={handleSubmit(onSubmit)}>
      <Row className="g-3">
        {/* Keyword Input */}
        <Col md={6}>
          <Form.Label htmlFor="keyword" className="fw-semibold">
            <FontAwesomeIcon icon={faSearch} className="me-2 text-primary" />
            Search Keyword
          </Form.Label>
          <InputGroup>
            <InputGroup.Text>
              <FontAwesomeIcon icon={faSearch} />
            </InputGroup.Text>
            <Form.Control
              id="keyword"
              type="text"
              placeholder="e.g., apple, technology, sports"
              className="form-control-custom"
              {...register('keyword', {
                required: 'Please enter a search keyword',
                minLength: {
                  value: 2,
                  message: 'Keyword must be at least 2 characters'
                },
                maxLength: {
                  value: 100,
                  message: 'Keyword must be less than 100 characters'
                }
              })}
              isInvalid={!!errors.keyword}
            />
            <Form.Control.Feedback type="invalid">
              {errors.keyword?.message}
            </Form.Control.Feedback>
          </InputGroup>
        </Col>

        {/* Interval Value */}
        <Col md={2}>
          <Form.Label htmlFor="intervalValue" className="fw-semibold">
            <FontAwesomeIcon icon={faClock} className="me-2 text-primary" />
            Interval Value
          </Form.Label>
          <Form.Control
            id="intervalValue"
            type="number"
            min="1"
            max="100"
            placeholder="12"
            className="form-control-custom"
            {...register('intervalValue', {
              min: {
                value: 1,
                message: 'Interval must be at least 1'
              },
              max: {
                value: 100,
                message: 'Interval must be less than 100'
              }
            })}
            isInvalid={!!errors.intervalValue}
          />
          <Form.Control.Feedback type="invalid">
            {errors.intervalValue?.message}
          </Form.Control.Feedback>
        </Col>

        {/* Interval Unit */}
        <Col md={2}>
          <Form.Label htmlFor="intervalUnit" className="fw-semibold">
            <FontAwesomeIcon icon={faCalendar} className="me-2 text-primary" />
            Interval Unit
          </Form.Label>
          <Form.Select
            id="intervalUnit"
            className="form-select-custom"
            {...register('intervalUnit')}
          >
            {intervalUnits.map((unit) => (
              <option key={unit.value} value={unit.value}>
                {unit.label}
              </option>
            ))}
          </Form.Select>
        </Col>

        {/* Offline Mode Toggle */}
        <Col md={2}>
          <Form.Label className="fw-semibold d-block">
            <FontAwesomeIcon icon={faToggleOn} className="me-2 text-primary" />
            Mode
          </Form.Label>
          <div className="mt-2">
            <Form.Check
              type="switch"
              id="offlineMode"
              label="Offline Mode"
              className="fw-semibold"
              {...register('offlineMode')}
            />
          </div>
        </Col>
      </Row>

      {/* Action Buttons */}
      <Row className="mt-4">
        <Col className="text-center">
          <Button
            type="submit"
            className="btn-gradient me-3"
            size="lg"
            disabled={isLoading || !watchedKeyword?.trim()}
          >
            {isLoading ? (
              <>
                <FontAwesomeIcon icon={faSpinner} spin className="me-2" />
                Searching...
              </>
            ) : (
              <>
                <FontAwesomeIcon icon={faSearch} className="me-2" />
                Search News
              </>
            )}
          </Button>
          
          <Button
            type="button"
            variant="outline-secondary"
            size="lg"
            onClick={handleClear}
            disabled={isLoading}
          >
            Clear
          </Button>
        </Col>
      </Row>

      {/* Search Tips */}
      <Row className="mt-3">
        <Col>
          <div className="text-muted small">
            <strong>Tips:</strong> Use specific keywords for better results. 
            Try different time intervals to see how news is distributed over time.
            Enable offline mode to use cached results when the external API is unavailable.
          </div>
        </Col>
      </Row>
    </Form>
  );
};

export default SearchForm;
