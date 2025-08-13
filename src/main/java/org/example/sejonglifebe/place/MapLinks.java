package org.example.sejonglifebe.place;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MapLinks {
    private String naverMap;
    private String kakaoMap;
    private String googleMap;
}
