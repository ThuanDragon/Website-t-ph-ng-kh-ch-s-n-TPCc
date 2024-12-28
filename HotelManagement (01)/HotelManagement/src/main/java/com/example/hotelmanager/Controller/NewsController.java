package com.example.hotelmanager.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hotelmanager.model.News;
import com.example.hotelmanager.service.NewsService;

@Controller
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    // Hiển thị tất cả các bài viết
    @GetMapping
    public String getAllNews(Model model) {
        List<News> newsList = newsService.getAllNews();
        model.addAttribute("newsList", newsList);
        return "pages/news"; // Tệp HTML hiển thị danh sách tin tức
    }
    @GetMapping("/{id}")
    public String getNewsDetail(@PathVariable("id") Long id, Model model) {
        News newsDetail = newsService.getNewsById(id);
        model.addAttribute("newsDetail", newsDetail);
        return "pages/news_detail"; // Tệp HTML hiển thị chi tiết bài viết
    }

}
