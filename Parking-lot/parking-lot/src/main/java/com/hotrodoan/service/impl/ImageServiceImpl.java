package com.hotrodoan.service.impl;

import com.hotrodoan.model.Image;
import com.hotrodoan.repository.ImageRepository;
import com.hotrodoan.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {
    private ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Image saveImage(MultipartFile file) throws Exception {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if(fileName.contains("..")){
                throw new Exception("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Image image
                    = new Image(fileName,
                    file.getBytes(),
                    file.getContentType());
            return imageRepository.save(image);
        } catch (Exception e) {
            throw new Exception("Could not store file " + fileName + ". Please try again!", e);
        }
    }

    @Override
    public Image getImage(String imageId) throws Exception{
        return imageRepository
                .findById(imageId)
                .orElseThrow(
                        () -> new RuntimeException("Not found image"));
    }

    @Override
    public void deleteImage(String imageId) throws Exception {
//        Image image = imageRepository
//                .findById(imageId)
//                .orElseThrow(
//                        () -> new RuntimeException("Not found image"));
//        imageRepository.delete(image);
        imageRepository.deleteById(imageId);
    }
}
