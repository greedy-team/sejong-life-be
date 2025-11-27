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

    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;

    private static final int COMPRESSION_QUALITY = 80;

    public ConvertedImage convert(MultipartFile image, String ext) {
        try {
            ImmutableImage immutableImage = ImmutableImage.loader().fromBytes(image.getBytes());

            int currentWidth = immutableImage.width;
            int currentHeight = immutableImage.height;


            //if (currentWidth > MAX_WIDTH || currentHeight > MAX_HEIGHT) {
            //    immutableImage = immutableImage.fit(MAX_WIDTH, MAX_HEIGHT);
            //}

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
