# News Search Microservice - React Frontend

A modern, responsive React frontend for the News Search Microservice that provides an intuitive interface for searching and exploring news articles with intelligent time-based grouping.

## 🚀 Features

### Core Functionality
- **Smart Search Interface**: Real-time input validation and form handling
- **Time-based Grouping**: Visual representation of articles grouped by time intervals
- **Offline Mode Support**: Toggle between online and offline modes
- **Advanced Filtering**: Customizable time intervals (minutes, hours, days, weeks, months, years)
- **Responsive Design**: Mobile-first design that works on all devices

### User Experience
- **Modern UI/UX**: Glass morphism design with smooth animations
- **Interactive Components**: Collapsible news groups, article previews, sharing functionality
- **Real-time Feedback**: Loading states, error handling, success notifications
- **Accessibility**: WCAG compliant with keyboard navigation and screen reader support
- **Performance**: Optimized with React Query caching and lazy loading

### Technical Features
- **React 18**: Latest React with concurrent features
- **TypeScript Ready**: Prepared for TypeScript migration
- **State Management**: React Query for server state, React Hook Form for forms
- **Routing**: React Router with protected routes and navigation
- **Error Boundaries**: Comprehensive error handling and recovery
- **PWA Ready**: Service worker support for offline capabilities

## 🛠️ Technology Stack

### Frontend Framework
- **React 18.2**: Modern React with hooks and concurrent features
- **React Router 6**: Client-side routing and navigation
- **React Bootstrap**: Bootstrap 5 components for React

### State Management & Data Fetching
- **React Query 3**: Server state management with caching
- **React Hook Form 7**: Performant forms with minimal re-renders
- **Axios**: HTTP client with interceptors and error handling

### UI & Styling
- **Bootstrap 5.3**: Modern CSS framework with utilities
- **Font Awesome 6**: Professional icon library
- **Custom CSS**: Advanced styling with CSS variables and animations
- **Google Fonts**: Inter font family for modern typography

### Development & Build Tools
- **Create React App**: Zero-config build setup
- **ESLint**: Code linting and quality checks
- **React Scripts**: Build, test, and development scripts

### Additional Libraries
- **React Hot Toast**: Beautiful toast notifications
- **Date-fns**: Modern date utility library
- **Lodash Debounce**: Input debouncing for performance

## 📦 Installation & Setup

### Prerequisites
- Node.js 16+ and npm/yarn
- News Search Microservice running on port 8080

### Quick Start

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Start development server**
   ```bash
   npm start
   # or
   yarn start
   ```

4. **Open in browser**
   ```
   http://localhost:3000
   ```

### Build for Production

```bash
# Create production build
npm run build

# Serve production build locally (optional)
npx serve -s build
```

## 🏗️ Project Structure

```
frontend/
├── public/
│   ├── index.html          # HTML template
│   └── manifest.json       # PWA manifest
├── src/
│   ├── components/         # React components
│   │   ├── Header.js       # Navigation header
│   │   ├── NewsSearch.js   # Main search page
│   │   ├── SearchForm.js   # Search form component
│   │   ├── SearchResults.js # Results display
│   │   ├── StatsCards.js   # Statistics cards
│   │   ├── NewsGroup.js    # Time group component
│   │   ├── NewsCard.js     # Individual article card
│   │   ├── LoadingSpinner.js # Loading states
│   │   ├── HealthCheck.js  # Health monitoring
│   │   └── About.js        # About page
│   ├── services/
│   │   └── api.js          # API service layer
│   ├── App.js              # Main app component
│   ├── App.css             # App-specific styles
│   ├── index.js            # App entry point
│   └── index.css           # Global styles
├── package.json            # Dependencies and scripts
└── README.md              # This file
```

## 🎨 Component Architecture

### Core Components

#### `NewsSearch.js`
- Main search page component
- Manages search state and results
- Integrates form, results, and error handling

#### `SearchForm.js`
- Advanced search form with validation
- Real-time input validation
- Supports all search parameters

#### `SearchResults.js`
- Displays search results and statistics
- Manages result grouping and presentation
- Handles empty states

#### `NewsCard.js`
- Individual article display
- Image handling with fallbacks
- Share functionality and article preview

