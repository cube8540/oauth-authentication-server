package cube8540.oauth.authentication.credentials.oauth.token.application;

import cube8540.oauth.authentication.credentials.oauth.AuthorizationRequest;
import cube8540.oauth.authentication.credentials.oauth.token.domain.AuthorizationCode;
import cube8540.oauth.authentication.credentials.oauth.token.domain.AuthorizationCodeGenerator;
import cube8540.oauth.authentication.credentials.oauth.token.domain.AuthorizationCodeRepository;
import cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AuthorizationCode;
import cube8540.oauth.authentication.credentials.oauth.token.infra.DefaultAuthorizationCodeGenerator;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompositionAuthorizationCodeService implements OAuth2AuthorizationCodeConsumer, OAuth2AuthorizationCodeGenerator {

    private final AuthorizationCodeRepository codeRepository;

    @Setter
    private AuthorizationCodeGenerator codeGenerator = new DefaultAuthorizationCodeGenerator();

    @Autowired
    public CompositionAuthorizationCodeService(AuthorizationCodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    @Override
    public Optional<OAuth2AuthorizationCode> consume(AuthorizationCode code) {
        Optional<OAuth2AuthorizationCode> authorizationCode = codeRepository.findById(code);
        authorizationCode.ifPresent(codeRepository::delete);

        return authorizationCode;
    }

    @Override
    public AuthorizationCode generateNewAuthorizationCode(AuthorizationRequest request) {
        OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(codeGenerator);

        authorizationCode.setAuthorizationRequest(request);
        codeRepository.save(authorizationCode);

        return authorizationCode.getCode();
    }
}