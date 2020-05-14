package cube8540.oauth.authentication.credentials.oauth.token.application;

import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2ClientId;
import cube8540.oauth.authentication.credentials.oauth.error.InvalidGrantException;
import cube8540.oauth.authentication.credentials.oauth.error.InvalidRequestException;
import cube8540.oauth.authentication.credentials.oauth.error.UserDeniedAuthorizationException;
import cube8540.oauth.authentication.credentials.oauth.security.OAuth2ClientDetails;
import cube8540.oauth.authentication.credentials.oauth.security.OAuth2TokenRequest;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenRepository;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AuthorizedAccessToken;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2TokenIdGenerator;
import cube8540.oauth.authentication.credentials.oauth.token.domain.PrincipalUsername;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.time.LocalDateTime;

public class ResourceOwnerPasswordTokenGranter extends AbstractOAuth2TokenGranter {

    private final AuthenticationManager authenticationManager;

    public ResourceOwnerPasswordTokenGranter(OAuth2TokenIdGenerator tokenIdGenerator, OAuth2AccessTokenRepository tokenRepository,
                                             AuthenticationManager authenticationManager) {
        super(tokenIdGenerator, tokenRepository);
        this.authenticationManager = authenticationManager;
    }

    @Override
    public OAuth2AuthorizedAccessToken createAccessToken(OAuth2ClientDetails clientDetails, OAuth2TokenRequest tokenRequest) {
        if (tokenRequest.getUsername() == null || tokenRequest.getPassword() == null) {
            throw InvalidRequestException.invalidRequest("username, password is required");
        }
        if (!getTokenRequestValidator().validateScopes(clientDetails, tokenRequest.getScopes())) {
            throw InvalidGrantException.invalidScope("cannot grant scopes");
        }
        Authentication authentication = authentication(tokenRequest);
        OAuth2AuthorizedAccessToken accessToken = OAuth2AuthorizedAccessToken.builder(getTokenIdGenerator())
                .expiration(extractTokenExpiration(clientDetails))
                .client(new OAuth2ClientId(clientDetails.getClientId()))
                .username(new PrincipalUsername(authentication.getName()))
                .scopes(extractGrantScope(clientDetails, tokenRequest))
                .tokenGrantType(AuthorizationGrantType.PASSWORD)
                .issuedAt(LocalDateTime.now(clock))
                .build();
        accessToken.generateRefreshToken(refreshTokenGenerator(), extractRefreshTokenExpiration(clientDetails));
        return accessToken;
    }

    private Authentication authentication(OAuth2TokenRequest tokenRequest) {
        try {
            UsernamePasswordAuthenticationToken usernamePasswordToken =
                    new UsernamePasswordAuthenticationToken(tokenRequest.getUsername(), tokenRequest.getPassword());
            return authenticationManager.authenticate(usernamePasswordToken);
        } catch (BadCredentialsException | AccountStatusException e) {
            throw new UserDeniedAuthorizationException(e.getMessage());
        }
    }
}
