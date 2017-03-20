package fi.livi.digitraffic.tie.metadata.controller;

import fi.livi.digitraffic.tie.annotation.ConditionalOnControllersEnabled;
import fi.livi.digitraffic.tie.annotation.CoverageIgnore;

@CoverageIgnore
@ConditionalOnControllersEnabled
public class HomeController {
//    @RequestMapping("/")
    public String home() {
        return "redirect:swagger-ui.html";
    }
}
