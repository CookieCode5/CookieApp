package com.cookiecode.cookieapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    // Every new page that requires a logged-in user must be added here
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
                        "/workshops/new",
                        "/workshops/*/join",
                        "/workshops/*/leave",
                        "/admin/**");
    }
}