#### `HealthCheck.js`
- Service health monitoring
- Real-time status updates
- System information display

### Utility Components

#### `LoadingSpinner.js`
- Reusable loading states
- Multiple animation styles
- Accessible loading indicators

#### `StatsCards.js`
- Statistics visualization
- Animated counters
- Responsive grid layout

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the frontend directory:

```env
# API Configuration
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1/news
REACT_APP_API_TIMEOUT=30000

# Feature Flags
REACT_APP_ENABLE_PWA=true
REACT_APP_ENABLE_ANALYTICS=false

# Development
REACT_APP_DEBUG_MODE=true
```

### Proxy Configuration

The `package.json` includes a proxy configuration for development:

```json
{
  "proxy": "http://localhost:8080"
}
```

This allows the frontend to make API calls to `/api/v1/news/*` without CORS issues during development.

## 🧪 Testing

### Running Tests

```bash
# Run all tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm test -- --watch
```

### Test Structure

```
src/
├── components/
│   └── __tests__/
│       ├── NewsSearch.test.js
│       ├── SearchForm.test.js
│       └── NewsCard.test.js
└── services/
    └── __tests__/
        └── api.test.js
```

## 📱 Responsive Design

### Breakpoints
- **Mobile**: < 576px
- **Tablet**: 576px - 768px
- **Desktop**: 768px - 1200px
- **Large Desktop**: > 1200px

### Key Responsive Features
- Collapsible navigation
- Responsive grid layouts
- Touch-friendly interactions
- Optimized font sizes
- Adaptive component spacing

## 🎯 Performance Optimizations

### React Query Caching
- Automatic background refetching
- Stale-while-revalidate strategy
- Intelligent cache invalidation
- Offline support

### Code Splitting
- Route-based code splitting
- Lazy loading of components
- Dynamic imports for heavy libraries

### Image Optimization
- Lazy loading of article images
- Fallback images for broken links
- Responsive image sizing

## 🔒 Security Features

### Input Validation
- Client-side form validation
- XSS prevention
- Input sanitization

### API Security
- Request/response interceptors
- Error message sanitization
- Timeout configurations

## 🌐 Browser Support

### Supported Browsers
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

### Polyfills Included
- Promise polyfill
- Fetch polyfill
- IntersectionObserver polyfill

## 🚀 Deployment

### Development Deployment

```bash
# Start development server
npm start
```

### Production Deployment

```bash
# Build for production
npm run build

# Deploy to static hosting (Netlify, Vercel, etc.)
# Upload the 'build' folder contents
```

### Docker Deployment

```dockerfile
# Multi-stage build
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 🔧 Development Guidelines

### Code Style
- Use functional components with hooks
- Follow React best practices
- Implement proper error boundaries
- Use TypeScript-ready patterns

### Component Guidelines
- Keep components small and focused
- Use proper prop validation
- Implement accessibility features
- Follow naming conventions

### Performance Guidelines
- Minimize re-renders with useMemo/useCallback
- Implement proper loading states
- Use React.lazy for code splitting
- Optimize bundle size

## 🐛 Troubleshooting

### Common Issues

#### API Connection Issues
```bash
# Check if backend is running
curl http://localhost:8080/api/v1/news/health

# Verify proxy configuration in package.json
```

#### Build Issues
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Clear React Scripts cache
npm start -- --reset-cache
```

#### CORS Issues
- Ensure proxy is configured in package.json
- Check backend CORS configuration
- Verify API endpoints are correct

## 📚 Additional Resources

### Documentation
- [React Documentation](https://reactjs.org/docs)
- [React Bootstrap](https://react-bootstrap.github.io/)
- [React Query](https://react-query.tanstack.com/)
- [React Hook Form](https://react-hook-form.com/)

### Design System
- [Bootstrap 5](https://getbootstrap.com/docs/5.3/)
- [Font Awesome](https://fontawesome.com/icons)
- [Google Fonts](https://fonts.google.com/)

## 🤝 Contributing

1. Follow the existing code style
2. Add tests for new features
3. Update documentation
4. Ensure responsive design
5. Test accessibility features

## 📄 License

This project is licensed under the MIT License - see the main project LICENSE file for details.

---

**Built with ❤️ using React and modern web technologies**
