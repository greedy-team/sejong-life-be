package org.example.sejonglifebe.s3;

import java.io.InputStream;

public record ConvertedImage(
        InputStream inputStream,
        long size,
        String contentType,
        String extension) {
}
