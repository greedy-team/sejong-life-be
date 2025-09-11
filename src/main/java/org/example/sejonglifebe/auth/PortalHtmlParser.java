package org.example.sejonglifebe.auth;

import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.auth.dto.PortalStudentInfo;
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

    // "사용자 정보" 테이블 - 고전독서인증현황 페이지
    private static final String STUDENT_INFO_TABLE = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
    private static final int STUDENT_INFO_TABLE_INDEX_ID = 1;
    private static final int STUDENT_INFO_TABLE_INDEX_NAME = 2;

    public PortalStudentInfo parseStudentInfo(String html) {
        Document doc = Jsoup.parse(html);

        String selector = STUDENT_INFO_TABLE;
        List<String> rowValues = new ArrayList<>();

        doc.select(selector).forEach(tr -> {
            String value = tr.select("td").text().trim();
            rowValues.add(value);
        });

        String studentId = getValueFromList(rowValues, STUDENT_INFO_TABLE_INDEX_ID);
        String studentName = getValueFromList(rowValues, STUDENT_INFO_TABLE_INDEX_NAME);

        if (studentId == null || studentName == null) {
            log.warn("[PortalLogin] 학생 정보 파싱 실패");
            throw new SejongLifeException(ErrorCode.PORTAL_PARSING_ERROR);
        }

        return new PortalStudentInfo(studentId, studentName);
    }

    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }
}
