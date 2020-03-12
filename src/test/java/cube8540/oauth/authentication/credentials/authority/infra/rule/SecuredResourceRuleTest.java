package cube8540.oauth.authentication.credentials.authority.infra.rule;

import cube8540.oauth.authentication.credentials.authority.domain.SecuredResource;
import cube8540.validator.core.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("자원 유효성 검사 테스트")
class SecuredResourceRuleTest {

    private static final String RAW_RESOURCE = "/resource/**";
    private static final URI RESOURCE = URI.create(RAW_RESOURCE);

    private SecuredResourceRule rule;

    @BeforeEach
    void setup() {
        this.rule = new SecuredResourceRule();
    }

    @Test
    @DisplayName("에러 메시지 확인")
    void checkErrorMessage() {
        ValidationError excepted = new ValidationError(SecuredResourceRule.DEFAULT_PROPERTY, SecuredResourceRule.DEFAULT_MESSAGE);

        assertEquals(excepted, rule.error());
    }

    @Test
    @DisplayName("자원이 null 일시 유효성 검사 결과는 false 가 반환되어야 한다.")
    void ifResourceIsNullValidationResultIsFalse() {
        SecuredResource resource = mock(SecuredResource.class);

        when(resource.getResource()).thenReturn(null);

        assertFalse(rule.isValid(resource));
    }

    @Test
    @DisplayName("자원이 null 이 아닐시 유효성 검사 결과는 true 가 반환되어야 한다.")
    void ifResourceIsNotNullValidationResultIsTrue() {
        SecuredResource resource = mock(SecuredResource.class);

        when(resource.getResource()).thenReturn(RESOURCE);

        assertTrue(rule.isValid(resource));
    }

}