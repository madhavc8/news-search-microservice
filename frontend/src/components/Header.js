import React from 'react';
import { Navbar, Nav, Container } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faNewspaper, faHeartbeat, faInfoCircle } from '@fortawesome/free-solid-svg-icons';

const Header = () => {
  return (
    <Navbar bg="dark" variant="dark" expand="lg" className="shadow-sm">
      <Container>
        <LinkContainer to="/">
          <Navbar.Brand className="fw-bold">
            <FontAwesomeIcon icon={faNewspaper} className="me-2" />
            News Search Microservice
          </Navbar.Brand>
        </LinkContainer>
        
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ms-auto">
            <LinkContainer to="/">
              <Nav.Link>
                <FontAwesomeIcon icon={faNewspaper} className="me-1" />
                Search
              </Nav.Link>
            </LinkContainer>
            <LinkContainer to="/health">
              <Nav.Link>
                <FontAwesomeIcon icon={faHeartbeat} className="me-1" />
                Health
              </Nav.Link>
            </LinkContainer>
            <LinkContainer to="/about">
              <Nav.Link>
                <FontAwesomeIcon icon={faInfoCircle} className="me-1" />
                About
              </Nav.Link>
            </LinkContainer>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;
