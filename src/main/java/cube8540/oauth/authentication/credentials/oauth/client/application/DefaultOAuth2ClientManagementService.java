package cube8540.oauth.authentication.credentials.oauth.client.application;

import cube8540.oauth.authentication.credentials.oauth.OAuth2Utils;
import cube8540.oauth.authentication.credentials.oauth.client.OAuth2ClientDetails;
import cube8540.oauth.authentication.credentials.oauth.client.domain.ClientOwnerNotMatchedException;
import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2Client;
import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2ClientAlreadyExistsException;
import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2ClientId;
import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2ClientNotFoundException;
import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2ClientRepository;
import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2ClientValidatePolicy;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeId;
import cube8540.oauth.authentication.users.domain.UserEmail;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Optional;

public class DefaultOAuth2ClientManagementService extends DefaultOAuth2ClientDetailsService implements OAuth2ClientManagementService {

    @Setter
    private OAuth2ClientValidatePolicy validatePolicy;

    @Setter
    private PasswordEncoder passwordEncoder;

    public DefaultOAuth2ClientManagementService(OAuth2ClientRepository repository) {
        super(repository);
    }

    @Override
    public Long countClient(String clientId) {
        return getRepository().countByClientId(new OAuth2ClientId(clientId));
    }

    @Override
    public Page<OAuth2ClientDetails> loadClientDetails(Pageable pageable) {
        UserEmail owner = new UserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        return getRepository().findByOwner(owner, pageable).map(DefaultOAuth2ClientDetails::new);
    }

    @Override
    @Transactional
    public OAuth2ClientDetails registerNewClient(OAuth2ClientRegisterRequest registerRequest) {
        if (getRepository().countByClientId(new OAuth2ClientId(registerRequest.getClientId())) > 0) {
            throw new OAuth2ClientAlreadyExistsException(registerRequest.getClientId() + " is exists");
        }
        OAuth2Client client = new OAuth2Client(registerRequest.getClientId(), registerRequest.getSecret());

        client.setClientName(registerRequest.getClientName());
        client.setOwner(new UserEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
        Optional.ofNullable(registerRequest.getGrantTypes())
                .ifPresent(grantType -> grantType.forEach(grant -> client.addGrantType(OAuth2Utils.extractGrantType(grant))));
        Optional.ofNullable(registerRequest.getScopes())
                .ifPresent(scope -> scope.forEach(s -> client.addScope(new OAuth2ScopeId(s))));
        Optional.ofNullable(registerRequest.getRedirectUris())
                .ifPresent(redirectUri -> redirectUri.forEach(uri -> client.addRedirectURI(URI.create(uri))));

        client.validate(validatePolicy);
        client.encrypted(passwordEncoder);
        return new DefaultOAuth2ClientDetails(getRepository().save(client));
    }

    @Override
    @Transactional
    public OAuth2ClientDetails modifyClient(String clientId, OAuth2ClientModifyRequest modifyRequest) {
        OAuth2Client client = getRepository().findByClientId(new OAuth2ClientId(clientId))
                .orElseThrow(() -> new OAuth2ClientNotFoundException(clientId + " is not found"));
        UserEmail authenticated = new UserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!client.getOwner().equals(authenticated)) {
            throw new ClientOwnerNotMatchedException("owner and authenticated user not matched");
        }
        client.setClientName(modifyRequest.getClientName());
        Optional.ofNullable(modifyRequest.getRemoveRedirectUris())
                .ifPresent(redirectUri -> redirectUri.forEach(uri -> client.removeRedirectURI(URI.create(uri))));
        Optional.ofNullable(modifyRequest.getNewRedirectUris())
                .ifPresent(redirectUri -> redirectUri.forEach(uri -> client.addRedirectURI(URI.create(uri))));
        Optional.ofNullable(modifyRequest.getRemoveGrantTypes())
                .ifPresent(grantType -> grantType.forEach(grant -> client.removeGrantType(OAuth2Utils.extractGrantType(grant))));
        Optional.ofNullable(modifyRequest.getNewGrantTypes())
                .ifPresent(grantType -> grantType.forEach(grant -> client.addGrantType(OAuth2Utils.extractGrantType(grant))));
        Optional.ofNullable(modifyRequest.getRemoveScopes())
                .ifPresent(scope -> scope.forEach(s -> client.removeScope(new OAuth2ScopeId(s))));
        Optional.ofNullable(modifyRequest.getNewScopes())
                .ifPresent(scope -> scope.forEach(s -> client.addScope(new OAuth2ScopeId(s))));

        client.validate(validatePolicy);
        return new DefaultOAuth2ClientDetails(getRepository().save(client));
    }

    @Override
    @Transactional
    public OAuth2ClientDetails changeSecret(String clientId, OAuth2ChangeSecretRequest changeRequest) {
        OAuth2Client client = getRepository().findByClientId(new OAuth2ClientId(clientId))
                .orElseThrow(() -> new OAuth2ClientNotFoundException(clientId + " is not found"));

        UserEmail authenticated = new UserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!client.getOwner().equals(authenticated)) {
            throw new ClientOwnerNotMatchedException("owner and authenticated user not matched");
        }

        client.changeSecret(changeRequest.getExistsSecret(), changeRequest.getNewSecret(), passwordEncoder);
        client.validate(validatePolicy);
        client.encrypted(passwordEncoder);

        return new DefaultOAuth2ClientDetails(getRepository().save(client));
    }

    @Override
    @Transactional
    public OAuth2ClientDetails removeClient(String clientId) {
        OAuth2Client client = getRepository().findByClientId(new OAuth2ClientId(clientId))
                .orElseThrow(() -> new OAuth2ClientNotFoundException(clientId + " is not found"));

        UserEmail authenticated = new UserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!client.getOwner().equals(authenticated)) {
            throw new ClientOwnerNotMatchedException("owner and authenticated user not matched");
        }

        getRepository().delete(client);
        return new DefaultOAuth2ClientDetails(client);
    }
}