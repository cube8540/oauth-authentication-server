package cube8540.oauth.authentication.credentials.oauth.security.provider;

import cube8540.oauth.authentication.credentials.oauth.security.OAuth2ClientDetails;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ClientCredentialsToken extends AbstractAuthenticationToken {

    private Object principal;
    private Object credentials;

    public ClientCredentialsToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(false);
    }

    protected ClientCredentialsToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        if (principal instanceof String) {
            return principal.toString();
        } else if (principal instanceof OAuth2ClientDetails) {
            return ((OAuth2ClientDetails) principal).getClientId();
        } else {
            return super.getName();
        }
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new IllegalArgumentException("this operation is not supported");
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}