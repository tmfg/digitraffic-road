package fi.livi.digitraffic.tie.metadata.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import fi.livi.digitraffic.tie.annotation.CoverageIgnore;

//@Controller
//@Profile("localhost")
@CoverageIgnore
@ConditionalOnProperty(name = "controllers.enabled", havingValue = "true", matchIfMissing = true)
public class HomeController {
//    @RequestMapping("/")
    public String home() {
        return "redirect:swagger-ui.html";
    }
}
