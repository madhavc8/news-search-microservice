package com.newsservice.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving static content and frontend application
 * Provides routing for the React-based user interface
 */
@Controller
public class StaticContentController {

    /**
     * Serve the main application page
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Handle client-side routing - redirect all non-API routes to index
     */
    @GetMapping(value = {"/search", "/health", "/about"})
    public String clientRouting() {
        return "forward:/index.html";
    }
}
