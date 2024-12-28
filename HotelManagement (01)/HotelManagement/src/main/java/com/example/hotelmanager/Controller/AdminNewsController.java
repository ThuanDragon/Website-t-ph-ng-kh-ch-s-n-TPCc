package com.example.hotelmanager.Controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.hotelmanager.model.News;
import com.example.hotelmanager.service.NewsService;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsService newsService;

    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/news")
    public String listNews(Model model) {
        List<News> newsList = newsService.getAllNews();
        System.out.println("Fetched newsList: " + newsList); // Log dữ liệu
        model.addAttribute("newsList", newsList);
        model.addAttribute("newNews", new News());
        return "admin/news/news1";
    }

    

    @GetMapping("/edit/{id}")
    public String editNewsForm(@PathVariable Long id, Model model) {
        News news = newsService.getNewsById(id);
        if (news == null) {
            // Nếu không tìm thấy, chuyển hướng hoặc trả về lỗi
            return "redirect:/admin/news?error=NewsNotFound";
        }
        model.addAttribute("news", news);
        return "admin/news/edit_news"; // Trả về trang chỉnh sửa
    }

    @PostMapping("/save")
    public String saveNews(@ModelAttribute News news) {
        // Thực hiện validate nếu cần
        newsService.saveNews(news);
        return "redirect:/api/admin/news";
    }
}
