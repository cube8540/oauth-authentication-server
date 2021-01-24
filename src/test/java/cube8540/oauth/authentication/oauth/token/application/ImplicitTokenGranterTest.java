package cube8540.oauth.authentication.oauth.token.application;

import cube8540.oauth.authentication.AuthenticationApplication;
import cube8540.oauth.authentication.security.AuthorityCode;
import cube8540.oauth.authentication.oauth.error.InvalidRequestException;
import cube8540.oauth.authentication.oauth.security.OAuth2ClientDetails;
import cube8540.oauth.authentication.oauth.security.OAuth2TokenRequest;
import cube8540.oauth.authentication.oauth.token.domain.OAuth2AccessTokenRepository;
import cube8540.oauth.authentication.oauth.token.domain.OAuth2AuthorizedAccessToken;
import cube8540.oauth.authentication.oauth.token.domain.OAuth2ComposeUniqueKeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

import java.time.Clock;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.ACCESS_TOKEN_ID;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.ACCESS_TOKEN_VALIDITY_SECONDS;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.CLIENT_ID;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.CLIENT_SCOPES;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.COMPOSE_UNIQUE_KEY;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.RAW_SCOPES;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.TOKEN_CREATED_DATETIME;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.USERNAME;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.makeClientDetails;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.makeComposeUniqueKeyGenerator;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.makeEmptyAccessTokenRepository;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.makeTokenIdGenerator;
import static cube8540.oauth.authentication.oauth.token.application.OAuth2TokenApplicationTestHelper.makeTokenRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("암묵적 동의를 통한 토큰 부여 테스트")
class ImplicitTokenGranterTest {

    @Test
    @DisplayName("요청한 유저 아이디가 null 일때 엑세스 코드 생성")
    void generateAccessTokenWhenRequestUsernameIsNull() {
        OAuth2ClientDetails clientDetails = makeClientDetails();
        OAuth2AccessTokenRepository repository = makeEmptyAccessTokenRepository();
        OAuth2TokenRequest request = makeTokenRequest();
        ImplicitTokenGranter granter = new ImplicitTokenGranter(makeTokenIdGenerator(ACCESS_TOKEN_ID), repository);

        when(request.getUsername()).thenReturn(null);

        OAuth2Error error = assertThrows(InvalidRequestException.class, () -> granter.createAccessToken(clientDetails, request)).getError();
        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, error.getErrorCode());
    }

    @Test
    @DisplayName("요청 스코프가 null 일때 엑세스 코드 생성")
    void generateAccessTokenWhenScopeOfRequestIsNull() {
        OAuth2ClientDetails clientDetails = makeClientDetails();
        OAuth2AccessTokenRepository repository = makeEmptyAccessTokenRepository();
        OAuth2TokenRequest request = makeTokenRequest();
        OAuth2ComposeUniqueKeyGenerator composeUniqueKeyGenerator = makeComposeUniqueKeyGenerator();
        ImplicitTokenGranter granter = new ImplicitTokenGranter(makeTokenIdGenerator(ACCESS_TOKEN_ID), repository);

        Clock clock = Clock.fixed(TOKEN_CREATED_DATETIME.toInstant(AuthenticationApplication.DEFAULT_ZONE_OFFSET), AuthenticationApplication.DEFAULT_TIME_ZONE.toZoneId());
        AbstractOAuth2TokenGranter.setClock(clock);
        when(request.getScopes()).thenReturn(null);
        granter.setComposeUniqueKeyGenerator(composeUniqueKeyGenerator);

        OAuth2AuthorizedAccessToken accessToken = granter.createAccessToken(clientDetails, request);
        assertEquals(CLIENT_SCOPES, accessToken.getScopes());
        assertAccessToken(accessToken);
    }

    @Test
    @DisplayName("요청 스코프가 비어 있을때 엑세스 코드 생성")
    void generateAccessTokenWhenScopeOfRequestIsEmpty() {
        OAuth2ClientDetails clientDetails = makeClientDetails();
        OAuth2AccessTokenRepository repository = makeEmptyAccessTokenRepository();
        OAuth2TokenRequest request = makeTokenRequest();
        OAuth2ComposeUniqueKeyGenerator composeUniqueKeyGenerator = makeComposeUniqueKeyGenerator();
        ImplicitTokenGranter granter = new ImplicitTokenGranter(makeTokenIdGenerator(ACCESS_TOKEN_ID), repository);

        Clock clock = Clock.fixed(TOKEN_CREATED_DATETIME.toInstant(AuthenticationApplication.DEFAULT_ZONE_OFFSET), AuthenticationApplication.DEFAULT_TIME_ZONE.toZoneId());
        AbstractOAuth2TokenGranter.setClock(clock);
        when(request.getScopes()).thenReturn(Collections.emptySet());
        granter.setComposeUniqueKeyGenerator(composeUniqueKeyGenerator);

        OAuth2AuthorizedAccessToken accessToken = granter.createAccessToken(clientDetails, request);
        assertEquals(CLIENT_SCOPES, accessToken.getScopes());
        assertAccessToken(accessToken);
    }

    @Test
    @DisplayName("요청 스코프가 null이 아닐 때 엑세스 토큰 생성")
    void generateAccessTokenWhenScopeOfRequestIsNotNull() {
        OAuth2ClientDetails clientDetails = makeClientDetails();
        OAuth2AccessTokenRepository repository = makeEmptyAccessTokenRepository();
        OAuth2TokenRequest request = makeTokenRequest();
        OAuth2ComposeUniqueKeyGenerator composeUniqueKeyGenerator = makeComposeUniqueKeyGenerator();
        ImplicitTokenGranter granter = new ImplicitTokenGranter(makeTokenIdGenerator(ACCESS_TOKEN_ID), repository);

        Clock clock = Clock.fixed(TOKEN_CREATED_DATETIME.toInstant(AuthenticationApplication.DEFAULT_ZONE_OFFSET), AuthenticationApplication.DEFAULT_TIME_ZONE.toZoneId());
        AbstractOAuth2TokenGranter.setClock(clock);
        when(request.getScopes()).thenReturn(RAW_SCOPES);
        granter.setComposeUniqueKeyGenerator(composeUniqueKeyGenerator);

        OAuth2AuthorizedAccessToken accessToken = granter.createAccessToken(clientDetails, request);
        Set<AuthorityCode> exceptedScopes = RAW_SCOPES.stream().map(AuthorityCode::new).collect(Collectors.toSet());
        assertEquals(exceptedScopes, accessToken.getScopes());
        assertAccessToken(accessToken);
    }

    private void assertAccessToken(OAuth2AuthorizedAccessToken accessToken) {
        assertEquals(ACCESS_TOKEN_ID, accessToken.getTokenId());
        assertEquals(USERNAME, accessToken.getUsername());
        assertEquals(CLIENT_ID, accessToken.getClient());
        assertEquals(AuthorizationGrantType.IMPLICIT, accessToken.getTokenGrantType());
        assertEquals(TOKEN_CREATED_DATETIME, accessToken.getIssuedAt());
        assertEquals(TOKEN_CREATED_DATETIME.plusSeconds(ACCESS_TOKEN_VALIDITY_SECONDS), accessToken.getExpiration());
        assertEquals(COMPOSE_UNIQUE_KEY, accessToken.getComposeUniqueKey());
    }
}