package com.example.hotelmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.News;
import com.example.hotelmanager.repository.NewsRepository;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public List<News> getAllNews() {
        return newsRepository.findAll();
    }
    public News getNewsById(Long id) {
        return newsRepository.findById(id).orElse(null);
    }
    
    public News saveNews(News news) {
        return newsRepository.save(news);
    }
}
