package cube8540.oauth.authentication.credentials.oauth.token.application;

import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeId;
import cube8540.oauth.authentication.credentials.oauth.security.OAuth2AccessTokenDetails;
import cube8540.oauth.authentication.credentials.oauth.security.OAuth2RefreshTokenDetails;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AuthorizedAccessToken;
import cube8540.oauth.authentication.credentials.oauth.token.domain.PrincipalUsername;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Builder
public class DefaultAccessTokenDetails implements OAuth2AccessTokenDetails {

    private static final String TOKEN_TYPE = "Bearer";

    private String tokenValue;
    private String clientId;
    private Set<String> scopes;
    private String tokenType;
    private String username;
    private Map<String, String> additionalInformation;
    private LocalDateTime expiration;
    private long expiresIn;
    private OAuth2RefreshTokenDetails refreshToken;
    private boolean isExpired;

    public static DefaultAccessTokenDetails of(OAuth2AuthorizedAccessToken accessToken) {
        DefaultAccessTokenDetailsBuilder builder = builder().tokenValue(accessToken.getTokenId().getValue())
                .clientId(accessToken.getClient().getValue())
                .expiration(accessToken.getExpiration())
                .expiresIn(accessToken.expiresIn())
                .isExpired(accessToken.isExpired())
                .tokenType(TOKEN_TYPE);
        builder.scopes(Optional.ofNullable(accessToken.getScopes()).orElse(Collections.emptySet()).stream().map(OAuth2ScopeId::getValue).collect(Collectors.toSet()));
        builder.username(Optional.ofNullable(accessToken.getUsername()).map(PrincipalUsername::getValue).orElse(null));
        builder.refreshToken(Optional.ofNullable(accessToken.getRefreshToken()).map(DefaultRefreshTokenDetails::of).orElse(null));
        builder.additionalInformation(Optional.ofNullable(accessToken.getAdditionalInformation()).orElse(null));
        return builder.build();
    }
}
