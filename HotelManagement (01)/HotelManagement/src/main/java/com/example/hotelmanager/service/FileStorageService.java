package com.example.hotelmanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final String uploadDir = "/images/";
    

    
    public String storeFile(MultipartFile file) throws IOException {
    	// Chuẩn hóa thư mục
        Path directoryPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Lấy tên file gốc
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new RuntimeException("Tên file không hợp lệ");
        }

        // Lưu file
        Path filePath = directoryPath.resolve(originalFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về URL không chứa tiền tố
        return uploadDir + originalFileName;
    }
    
    public List<String> storeFiles(MultipartFile[] files) throws IOException {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            // Lưu từng file
            String fileUrl = storeFile(file);
            fileUrls.add(fileUrl);
        }

        return fileUrls;
    }
    
    
    
}

