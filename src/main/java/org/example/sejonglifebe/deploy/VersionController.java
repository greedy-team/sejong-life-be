package org.example.sejonglifebe.deploy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @GetMapping("/version")
    public String getVersion() {
        return activeProfile;
    }
}
