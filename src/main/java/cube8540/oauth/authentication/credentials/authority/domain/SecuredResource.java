package cube8540.oauth.authentication.credentials.authority.domain;

import cube8540.oauth.authentication.credentials.authority.domain.converter.ResourceConverter;
import cube8540.oauth.authentication.credentials.authority.domain.exception.ResourceInvalidException;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeId;
import cube8540.validator.core.Validator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.AttributeOverride;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "secured_resource")
public class SecuredResource extends AbstractAggregateRoot<SecuredResource> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "resource_id", length = 32))
    private SecuredResourceId resourceId;

    @Convert(converter = ResourceConverter.class)
    @Column(name = "resource", length = 128, nullable = false)
    private URI resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 32, nullable = false)
    private ResourceMethod method;

    @ElementCollection
    @CollectionTable(name = "authority_accessible_resources", joinColumns = @JoinColumn(name = "resource_id", nullable = false))
    @AttributeOverride(name = "value", column = @Column(name = "authority", length = 32, nullable = false))
    private Set<OAuth2ScopeId> authorities;

    public SecuredResource(SecuredResourceId resourceId, URI resource, ResourceMethod method) {
        this.resourceId = resourceId;
        this.resource = resource;
        this.method = method;
    }

    public void changeResourceInfo(URI changeResource, ResourceMethod changeMethod) {
        this.resource = changeResource;
        this.method = changeMethod;

        registerSecuredResourceChangedEvent();
    }

    public void addAuthority(OAuth2ScopeId scope) {
        if (this.authorities == null) {
            this.authorities = new HashSet<>();
        }
        this.authorities.add(scope);

        registerSecuredResourceChangedEvent();
    }

    public void removeAuthority(OAuth2ScopeId scope) {
        Optional.ofNullable(authorities).ifPresent(auth -> auth.remove(scope));

        registerSecuredResourceChangedEvent();
    }

    public void validation(SecuredResourceValidationPolicy policy) {
        Validator.of(this).registerRule(policy.resourceIdRule())
                .registerRule(policy.resourceRule())
                .registerRule(policy.methodRule())
                .registerRule(policy.authoritiesRule())
                .getResult().hasErrorThrows(ResourceInvalidException::instance);
    }

    private void registerSecuredResourceChangedEvent() {
        SecuredResourceChangedEvent event = new SecuredResourceChangedEvent(resourceId);
        if (!domainEvents().contains(event)) {
            registerEvent(event);
        }
    }
}
