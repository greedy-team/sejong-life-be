package org.example.sejonglifebe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

import java.lang.reflect.Type;

public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper,
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return (mediaType == null ||
                mediaType.isCompatibleWith(MediaType.APPLICATION_JSON) ||
                mediaType.isCompatibleWith(MediaType.TEXT_PLAIN) ||
                mediaType.isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}
