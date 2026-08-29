package org.example.sejonglifebe.s3;


import java.util.List;
import org.example.sejonglifebe.common.image.ConvertedImage;
import org.example.sejonglifebe.common.image.ImageConverter;
import org.example.sejonglifebe.common.storage.ImageStorage;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service implements ImageStorage {

    private final static int MAX_SIZE = 30 * 1024 * 1024;
    private final static String KEY_DELIMITER = "-";

    private final S3Client s3Client;
    private final String bucket;
    private final ImageConverter imageConverter;

    public S3Service(S3Client s3Client,
                     @Value("${cloud.aws.s3.bucket}") String bucket,
                     ImageConverter imageConverter) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.imageConverter = imageConverter;
    }

    @Override
    public String uploadImage(String keyPrefix, MultipartFile image) {
        validate(image);

        String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
        ConvertedImage converted = imageConverter.convert(image, ext);

        String key = generateKey(keyPrefix, converted.extension());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(converted.contentType())
                .build();

        try (InputStream inputStream = converted.inputStream()) {

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, converted.size())
            );

            PutObjectAclRequest aclRequest = PutObjectAclRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObjectAcl(aclRequest);

            return s3Client.utilities()
                    .getUrl(b -> b.bucket(bucket).key(key))
                    .toExternalForm();

        } catch (IOException e) {
            throw new SejongLifeException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (S3Exception e) {
            throw new SejongLifeException(ErrorCode.S3_UPLOAD_FAILED, e);
        }
    }

    @Override
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            return;
        }
        List<ObjectIdentifier> identifiers = imageUrls.stream()
                .map(url -> ObjectIdentifier.builder().key(url).build())
                .toList();
        Delete deleteRequest = Delete.builder()
                .objects(identifiers)
                .quiet(false)
                .build();
        DeleteObjectsRequest bulkDeleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(deleteRequest)
                .build();
        try {
            s3Client.deleteObjects(bulkDeleteRequest);
        } catch (S3Exception e) {
            throw new SejongLifeException(ErrorCode.S3_DELETE_FAILED);
        }
    }

    private String generateKey(String keyPrefix, String ext) {
        return keyPrefix + KEY_DELIMITER + UUID.randomUUID() + (ext != null ? "." + ext : "");
    }

    private void validate(MultipartFile image) {
        if (image.getSize() > MAX_SIZE) {
            throw new SejongLifeException(ErrorCode.FILE_TOO_LARGE);
        }
    }
}
