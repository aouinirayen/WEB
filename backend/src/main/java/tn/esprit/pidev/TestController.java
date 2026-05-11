package tn.esprit.pidev;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")

public class TestController {
    @GetMapping("/hello")  // → full path is /api/hello

    public String hello() {
        return "Hello mello Spring Boot!";
    }
}