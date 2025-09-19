import axios from 'axios';

// Create axios instance with base configuration
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';

// Debug: Log the API base URL being used
console.log('🚀 API_BASE_URL:', API_BASE_URL);
console.log('🌍 Environment variables:', {
  NODE_ENV: process.env.NODE_ENV,
  REACT_APP_API_BASE_URL: process.env.REACT_APP_API_BASE_URL
});
console.log('🔍 All process.env keys:', Object.keys(process.env).filter(key => key.startsWith('REACT_APP_')));

const api = axios.create({
  baseURL: `${API_BASE_URL}/news`,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for logging and auth
api.interceptors.request.use(
  (config) => {
    console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`);
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    console.log(`Response received from ${response.config.url}:`, response.status);
    return response;
  },
  (error) => {
    console.error('Response error:', error);
    
    // Handle different error types
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;
      
      switch (status) {
        case 400:
          throw new Error(data.message || 'Bad Request - Please check your input');
        case 401:
          throw new Error('Unauthorized - Authentication required');
        case 403:
          throw new Error('Forbidden - Access denied');
        case 404:
          throw new Error('Not Found - The requested resource was not found');
        case 429:
          throw new Error('Rate Limited - Too many requests, please try again later');
        case 500:
          throw new Error('Server Error - Internal server error occurred');
        case 502:
          throw new Error('Bad Gateway - External service unavailable');
        case 503:
          throw new Error('Service Unavailable - Service is temporarily down');
        default:
          throw new Error(data.message || `HTTP Error ${status}`);
      }
    } else if (error.request) {
      // Network error
      throw new Error('Network Error - Unable to connect to the server');
    } else {
      // Other error
      throw new Error(error.message || 'An unexpected error occurred');
    }
  }
);

// API service methods
export const newsApi = {
  /**
   * Search for news articles
   * @param {Object} params - Search parameters
   * @param {string} params.keyword - Search keyword
   * @param {number} params.intervalValue - Time interval value
   * @param {string} params.intervalUnit - Time interval unit
   * @param {boolean} params.offlineMode - Enable offline mode
   * @returns {Promise} Search results
   */
  searchNews: async (params) => {
    const { keyword, intervalValue, intervalUnit, offlineMode } = params;
    
    // Build query parameters
    const queryParams = new URLSearchParams();
    queryParams.append('keyword', keyword);
    
    if (intervalValue) {
      queryParams.append('intervalValue', intervalValue.toString());
    }
    
    if (intervalUnit) {
      queryParams.append('intervalUnit', intervalUnit);
    }
    
    if (offlineMode) {
      queryParams.append('offlineMode', 'true');
    }
    
    const response = await api.get(`/search?${queryParams.toString()}`);
    return response.data;
  },

  /**
   * Search for news articles using POST method
   * @param {Object} searchRequest - Search request object
   * @returns {Promise} Search results
   */
  searchNewsPost: async (searchRequest) => {
    const response = await api.post('/search', searchRequest);
    return response.data;
  },

  /**
   * Get service health status
   * @returns {Promise} Health status
   */
  getHealth: async () => {
    const response = await api.get('/health');
    return response.data;
  },

  /**
   * Get API information
   * @returns {Promise} API information
   */
  getInfo: async () => {
    const response = await api.get('/info');
    return response.data;
  },

  /**
   * Get cache statistics
   * @returns {Promise} Cache statistics
   */
  getCacheStats: async () => {
    const response = await api.get('/cache/stats');
    return response.data;
  },

  /**
   * Clear cache
   * @returns {Promise} Clear cache result
   */
  clearCache: async () => {
    const response = await api.delete('/cache');
    return response.data;
  },
};

// Utility functions for API calls
export const apiUtils = {
  /**
   * Check if the API is available
   * @returns {Promise<boolean>} API availability status
   */
  checkApiAvailability: async () => {
    try {
      await newsApi.getHealth();
      return true;
    } catch (error) {
      console.warn('API availability check failed:', error.message);
      return false;
    }
  },

  /**
   * Retry an API call with exponential backoff
   * @param {Function} apiCall - The API call function
   * @param {number} maxRetries - Maximum number of retries
   * @param {number} baseDelay - Base delay in milliseconds
   * @returns {Promise} API call result
   */
  retryApiCall: async (apiCall, maxRetries = 3, baseDelay = 1000) => {
    let lastError;
    
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        return await apiCall();
      } catch (error) {
        lastError = error;
        
        if (attempt === maxRetries) {
          break;
        }
        
        // Exponential backoff
        const delay = baseDelay * Math.pow(2, attempt);
        console.log(`API call failed, retrying in ${delay}ms... (attempt ${attempt + 1}/${maxRetries + 1})`);
        
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
    
    throw lastError;
  },

  /**
   * Format error message for display
   * @param {Error} error - The error object
   * @returns {string} Formatted error message
   */
  formatErrorMessage: (error) => {
    if (error.response?.data?.message) {
      return error.response.data.message;
    }
    
    if (error.message) {
      return error.message;
    }
    
    return 'An unexpected error occurred';
  },
};

export default api;
