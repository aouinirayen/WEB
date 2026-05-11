package tn.esprit.pidev.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Order(Integer.MAX_VALUE)
public class SpaController {

    @GetMapping(value = {
            "/",
            "/vehicles/**",
            "/maintenance/**",
            "/lines/**",
            "/drivers/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}