package org.example.sejonglifebe.common.image;

import java.io.InputStream;

public record ConvertedImage(
        InputStream inputStream,
        long size,
        String contentType,
        String extension) {
}
