package com.hiphop.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // 允许本地开发环境和生产环境
                        .allowedOriginPatterns(
                            "http://localhost:*",
                            "https://*.netlify.app",  // Netlify 默认域名
                            "https://*.netlify.com",  // Netlify 自定义域名
                            "https://your-custom-domain.com" // 替换为你的自定义域名（如果有）
                        )
                        .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
