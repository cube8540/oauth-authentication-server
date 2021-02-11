package cube8540.oauth.authentication.oauth.token.domain;

import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import cube8540.oauth.authentication.oauth.client.domain.OAuth2ClientId;
import cube8540.oauth.authentication.oauth.security.AuthorizationCode;
import cube8540.oauth.authentication.oauth.security.AuthorizationRequest;
import cube8540.oauth.authentication.oauth.security.OAuth2TokenRequest;
import cube8540.oauth.authentication.security.AuthorityCode;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2AuthorizationCodeTestHelper {
    static final String RAW_CODE = "CODE";
    static final AuthorizationCode CODE = new AuthorizationCode(RAW_CODE);

    static final String RAW_CLIENT_ID = "CLIENT-ID";
    static final OAuth2ClientId CLIENT_ID = new OAuth2ClientId(RAW_CLIENT_ID);

    static final String RAW_DIFFERENT_CLIENT_ID = "DIFFERENT-CLIENT-ID";

    static final String RAW_USERNAME = "username";
    static final PrincipalUsername USERNAME = new PrincipalUsername(RAW_USERNAME);

    static final String STATE = "STATE";

    static final String RAW_REDIRECT_URI = "http://localhost";
    static final URI REDIRECT_URI = URI.create(RAW_REDIRECT_URI);

    static final String RAW_DIFFERENT_REDIRECT_URI = "http://localhost:8080";
    static final URI DIFFERENT_REDIRECT_URI = URI.create(RAW_DIFFERENT_REDIRECT_URI);

    static final LocalDateTime NOW = LocalDateTime.of(2020, 2, 8, 23, 22);
    static final LocalDateTime EXPIRATION_DATETIME = NOW.plusMinutes(5);

    static final Set<String> RAW_SCOPES = new HashSet<>(Arrays.asList("SCOPE-1", "SCOPE-2", "SCOPE-3"));
    static final Set<AuthorityCode> SCOPES = RAW_SCOPES.stream().map(AuthorityCode::new).collect(Collectors.toSet());

    static final String RAW_CODE_VERIFIER = "FP7Am8xqMbyTCBgSYiTVuVkVv8ffScYCt2wali8JVC8";
    static final CodeVerifier CODE_VERIFIER = new CodeVerifier(RAW_CODE_VERIFIER);

    static final String RAW_DIFFERENT_CODE_VERIFIER = "AnBhW7vVk8j4tdsc6jqcZ6YIwHbWhFzpIctxyTCv8jC";
    static final CodeVerifier DIFFERENT_CODE_VERIFIER = new CodeVerifier(RAW_DIFFERENT_CODE_VERIFIER);

    static final CodeChallengeMethod CODE_CHALLENGE_METHOD = CodeChallengeMethod.S256;

    static final CodeChallenge CODE_CHALLENGE = CodeChallenge.compute(CODE_CHALLENGE_METHOD, CODE_VERIFIER);


    static AuthorizationCodeGenerator makeDefaultCodeGenerator() {
        AuthorizationCodeGenerator generator = mock(AuthorizationCodeGenerator.class);
        when(generator.generate()).thenReturn(RAW_CODE);
        return generator;
    }

    static AuthorizationRequest makeAuthorizationRequest() {
        AuthorizationRequest request = mock(AuthorizationRequest.class);

        when(request.getClientId()).thenReturn(RAW_CLIENT_ID);
        when(request.getUsername()).thenReturn(RAW_USERNAME);
        when(request.getState()).thenReturn(STATE);
        when(request.getRedirectUri()).thenReturn(REDIRECT_URI);
        when(request.getRequestScopes()).thenReturn(RAW_SCOPES);
        when(request.getCodeChallenge()).thenReturn(CODE_CHALLENGE);
        when(request.getCodeChallengeMethod()).thenReturn(CODE_CHALLENGE_METHOD);

        return request;
    }

    static OAuth2TokenRequest makeTokenRequest() {
        OAuth2TokenRequest request = mock(OAuth2TokenRequest.class);

        when(request.getClientId()).thenReturn(RAW_CLIENT_ID);
        when(request.getUsername()).thenReturn(RAW_USERNAME);
        when(request.getState()).thenReturn(STATE);
        when(request.getRedirectUri()).thenReturn(REDIRECT_URI);
        when(request.getScopes()).thenReturn(RAW_SCOPES);
        when(request.getCodeVerifier()).thenReturn(CODE_VERIFIER);

        return request;
    }
}
