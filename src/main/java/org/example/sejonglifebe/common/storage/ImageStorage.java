package org.example.sejonglifebe.common.storage;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 저장소 추상화.
 * deleteImages는 uploadImage가 반환한 값(URL)을 그대로 받으며
 * 내부 식별자(예: S3 객체 key) 해석은 구현체 책임이다.
 */
public interface ImageStorage {

    String uploadImage(String keyPrefix, MultipartFile image);

    void deleteImages(List<String> imageUrls);
}
