package fi.livi.digitraffic.tie;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class IkavaTest {
    @Test
    public void ikavaTesti() {
        System.out.println("Runner OS: ${{ runner.os }}");
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.println(envName);
        }
    }
    
}