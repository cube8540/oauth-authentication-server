package cube8540.oauth.authentication.credentials.authority.application;

import cube8540.oauth.authentication.credentials.authority.AuthorityDetails;
import cube8540.oauth.authentication.credentials.authority.domain.Authority;
import cube8540.oauth.authentication.credentials.authority.domain.AuthorityCode;
import cube8540.oauth.authentication.credentials.authority.domain.AuthorityRepository;
import cube8540.oauth.authentication.credentials.authority.error.AuthorityNotFoundException;
import cube8540.oauth.authentication.credentials.authority.error.AuthorityRegisterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class DefaultAuthorityManagementService implements AuthorityManagementService {

    private final AuthorityRepository repository;

    @Autowired
    public DefaultAuthorityManagementService(AuthorityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Long countAuthority(String code) {
        return repository.countByCode(new AuthorityCode(code));
    }

    @Override
    public AuthorityDetails getAuthority(String code) {
        return DefaultAuthorityDetails.of(getRegisteredAuthority(code));
    }

    @Override
    public Collection<AuthorityDetails> getAuthorities() {
        return repository.findAll().stream().map(DefaultAuthorityDetails::of).collect(Collectors.toList());
    }

    @Override
    public AuthorityDetails registerAuthority(AuthorityRegisterRequest registerRequest) {
        if (countAuthority(registerRequest.getCode()) > 0){
            throw AuthorityRegisterException.existsIdentifier(registerRequest.getCode() + " is already exists");
        }

        Authority authority = new Authority(registerRequest.getCode(), registerRequest.getDescription());
        if (registerRequest.isBasic()) {
            authority.settingBasicAuthority();
        }
        return DefaultAuthorityDetails.of(repository.save(authority));
    }

    @Override
    public AuthorityDetails modifyAuthority(String code, AuthorityModifyRequest modifyRequest) {
        Authority authority = getRegisteredAuthority(code);
        authority.setDescription(modifyRequest.getDescription());
        if (modifyRequest.isBasic()) {
            authority.settingBasicAuthority();
        } else {
            authority.settingNotBasicAuthority();
        }
        return DefaultAuthorityDetails.of(repository.save(authority));
    }

    @Override
    public AuthorityDetails removeAuthority(String code) {
        Authority authority = getRegisteredAuthority(code);
        repository.delete(authority);
        return DefaultAuthorityDetails.of(authority);
    }

    private Authority getRegisteredAuthority(String code) {
        return repository.findById(new AuthorityCode(code))
                .orElseThrow(() -> AuthorityNotFoundException.instance(code + " is not found"));
    }
}
