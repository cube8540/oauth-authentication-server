package cube8540.oauth.authentication.credentials.oauth.client.infra.rule;

import cube8540.oauth.authentication.credentials.AuthorityCode;
import cube8540.oauth.authentication.credentials.AuthorityDetails;
import cube8540.oauth.authentication.credentials.oauth.client.domain.OAuth2Client;
import cube8540.oauth.authentication.credentials.oauth.scope.application.OAuth2ScopeManagementService;
import cube8540.validator.core.ValidationError;
import cube8540.validator.core.ValidationRule;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

public class ClientCanGrantedScopeValidationRule implements ValidationRule<OAuth2Client> {

    private static final String DEFAULT_PROPERTY = "scope";
    private static final String DEFAULT_MESSAGE = "부여할 수 없는 스코프 입니다.";

    private String property;
    private String message;

    @Setter
    private OAuth2ScopeManagementService scopeDetailsService;

    public ClientCanGrantedScopeValidationRule() {
        this(DEFAULT_PROPERTY, DEFAULT_MESSAGE);
    }

    public ClientCanGrantedScopeValidationRule(String property, String message) {
        this.property = property;
        this.message = message;
    }

    @Override
    public ValidationError error() {
        return new ValidationError(property, message);
    }

    @Override
    public boolean isValid(OAuth2Client target) {
        if (scopeDetailsService == null) {
            return false;
        }
        if (target.getScopes() == null || target.getScopes().isEmpty()) {
            return false;
        }
        Set<AuthorityCode> accessibleScopes = scopeDetailsService.loadAllScopes()
                .stream().map(AuthorityDetails::getCode)
                .map(AuthorityCode::new).collect(Collectors.toSet());
        return accessibleScopes.containsAll(target.getScopes());
    }
}
