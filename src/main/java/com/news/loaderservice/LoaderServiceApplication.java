package com.news.loaderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication
@ComponentScan(basePackages = {"com.news.loaderservice"})
public class LoaderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoaderServiceApplication.class, args);
    }
}
