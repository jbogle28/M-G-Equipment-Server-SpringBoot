package com.java.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
	    registry.addMapping("/**")
	            .allowedOrigins(
	                "https://m-g-equipment-client.vercel.app",
	                "https://m-g-equipment-client-git-main-jordan-bogles-projects.vercel.app",
	                "https://m-g-equipment-client-o7kpoqfya-jordan-bogles-projects.vercel.app",
	                "http://localhost:5173" // Keeps your local development working
	            )
	            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
	            .allowedHeaders("*")
	            .allowCredentials(true);
	}
}
