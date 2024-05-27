package com.news.loaderservice.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "articles")
public class Article {

    @Id
    private String id;
    private String author;
    private String publishedAt;
    private String title;
    private String content;

    // Getters and setters
}
