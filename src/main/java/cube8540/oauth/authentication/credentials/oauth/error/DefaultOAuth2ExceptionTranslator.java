package cube8540.oauth.authentication.credentials.oauth.error;

import cube8540.oauth.authentication.credentials.oauth.AbstractOAuth2AuthenticationException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.HttpRequestMethodNotSupportedException;

public class DefaultOAuth2ExceptionTranslator implements OAuth2ExceptionTranslator {
    @Override
    public ResponseEntity<OAuth2Error> translate(Exception exception) {
        if (exception instanceof AbstractOAuth2AuthenticationException) {
            return createResponseEntity(((AbstractOAuth2AuthenticationException) exception));
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            return createResponseEntity(new MethodNotAllowedException(exception.getMessage()));
        } else {
            return createResponseEntity(new ServerErrorException(exception.getMessage()));
        }
    }

    private ResponseEntity<OAuth2Error> createResponseEntity(AbstractOAuth2AuthenticationException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore());
        headers.setPragma("no-cache");

        return new ResponseEntity<>(e.getError(), headers, HttpStatus.valueOf(e.getStatusCode()));
    }

    private static class MethodNotAllowedException extends AbstractOAuth2AuthenticationException {

        private static final String ERROR_CODE = "method_not_allowed";

        public MethodNotAllowedException(String message) {
            super(405, new OAuth2Error(ERROR_CODE, message, null));
        }
    }

    private static class ServerErrorException extends AbstractOAuth2AuthenticationException {

        public ServerErrorException(String message) {
            super(500, new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, message, null));
        }
    }
}
