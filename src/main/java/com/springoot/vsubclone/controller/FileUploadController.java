package com.springoot.vsubclone.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @PostMapping("/upload_video")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        String uploadDirectory = "uploads/"; // Specify your upload directory
        File uploadDir = new File(uploadDirectory);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try {
            file.transferTo(new File(uploadDirectory + file.getOriginalFilename()));
            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("File upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
