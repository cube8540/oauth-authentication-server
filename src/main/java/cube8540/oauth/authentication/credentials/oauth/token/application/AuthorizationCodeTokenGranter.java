package cube8540.oauth.authentication.credentials.oauth.token.application;

import cube8540.oauth.authentication.credentials.oauth.AuthorizationRequest;
import cube8540.oauth.authentication.credentials.oauth.OAuth2TokenRequest;
import cube8540.oauth.authentication.credentials.oauth.client.OAuth2ClientDetails;
import cube8540.oauth.authentication.credentials.oauth.error.InvalidGrantException;
import cube8540.oauth.authentication.credentials.oauth.error.InvalidRequestException;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeId;
import cube8540.oauth.authentication.credentials.oauth.token.domain.AuthorizationCode;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenRepository;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AuthorizationCode;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AuthorizedAccessToken;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2TokenIdGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationCodeTokenGranter extends AbstractOAuth2TokenGranter {

    private final OAuth2AuthorizationCodeConsumer authorizationCodeConsumer;

    public AuthorizationCodeTokenGranter(OAuth2TokenIdGenerator tokenIdGenerator, OAuth2AccessTokenRepository tokenRepository,
                                         OAuth2AuthorizationCodeConsumer authorizationCodeConsumer) {
        super(tokenIdGenerator, tokenRepository);
        this.authorizationCodeConsumer = authorizationCodeConsumer;
    }

    @Override
    public OAuth2AuthorizedAccessToken createAccessToken(OAuth2ClientDetails clientDetails, OAuth2TokenRequest tokenRequest) {
        OAuth2AuthorizationCode authorizationCode = authorizationCodeConsumer.consume(new AuthorizationCode(tokenRequest.code()))
                .orElseThrow(() -> InvalidRequestException.invalidRequest("authorization code not found"));

        Set<String> authorizationCodeScope = Optional.ofNullable(authorizationCode.getApprovedScopes())
                .orElse(Collections.emptySet()).stream().map(OAuth2ScopeId::getValue).collect(Collectors.toSet());
        if (authorizationCodeScope.isEmpty()) {
            throw InvalidGrantException.invalidScope("cannot not grant empty scope");
        }
        if (!getTokenRequestValidator().validateScopes(clientDetails, authorizationCodeScope)) {
            throw InvalidGrantException.invalidScope("cannot grant scope");
        }
        authorizationCode.validateWithAuthorizationRequest(new AuthorizationCodeRequest(clientDetails, tokenRequest));
        OAuth2AuthorizedAccessToken accessToken = OAuth2AuthorizedAccessToken.builder(getTokenIdGenerator())
                .expiration(extractTokenExpiration(clientDetails))
                .client(authorizationCode.getClientId())
                .email(authorizationCode.getEmail())
                .scope(authorizationCode.getApprovedScopes())
                .tokenGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();
        accessToken.generateRefreshToken(refreshTokenGenerator(), extractRefreshTokenExpiration(clientDetails));
        return accessToken;
    }

    private static class AuthorizationCodeRequest implements AuthorizationRequest {

        private OAuth2ClientDetails clientDetails;
        private OAuth2TokenRequest tokenRequest;

        private AuthorizationCodeRequest(OAuth2ClientDetails clientDetails, OAuth2TokenRequest tokenRequest) {
            this.clientDetails = clientDetails;
            this.tokenRequest = tokenRequest;
        }

        @Override
        public String clientId() {
            return clientDetails.getClientId();
        }

        @Override
        public String username() {
            return tokenRequest.username();
        }

        @Override
        public String state() {
            return tokenRequest.state();
        }

        @Override
        public URI redirectURI() {
            return tokenRequest.redirectURI();
        }

        @Override
        public Set<String> requestScopes() {
            return tokenRequest.scopes();
        }

        @Override
        public OAuth2AuthorizationResponseType responseType() {
            return OAuth2AuthorizationResponseType.CODE;
        }

        @Override
        public void setRedirectURI(URI redirectURI) {
            throw new UnsupportedOperationException(getClass().getName() + "#setRedirectURI");
        }

        @Override
        public void setRequestScopes(Set<String> requestScope) {
            throw new UnsupportedOperationException(getClass().getName() + "#setRequestScopes");
        }
    }
}
