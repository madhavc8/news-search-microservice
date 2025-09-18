import React, { useState } from 'react';
import { Card, Badge, Button, Modal } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faExternalLinkAlt, 
  faShare, 
  faUser, 
  faCalendarAlt,
  faEye,
  faCopy,
  faCheck
} from '@fortawesome/free-solid-svg-icons';
import { format } from 'date-fns';
import { toast } from 'react-hot-toast';

const NewsCard = ({ article }) => {
  const [showModal, setShowModal] = useState(false);
  const [imageError, setImageError] = useState(false);
  const [copied, setCopied] = useState(false);

  const {
    title,
    description,
    content,
    url,
    urlToImage,
    source,
    author,
    publishedAt
  } = article;

  const formatDate = (dateString) => {
    try {
      return format(new Date(dateString), 'MMM dd, yyyy HH:mm');
    } catch (error) {
      return dateString;
    }
  };

  const truncateText = (text, maxLength) => {
    if (!text) return 'No description available';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength).trim() + '...';
  };

  const handleShare = async () => {
    try {
      if (navigator.share) {
        await navigator.share({
          title: title,
          text: description,
          url: url
        });
        toast.success('Article shared successfully!');
      } else {
        // Fallback to clipboard
        await navigator.clipboard.writeText(url);
        setCopied(true);
        toast.success('Article URL copied to clipboard!');
        setTimeout(() => setCopied(false), 2000);
      }
    } catch (error) {
      console.error('Error sharing:', error);
      toast.error('Failed to share article');
    }
  };

  const handleImageError = () => {
    setImageError(true);
  };

  const defaultImage = 'https://via.placeholder.com/300x200/6c757d/ffffff?text=No+Image';

  return (
    <>
      <Card className="news-card h-100">
        {/* Article Image */}
        {urlToImage && !imageError ? (
          <Card.Img
            variant="top"
            src={urlToImage}
            alt={title}
            onError={handleImageError}
            style={{ height: '200px', objectFit: 'cover' }}
          />
        ) : (
          <Card.Img
            variant="top"
            src={defaultImage}
            alt="No image available"
            style={{ height: '200px', objectFit: 'cover' }}
          />
        )}

        <Card.Body className="d-flex flex-column">
          {/* Source and Date */}
          <div className="d-flex justify-content-between align-items-start mb-2">
            <Badge bg="primary" className="source-badge">
              {source?.name || 'Unknown Source'}
            </Badge>
            <small className="text-muted">
              <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
              {formatDate(publishedAt)}
            </small>
          </div>

          {/* Title */}
          <Card.Title className="h6 mb-2 flex-grow-0">
            <a 
              href={url} 
              target="_blank" 
              rel="noopener noreferrer"
              className="text-decoration-none text-dark"
              title={title}
            >
              {truncateText(title, 80)}
            </a>
          </Card.Title>

          {/* Description */}
          <Card.Text className="text-muted small mb-3 flex-grow-1">
            {truncateText(description, 120)}
          </Card.Text>

          {/* Author */}
          {author && (
            <div className="d-flex align-items-center mb-3">
              <FontAwesomeIcon icon={faUser} className="me-2 text-muted" />
              <small className="text-muted">{truncateText(author, 30)}</small>
            </div>
          )}

          {/* Action Buttons */}
          <div className="d-flex justify-content-between align-items-center mt-auto">
            <Button
              variant="outline-primary"
              size="sm"
              href={url}
              target="_blank"
              rel="noopener noreferrer"
            >
              <FontAwesomeIcon icon={faExternalLinkAlt} className="me-1" />
              Read More
            </Button>

            <div className="d-flex gap-2">
              <Button
                variant="outline-info"
                size="sm"
                onClick={() => setShowModal(true)}
                title="Preview"
              >
                <FontAwesomeIcon icon={faEye} />
              </Button>
              
              <Button
                variant="outline-secondary"
                size="sm"
                onClick={handleShare}
                title="Share"
              >
                <FontAwesomeIcon icon={copied ? faCheck : faShare} />
              </Button>
            </div>
          </div>
        </Card.Body>
      </Card>

      {/* Article Preview Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)} size="lg">
        <Modal.Header closeButton>
          <Modal.Title className="h5">{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {urlToImage && !imageError && (
            <img
              src={urlToImage}
              alt={title}
              className="img-fluid mb-3 rounded"
              onError={handleImageError}
            />
          )}
          
          <div className="mb-3">
            <Badge bg="primary" className="me-2">
              {source?.name || 'Unknown Source'}
            </Badge>
            <small className="text-muted">
              <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
              {formatDate(publishedAt)}
            </small>
          </div>

          {author && (
            <p className="text-muted mb-3">
              <FontAwesomeIcon icon={faUser} className="me-2" />
              By {author}
            </p>
          )}

          <p className="mb-3">{description}</p>
          
          {content && content !== description && (
            <div>
              <h6>Full Content:</h6>
              <p>{content}</p>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Close
          </Button>
          <Button
            variant="primary"
            href={url}
            target="_blank"
            rel="noopener noreferrer"
          >
            <FontAwesomeIcon icon={faExternalLinkAlt} className="me-2" />
            Read Full Article
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default NewsCard;
