package org.example.sejonglifebe.common.config;

public class SwaggerDescription {

    private static final String FACE_TYPE = """
            <details>
            <summary><b>😊 FaceType (얼굴상)</b></summary>
            <div style="margin-left:10px; line-height:1.6;">
            • DOG: 강아지상<br>
            • CAT: 고양이상<br>
            • FOX: 여우상<br>
            • RABBIT: 토끼상<br>
            • BEAR: 곰상<br>
            • DEER: 사슴상<br>
            • HAMSTER: 햄스터상<br>
            • DINOSAUR: 공룡상<br>
            </div>
            </details>
            """;

    private static final String GENDER = """
            <details>
            <summary><b>👤 Gender (성별)</b></summary>
            <div style="margin-left:10px; line-height:1.6;">
            • MALE: 남성<br>
            • FEMALE: 여성<br>
            </div>
            </details>
            """;

    public static String get() {
        return """
                <div style="font-weight:bold; margin-bottom:10px;">< 공통 ENUM 용어 정리 ></div>
                """ + FACE_TYPE + GENDER;
    }

    private SwaggerDescription() {}
}
