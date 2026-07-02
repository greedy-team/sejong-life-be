package org.example.sejonglifebe.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PortalStudentInfo {

    private String studentId;
    private String name;
    private String department;
}
