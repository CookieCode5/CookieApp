package com.cookiecode.cookieapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final PremiumRecipeInterceptor premiumRecipeInterceptor;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/fridge/**",
                        "/account/**",
                        "/offers/**",
                        "/recipes/new",
                        "/recipes/*/edit",
                        "/recipes/*/delete",
                        "/recipes/*/reviews",
                        "/recipes/*/favorite",
                        "/workshops",
                        "/workshops/**",
                        "/recipes/*/reviews/**",
                        "/admin/**");

        registry.addInterceptor(premiumRecipeInterceptor)
                .addPathPatterns("/recipes/*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
