package org.example.sejonglifebe.common.image;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.web.multipart.MultipartFile;

public class ImageConverter {
    private static final String EXT_WEBP = "webp";
    private static final String CONTENT_TYPE_WEBP = "image/webp";
    // 화질 설정: 0~100 (ImageIO의 0.8f와 비슷하게 80 정도로 설정)
    private static final int COMPRESSION_QUALITY = 80;

    public ConvertedImage convert(MultipartFile image, String ext) {
        try {
            if (ext != null && ext.equalsIgnoreCase(EXT_WEBP)) {
                byte[] bytes = image.getBytes();
                return new ConvertedImage(
                        new ByteArrayInputStream(bytes),
                        bytes.length,
                        CONTENT_TYPE_WEBP,
                        EXT_WEBP
                );
            }

            byte[] webpBytes = ImmutableImage.loader()
                    .fromBytes(image.getBytes())
                    .bytes(WebpWriter.DEFAULT.withQ(COMPRESSION_QUALITY)); // Q: Quality

            return new ConvertedImage(
                    new ByteArrayInputStream(webpBytes),
                    webpBytes.length,
                    CONTENT_TYPE_WEBP,
                    EXT_WEBP
            );

        } catch (IOException e) {
            throw new SejongLifeException(ErrorCode.IMAGE_CONVERT_FAILED, e);
        } catch (Exception e) {
            // Scrimage 내부에서 발생할 수 있는 이미지 처리 예외 잡기
            throw new SejongLifeException(ErrorCode.IMAGE_CONVERT_FAILED, e);
        }
    }


}
