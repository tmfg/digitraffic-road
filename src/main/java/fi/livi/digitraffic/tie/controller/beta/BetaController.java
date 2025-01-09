package fi.livi.digitraffic.tie.controller.beta;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Beta",
     description = "Beta APIs can change and stop working at any time.")
@RestController
@Validated
@ConditionalOnWebApplication
public class BetaController {

    //@Autowired
    public BetaController() {

    }




}
