package com.example.backend.entities.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration for Rule Validation components
 * Ensures both standard and enhanced services are available
 */
@Configuration
@ComponentScan(basePackages = {"com.example.backend.services", "com.example.backend.Controllers"})
public class RuleValidationConfig {
    
    // The services will be auto-registered by Spring
    // - RuleValidationServiceImpl as primary (@Primary)
    // - RuleValidationServiceImplEnhanced as enhancedRuleValidationService (@Service("enhancedRuleValidationService"))
    
}