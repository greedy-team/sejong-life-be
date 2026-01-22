package org.example.sejonglifebe.place.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MapLinks {

    @URL(message = "naverMap은 올바른 URL이어야 합니다.")
    private String naverMap;

    @URL(message = "kakaoMap은 올바른 URL이어야 합니다.")
    private String kakaoMap;

    @URL(message = "googleMap은 올바른 URL이어야 합니다.")
    private String googleMap;

}
