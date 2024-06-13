package com.kevin.data_annotation_backendmaster.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {
//    @Override
//    protected void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(jwtInterceptor())   // 配置jwt的拦截器规则
//                .addPathPatterns("/**")            // 拦截所有的请求路径
//                .excludePathPatterns("/web/**")
//                .excludePathPatterns("/video/showVideo");  // 不拦截的路径d
//        super.addInterceptors(registry);
//    }
//
//    @Bean
//    public JwtInterceptor jwtInterceptor() {
//        return new JwtInterceptor();
//    }

//    @Override
//    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**")
//                .addResourceLocations("classpath:/static/");
//        super.addResourceHandlers(registry);
//    }
}
