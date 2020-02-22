package cube8540.oauth.authentication.users.endpoint;

import cube8540.oauth.authentication.error.ErrorMessage;
import cube8540.oauth.authentication.users.application.UserCredentialsService;
import cube8540.oauth.authentication.users.application.UserProfile;
import cube8540.oauth.authentication.users.error.UserExceptionTranslator;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserCredentialsAPIEndpoint {

    private final UserCredentialsService service;

    @Setter
    private UserExceptionTranslator translator = new UserExceptionTranslator();

    @Autowired
    public UserCredentialsAPIEndpoint(UserCredentialsService service) {
        this.service = service;
    }

    @PutMapping(value = "/api/accounts/credentials/{email}")
    public UserProfile credentials(@PathVariable("email") String email, @RequestParam String credentialsKey) {
        return service.accountCredentials(email, credentialsKey);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage<?>> handle(Exception e) {
        return translator.translate(e);
    }
}
