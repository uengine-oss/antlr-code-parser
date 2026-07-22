package legacymodernizer.parser.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public ResponseEntity<String> globalHealthCheck() {
        System.out.println("🔥 Root health check received");
        return ResponseEntity.ok("OK");
    }
}
