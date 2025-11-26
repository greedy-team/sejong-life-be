package org.example.sejonglifebe.common.image;

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
    private static final float COMPRESSION_QUALITY = 0.8f; // 화질 80% 유지 (효율/화질 밸런스)

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

            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());

            if (bufferedImage == null) {
                throw new SejongLifeException(ErrorCode.IMAGE_CONVERT_FAILED);
            }

            byte[] webpBytes = encodeToWebP(bufferedImage);

            return new ConvertedImage(
                    new ByteArrayInputStream(webpBytes),
                    webpBytes.length,
                    CONTENT_TYPE_WEBP,
                    EXT_WEBP
            );
        } catch (SejongLifeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] encodeToWebP(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(CONTENT_TYPE_WEBP);
        if (!writers.hasNext()) {
            throw new SejongLifeException(ErrorCode.IMAGE_CONVERT_FAILED);
        }
        ImageWriter writer = writers.next();

        ImageWriteParam params = writer.getDefaultWriteParam();
        if (params.canWriteCompressed()) {
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(COMPRESSION_QUALITY); // 0.0 ~ 1.0
        }


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {

            writer.setOutput(ios);

            writer.write(null, new IIOImage(image, null, null), params);

            writer.dispose();

            return baos.toByteArray();
        }
    }

}
