package org.example.sejonglifebe.s3;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final static int MAX_SIZE = 10 * 1024 * 1024;
    private final static String KEY_DELIMITER = "-";

    private final S3Client s3Client;
    private final String bucket;

    public S3Service(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String uploadImage(Long placeId, MultipartFile image) {
        validate(image);
        String key = generateKey(placeId, image);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(image.getContentType())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image.getBytes()));
            return key;
        } catch (IOException e) {
            throw new SejongLifeException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (S3Exception e) {
            throw new SejongLifeException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    private String generateKey(Long placeId, MultipartFile image) {
        String ext = org.springframework.util.StringUtils.getFilenameExtension(image.getOriginalFilename());
        return placeId + KEY_DELIMITER + UUID.randomUUID() + (ext != null ? "." + ext : "");
    }

    private void validate(MultipartFile image) {
        if (image.getSize() > MAX_SIZE) {
            throw new SejongLifeException(ErrorCode.FILE_TOO_LARGE);
        }
    }
}
