import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import Header from './components/Header';
import NewsSearch from './components/NewsSearch';
import HealthCheck from './components/HealthCheck';
import About from './components/About';
import './App.css';

function App() {
  return (
    <div className="App">
      {/* Floating Background Elements */}
      <div className="floating-elements">
        <div className="floating-circle"></div>
        <div className="floating-circle"></div>
        <div className="floating-circle"></div>
      </div>
      
      <Header />
      <Container fluid className="main-content">
        <div className="glass-morphism main-container fade-in">
          <Routes>
            <Route path="/" element={<NewsSearch />} />
            <Route path="/health" element={<HealthCheck />} />
            <Route path="/about" element={<About />} />
          </Routes>
        </div>
      </Container>
    </div>
  );
}

export default App;
