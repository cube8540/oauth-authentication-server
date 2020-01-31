package cube8540.oauth.authentication.credentials.oauth.client.domain;

import cube8540.oauth.authentication.credentials.oauth.converter.AuthorizationGrantTypeConverter;
import cube8540.oauth.authentication.credentials.oauth.converter.RedirectUriConverter;
import cube8540.oauth.authentication.credentials.oauth.scope.domain.OAuth2ScopeId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Target;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import javax.persistence.AttributeOverride;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "oauth2_clients")
public class OAuth2Client extends AbstractAggregateRoot<OAuth2Client> {

    public static final Duration DEFAULT_ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(10);
    public static final Duration DEFAULT_REFRESH_TOKEN_VALIDITY = Duration.ofHours(2);

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "client_id", length = 32))
    private OAuth2ClientId clientId;

    @Embedded
    @Target(OAuth2ClientDefaultSecret.class)
    @AttributeOverride(name = "secret", column = @Column(name = "client_secret", length = 64, nullable = false))
    private OAuth2ClientSecret secret;

    @Column(name = "client_name", length = 32, nullable = false)
    private String clientName;

    @ElementCollection
    @Column(name = "redirect_uri", length = 128, nullable = false)
    @CollectionTable(name = "oauth2_client_redirect_uri", joinColumns = @JoinColumn(name = "client_id", nullable = false))
    @Convert(converter = RedirectUriConverter.class)
    private Set<URI> redirectURI;

    @ElementCollection
    @Column(name = "grant_type", length = 32, nullable = false)
    @CollectionTable(name = "oauth2_client_grant_type", joinColumns = @JoinColumn(name = "client_id", nullable = false))
    @Convert(converter = AuthorizationGrantTypeConverter.class)
    private Set<AuthorizationGrantType> grantType;

    @ElementCollection
    @CollectionTable(name = "oauth2_client_scope", joinColumns = @JoinColumn(name = "client_id", nullable = false))
    @AttributeOverride(name = "value", column = @Column(name = "scope_id", length = 32, nullable = false))
    private Set<OAuth2ScopeId> scope;

    @Column(name = "access_token_validity", nullable = false)
    private Duration accessTokenValidity;

    @Column(name = "refresh_token_validity", nullable = false)
    private Duration refreshTokenValidity;

    public OAuth2Client(String clientId, String secret, String clientName, OAuth2ClientSecretEncoder encoder) {
        this.clientId = new OAuth2ClientId(clientId);
        this.secret = new OAuth2ClientDefaultSecret(secret);
        this.clientName = clientName;
        this.accessTokenValidity = DEFAULT_ACCESS_TOKEN_VALIDITY;
        this.refreshTokenValidity = DEFAULT_REFRESH_TOKEN_VALIDITY;

        this.secret = this.secret.encrypted(encoder);
    }

    public void addRedirectURI(URI uri) {
        if (this.redirectURI == null) {
            this.redirectURI = new HashSet<>();
        }
        this.redirectURI.add(uri);
    }

    public void removeRedirectURI(URI redirectURI) {
        Optional.ofNullable(this.redirectURI)
                .ifPresent(uris -> uris.remove(redirectURI));
    }

    public void addGrantType(AuthorizationGrantType grantType) {
        if (this.grantType == null) {
            this.grantType = new HashSet<>();
        }
        this.grantType.add(grantType);
    }

    public void removeGrantType(AuthorizationGrantType grantType) {
        Optional.ofNullable(this.grantType)
                .ifPresent(types -> types.remove(grantType));
    }

    public void addScope(OAuth2ScopeId scope) {
        if (this.scope == null) {
            this.scope = new HashSet<>();
        }
        this.scope.add(scope);
    }

    public void removeScope(OAuth2ScopeId scope) {
        Optional.ofNullable(this.scope)
                .ifPresent(scopes -> scopes.remove(scope));
    }
}
