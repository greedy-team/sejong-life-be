package org.example.sejonglifebe.meeting.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;

@Getter
@RequiredArgsConstructor
public enum FaceType {
    DOG("강아지상"),
    CAT("고양이상"),
    RABBIT("토끼상"),
    BEAR("곰상"),
    DEER("사슴상"),
    FOX("여우상"),
    DINOSAUR("공룡상");

    private final String description;

    @JsonCreator
    public static FaceType from(String value) {
        for (FaceType faceType : FaceType.values()) {
            if (faceType.name().equals(value)) {
                return faceType;
            }
        }
        throw new SejongLifeException(ErrorCode.INVALID_FACE_TYPE);
    }
}
