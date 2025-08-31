package org.example.sejonglifebe.auth;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        String studentId = getValueFromList(rowValues, 1);
        String name = getValueFromList(rowValues, 2);

        return PortalStudentInfo.builder()
                .studentId(studentId)
                .name(name)
                .build();
    }

    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }
}
