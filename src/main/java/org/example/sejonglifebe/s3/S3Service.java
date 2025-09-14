package org.example.sejonglifebe.s3;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service {

    private final static int MAX_SIZE = 10 * 1024 * 1024;
    private final static String KEY_DELIMITER = "-";
    private static final String EXT_HEIC = "heic";
    private static final String EXT_JPG = "jpg";
    private static final String CONTENT_TYPE_JPEG = "image/jpeg";

    private final S3Client s3Client;
    private final String bucket;

    public S3Service(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String uploadImage(Long placeId, MultipartFile image) {
        validate(image);

        String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
        ConvertedImage converted = convertIfNeeded(image, ext);

        String key = generateKey(placeId, converted.extension());

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

    private String generateKey(Long placeId, String ext) {
        return placeId + KEY_DELIMITER + UUID.randomUUID() + (ext != null ? "." + ext : "");
    }

    private ConvertedImage convertIfNeeded(MultipartFile image, String ext) {
        try {
            if (ext != null && ext.equalsIgnoreCase(EXT_HEIC)) {
                BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
                if (bufferedImage == null) {
                    throw new SejongLifeException(ErrorCode.HEIC_CONVERT_FAILED);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, EXT_JPG, baos);

                byte[] bytes = baos.toByteArray();
                return new ConvertedImage(
                        new ByteArrayInputStream(bytes),
                        bytes.length,
                        CONTENT_TYPE_JPEG,
                        EXT_JPG
                );
            }
            byte[] bytes = image.getBytes();
            return new ConvertedImage(
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    image.getContentType(),
                    ext
            );
        } catch (IOException e) {
            throw new SejongLifeException(ErrorCode.HEIC_CONVERT_FAILED, e);
        }
    }

    private void validate(MultipartFile image) {
        if (image.getSize() > MAX_SIZE) {
            throw new SejongLifeException(ErrorCode.FILE_TOO_LARGE);
        }
    }
}
