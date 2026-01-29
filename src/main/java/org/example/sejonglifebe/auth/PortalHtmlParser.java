package org.example.sejonglifebe.auth;

import static org.apache.logging.log4j.util.Strings.isBlank;

import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PortalHtmlParser {

    public PortalStudentInfo parseStudentInfo(String html) {
        Document doc = Jsoup.parse(html);

        // "사용자 정보" 테이블 - 고전독서인증현황 페이지
        String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
        List<String> rowValues = new ArrayList<>();

        doc.select(selector).forEach(tr -> {
            String value = tr.select("td").text().trim();
            rowValues.add(value);
        });

        String department = getValueFromList(rowValues, 0);
        String studentId = getValueFromList(rowValues, 1);
        String name = getValueFromList(rowValues, 2);


        // 파싱 결과 검증
        if (studentId == null || studentId.isEmpty() || name == null || name.isEmpty()) {
            log.error("포털 HTML 파싱 실패 - 학번: {}, 이름: {}", studentId, name);
            throw new SejongLifeException(ErrorCode.PORTAL_PARSING_FAILED);
        }

        // 학과는 필수는 아니므로 로그만 남김 (선택)
        if (isBlank(department)) {
            log.warn("포털 HTML 파싱 - 학과 정보 없음 (studentId={})", studentId);
        }

        return PortalStudentInfo.builder()
                .studentId(studentId)
                .name(name)
                .department(department)
                .build();
    }

    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }
}
