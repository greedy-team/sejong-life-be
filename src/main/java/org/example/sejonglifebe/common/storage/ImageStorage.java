package org.example.sejonglifebe.common.storage;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

    String uploadImage(Long placeId, MultipartFile image);

    void deleteImages(List<String> imageUrls);
}
