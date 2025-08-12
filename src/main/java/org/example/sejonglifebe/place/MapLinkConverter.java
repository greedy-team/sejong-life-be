package org.example.sejonglifebe.place;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MapLinkConverter implements AttributeConverter<MapLinks, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(MapLinks attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MapLinks convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, MapLinks.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
