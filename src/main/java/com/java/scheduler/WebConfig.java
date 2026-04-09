package com.java.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://m-g-equipment-client-git-main-jordan-bogles-projects.vercel.app") // This connects React server
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
