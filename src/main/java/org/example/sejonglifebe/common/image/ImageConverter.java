package org.example.sejonglifebe.common.image;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageConverter {
    private static final String EXT_WEBP = "webp";
    private static final String CONTENT_TYPE_WEBP = "image/webp";

    private static final int COMPRESSION_QUALITY = 80;

    public ConvertedImage convert(MultipartFile image, String ext) {
        return convert(image, ext, 0, 0);
    }

    public ConvertedImage convert(MultipartFile image, String ext, int width, int height) {
        try {
            if (ext != null && ext.equalsIgnoreCase(EXT_WEBP) && width <= 0) {
                byte[] bytes = image.getBytes();
                return new ConvertedImage(
                        new ByteArrayInputStream(bytes),
                        bytes.length,
                        CONTENT_TYPE_WEBP,
                        EXT_WEBP
                );
            }

            ImmutableImage immutableImage = ImmutableImage.loader().fromBytes(image.getBytes());

            if (width > 0 && height > 0) {
                immutableImage = immutableImage.scaleTo(width, height);
            }

            byte[] webpBytes = immutableImage.bytes(WebpWriter.DEFAULT.withQ(COMPRESSION_QUALITY));

            return new ConvertedImage(
                    new ByteArrayInputStream(webpBytes),
                    webpBytes.length,
                    CONTENT_TYPE_WEBP,
                    EXT_WEBP
            );

        } catch (IOException e) {
            throw new SejongLifeException(ErrorCode.IMAGE_CONVERT_FAILED, e);
        }
    }
}
