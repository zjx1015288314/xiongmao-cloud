package com.itzjx.upload.controller;

import com.itzjx.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhaojiexiong
 * @create 2020/5/24
 * @since 1.0.0
 */
@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @PostMapping("image")
    public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file){
        return ResponseEntity.ok(uploadService.uploadImage(file));
    }
}
