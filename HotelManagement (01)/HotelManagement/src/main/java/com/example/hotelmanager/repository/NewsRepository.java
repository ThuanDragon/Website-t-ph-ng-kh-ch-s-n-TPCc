package com.example.hotelmanager.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
}
