package com.hotrodoan.service;

import com.hotrodoan.model.Image;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image saveImage(MultipartFile file) throws Exception;
    Image getImage(String imageId) throws Exception;
    void deleteImage(String imageId) throws Exception;
}
