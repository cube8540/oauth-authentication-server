package cube8540.oauth.authentication.credentials.authority.application;

import cube8540.oauth.authentication.credentials.authority.AuthorityDetails;
import cube8540.oauth.authentication.credentials.authority.domain.Authority;
import lombok.Value;

@Value
public class DefaultAuthorityDetails implements AuthorityDetails {

    private String code;

    private String description;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String description() {
        return description;
    }

    public static DefaultAuthorityDetails of(Authority authority) {
        return new DefaultAuthorityDetails(authority.getCode().getValue(), authority.getDescription());
    }
}
