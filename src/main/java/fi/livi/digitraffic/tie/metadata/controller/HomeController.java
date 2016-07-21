package fi.livi.digitraffic.tie.metadata.controller;

//@Controller
//@Profile("localhost")
public class HomeController {
//    @RequestMapping("/")
    public String home() {
        return "redirect:swagger-ui.html";
    }
}