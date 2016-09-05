package fi.livi.digitraffic.tie.metadata.controller;

import fi.livi.digitraffic.tie.annotation.CoverageIgnore;

//@Controller
//@Profile("localhost")
@CoverageIgnore
public class HomeController {
//    @RequestMapping("/")
    public String home() {
        return "redirect:swagger-ui.html";
    }
}
