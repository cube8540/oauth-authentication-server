package cube8540.oauth.authentication.credentials.oauth.error;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

public class InvalidRequestException extends AbstractOAuth2AuthenticationException {

    private static final int HTTP_STATUS_CODE = 400;

    public InvalidRequestException(String message) {
        super(HTTP_STATUS_CODE, new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, message, null));
    }

}
