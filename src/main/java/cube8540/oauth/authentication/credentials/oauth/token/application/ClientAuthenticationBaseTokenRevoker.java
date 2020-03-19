package cube8540.oauth.authentication.credentials.oauth.token.application;

import cube8540.oauth.authentication.credentials.oauth.error.InvalidClientException;
import cube8540.oauth.authentication.credentials.oauth.security.OAuth2AccessTokenDetails;
import cube8540.oauth.authentication.credentials.oauth.security.OAuth2TokenRevoker;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenNotFoundException;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenRepository;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AuthorizedAccessToken;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2TokenId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ClientAuthenticationBaseTokenRevoker implements OAuth2TokenRevoker {

    private final OAuth2AccessTokenRepository repository;

    @Autowired
    public ClientAuthenticationBaseTokenRevoker(OAuth2AccessTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public OAuth2AccessTokenDetails revoke(String tokenValue) {
        OAuth2AuthorizedAccessToken accessToken = repository.findById(new OAuth2TokenId(tokenValue))
                .orElseThrow(() -> new OAuth2AccessTokenNotFoundException(tokenValue));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.getName().equals(accessToken.getClient().getValue())) {
            throw InvalidClientException.invalidClient("client and access token client is different");
        }
        repository.delete(accessToken);
        return DefaultAccessTokenDetails.of(accessToken);
    }
}