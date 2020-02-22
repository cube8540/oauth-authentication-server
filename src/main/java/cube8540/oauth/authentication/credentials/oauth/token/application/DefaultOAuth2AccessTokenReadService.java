package cube8540.oauth.authentication.credentials.oauth.token.application;

import cube8540.oauth.authentication.credentials.oauth.token.OAuth2AccessTokenDetails;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenExpiredException;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenNotFoundException;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenRepository;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AuthorizedAccessToken;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2TokenId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class DefaultOAuth2AccessTokenReadService implements OAuth2AccessTokenReadService {

    private final OAuth2AccessTokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;

    @Autowired
    public DefaultOAuth2AccessTokenReadService(OAuth2AccessTokenRepository tokenRepository,
                                               @Qualifier("defaultUserService") UserDetailsService userDetailsService) {
        this.tokenRepository = tokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public OAuth2AccessTokenDetails readAccessToken(String tokenValue) {
        OAuth2AuthorizedAccessToken accessToken = tokenRepository.findById(new OAuth2TokenId(tokenValue))
                .orElseThrow(() -> new OAuth2AccessTokenNotFoundException("[" + tokenValue + "] token is not found"));

        if (accessToken.isExpired()) {
            throw new OAuth2AccessTokenExpiredException("[" + tokenValue + "] is expired");
        }
        return DefaultAccessTokenDetails.of(accessToken);
    }

    @Override
    public UserDetails readAccessTokenUser(String tokenValue) {
        OAuth2AuthorizedAccessToken accessToken = tokenRepository.findById(new OAuth2TokenId(tokenValue))
                .orElseThrow(() -> new OAuth2AccessTokenNotFoundException("[" + tokenValue + "] token is not found"));

        UserDetails user = userDetailsService.loadUserByUsername(accessToken.getEmail().getValue());
        if (user instanceof CredentialsContainer) {
            ((CredentialsContainer) user).eraseCredentials();
        }
        return user;
    }
}
