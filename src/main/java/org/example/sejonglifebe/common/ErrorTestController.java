package org.example.sejonglifebe.common;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("/test")
public class ErrorTestController {

    @GetMapping("/boom")
    public String boom() { throw new RuntimeException("boom"); }

    @GetMapping("/npe")
    public String npe() { String s = null; s.length(); return "never"; }

    @GetMapping("/div0")
    public String div0() { int x = 1/0; return "never"; }
}
