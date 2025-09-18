package com.newsservice.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a news article
 * Follows Domain-Driven Design principles
 */
public class NewsArticle {
    
    private String title;
    private String description;
    private String content;
    private String url;
    private String urlToImage;
    
    @JsonProperty("publishedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime publishedAt;
    
    private Source source;
    private String author;

    // Default constructor for JSON deserialization
    public NewsArticle() {}

    public NewsArticle(String title, String description, String content, String url, 
                      String urlToImage, LocalDateTime publishedAt, Source source, String author) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        this.source = source;
        this.author = author;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsArticle that = (NewsArticle) o;
        return Objects.equals(url, that.url) && 
               Objects.equals(publishedAt, that.publishedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, publishedAt);
    }

    @Override
    public String toString() {
        return "NewsArticle{" +
                "title='" + title + '\'' +
                ", publishedAt=" + publishedAt +
                ", source=" + source +
                '}';
    }

    /**
     * Nested class representing the news source
     */
    public static class Source {
        private String id;
        private String name;

        public Source() {}

        public Source(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Source source = (Source) o;
            return Objects.equals(id, source.id) && Objects.equals(name, source.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }

        @Override
        public String toString() {
            return "Source{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
