package tn.esprit.pidev.config;

import tn.esprit.pidev.interceptor.AuditLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration for serving static files from htdocs
 * Allows direct HTTP access to uploaded documents and photos
 * Includes interceptor for audit logging
 * 
 * Example URLs:
 * - http://localhost:8081/pidev-uploads/1/document.pdf
 * - http://localhost:8081/pidev-uploads/2/photo.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload.dir:C:/xampp/htdocs/pidev-uploads}")
    private String uploadDir;

    @Autowired(required = false)
    private AuditLoggingInterceptor auditLoggingInterceptor;

    /**
     * Register interceptors for automatic audit logging
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (auditLoggingInterceptor != null) {
            registry.addInterceptor(auditLoggingInterceptor)
                    .addPathPatterns("/api/**"); // Intercept all API endpoints
        }
    }

    /**
     * Configure resource handlers to serve static files from htdocs directory
     * This allows users to directly access files via HTTP URLs
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from pidev-uploads directory
        // Maps: /pidev-uploads/** -> file:C:/xampp/htdocs/pidev-uploads/
        registry.addResourceHandler("/pidev-uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600) // Cache for 1 hour
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver());

        // Alternative: You can also serve from root if needed
        // registry.addResourceHandler("/**")
        //        .addResourceLocations("file:" + uploadDir + "/");
    }
}

