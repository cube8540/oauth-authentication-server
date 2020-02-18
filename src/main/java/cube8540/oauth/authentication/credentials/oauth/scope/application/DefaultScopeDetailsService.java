package cube8540.oauth.authentication.credentials.oauth.scope.application;

import cube8540.oauth.authentication.credentials.authority.domain.AuthorityCode;
import cube8540.oauth.authentication.credentials.oauth.scope.OAuth2AccessibleScopeDetailsService;
import cube8540.oauth.authentication.credentials.oauth.scope.OAuth2ScopeDetails;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2Scope;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeAlreadyExistsException;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeId;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeNotFoundException;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeRepository;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeValidationPolicy;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultScopeDetailsService implements OAuth2ScopeManagementService, OAuth2AccessibleScopeDetailsService {

    private final OAuth2ScopeRepository repository;

    @Setter
    private OAuth2ScopeValidationPolicy validationPolicy;

    public DefaultScopeDetailsService(OAuth2ScopeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Collection<OAuth2ScopeDetails> loadScopeDetailsByScopeIds(Collection<String> scopeIds) {
        List<OAuth2ScopeId> scopeIn = scopeIds.stream()
                .map(OAuth2ScopeId::new).collect(Collectors.toList());
        return repository.findByIdIn(scopeIn).stream()
                .map(DefaultOAuth2ScopeDetails::new).collect(Collectors.toList());
    }

    @Override
    public Collection<OAuth2ScopeDetails> readAccessibleScopes(Authentication authentication) {
        return repository.findAll().stream().filter(scope -> scope.isAccessible(authentication))
                .map(DefaultOAuth2ScopeDetails::new).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OAuth2ScopeDetails registerNewScope(OAuth2ScopeRegisterRequest registerRequest) {
        if (repository.countById(new OAuth2ScopeId(registerRequest.getScopeId())) > 0) {
            throw new OAuth2ScopeAlreadyExistsException(registerRequest.getScopeId() + " is exists");
        }

        OAuth2Scope scope = new OAuth2Scope(registerRequest.getScopeId(), registerRequest.getDescription());
        Optional.ofNullable(registerRequest.getAccessibleAuthority())
                .ifPresent(authorities -> authorities.forEach(authority -> scope.addAccessibleAuthority(new AuthorityCode(authority))));

        scope.validate(validationPolicy);
        return new DefaultOAuth2ScopeDetails(repository.save(scope));
    }

    @Override
    @Transactional
    public OAuth2ScopeDetails modifyScope(String scopeId, OAuth2ScopeModifyRequest modifyRequest) {
        OAuth2Scope scope = repository.findById(new OAuth2ScopeId(scopeId))
                .orElseThrow(() -> new OAuth2ScopeNotFoundException(scopeId + " is not found"));

        scope.setDescription(modifyRequest.getDescription());
        Optional.ofNullable(modifyRequest.getRemoveAccessibleAuthority())
                .ifPresent(authorities -> authorities.forEach(auth -> scope.removeAccessibleAuthority(new AuthorityCode(auth))));
        Optional.ofNullable(modifyRequest.getNewAccessibleAuthority())
                .ifPresent(authorities -> authorities.forEach(auth -> scope.addAccessibleAuthority(new AuthorityCode(auth))));

        scope.validate(validationPolicy);
        return new DefaultOAuth2ScopeDetails(repository.save(scope));
    }

    @Override
    @Transactional
    public OAuth2ScopeDetails removeScope(String scopeId) {
        OAuth2Scope scope = repository.findById(new OAuth2ScopeId(scopeId))
                .orElseThrow(() -> new OAuth2ScopeNotFoundException(scopeId + " is not found"));

        repository.delete(scope);

        return new DefaultOAuth2ScopeDetails(scope);
    }
}