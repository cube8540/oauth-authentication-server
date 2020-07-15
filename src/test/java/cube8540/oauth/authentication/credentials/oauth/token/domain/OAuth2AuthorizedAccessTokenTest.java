package cube8540.oauth.authentication.credentials.oauth.token.domain;

import cube8540.oauth.authentication.AuthenticationApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.CLIENT_ID;
import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.EXPIRATION_DATETIME;
import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.GRANT_TYPE;
import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.REFRESH_EXPIRATION_DATETIME;
import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.REFRESH_TOKEN_ID;
import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.USERNAME;
import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.makeAccessTokenIdGenerator;
import static cube8540.oauth.authentication.credentials.oauth.token.domain.OAuth2AccessTokenTestHelper.makeRefreshTokenIdGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OAuth2 액세스 토큰 도메인 테스트")
class OAuth2AuthorizedAccessTokenTest {

    @Test
    @DisplayName("현재 시간이 만료일을 초과 했을시")
    void whenCurrentTimeExceedsExpirationDate() {
        OAuth2AuthorizedAccessToken token = OAuth2AuthorizedAccessToken.builder(makeAccessTokenIdGenerator())
                .username(USERNAME).client(CLIENT_ID).tokenGrantType(GRANT_TYPE).expiration(EXPIRATION_DATETIME).build();

        Clock clock = Clock.fixed(EXPIRATION_DATETIME.plusNanos(1).toInstant(AuthenticationApplication.DEFAULT_ZONE_OFFSET), AuthenticationApplication.DEFAULT_TIME_ZONE.toZoneId());
        OAuth2AuthorizedAccessToken.setClock(clock);

        assertTrue(token.isExpired());
        assertEquals(0, token.expiresIn());
    }

    @Test
    @DisplayName("현재 시간이 만료일을 초과 하지 않을시")
    void whenCurrentTimeNotExceedsExpirationDate() {
        OAuth2AuthorizedAccessToken token = OAuth2AuthorizedAccessToken.builder(makeAccessTokenIdGenerator())
                .username(USERNAME).client(CLIENT_ID).tokenGrantType(GRANT_TYPE).expiration(EXPIRATION_DATETIME).build();

        long expiresIn = 10;
        Clock clock = Clock.fixed(EXPIRATION_DATETIME.minusSeconds(expiresIn).toInstant(AuthenticationApplication.DEFAULT_ZONE_OFFSET), AuthenticationApplication.DEFAULT_TIME_ZONE.toZoneId());
        OAuth2AuthorizedAccessToken.setClock(clock);

        assertFalse(token.isExpired());
        assertEquals(expiresIn, token.expiresIn());
    }

    @Test
    @DisplayName("토큰 추가 정보 저장")
    void tokenAdditionalInformationStorage() {
        OAuth2AuthorizedAccessToken token = OAuth2AuthorizedAccessToken.builder(makeAccessTokenIdGenerator())
                .username(USERNAME).client(CLIENT_ID).tokenGrantType(GRANT_TYPE).expiration(EXPIRATION_DATETIME).build();

        token.putAdditionalInformation("KEY-0", "VALUE-0");
        token.putAdditionalInformation("KEY-1", "VALUE-1");

        assertTrue(token.getAdditionalInformation().containsKey("KEY-0"));
        assertTrue(token.getAdditionalInformation().containsKey("KEY-1"));
        assertEquals("VALUE-0", token.getAdditionalInformation().get("KEY-0"));
        assertEquals("VALUE-1", token.getAdditionalInformation().get("KEY-1"));
    }

    @Test
    @DisplayName("리플래시 토큰 생성")
    void generateRefreshToken() {
        OAuth2AuthorizedAccessToken token = OAuth2AuthorizedAccessToken.builder(makeAccessTokenIdGenerator())
                .username(USERNAME).client(CLIENT_ID).tokenGrantType(GRANT_TYPE).expiration(EXPIRATION_DATETIME).build();
        OAuth2TokenIdGenerator refreshTokenIdGenerator = makeRefreshTokenIdGenerator();

        token.generateRefreshToken(refreshTokenIdGenerator, REFRESH_EXPIRATION_DATETIME);
        OAuth2AuthorizedRefreshToken refreshToken = token.getRefreshToken();
        assertEquals(REFRESH_TOKEN_ID, refreshToken.getTokenId());
        assertEquals(REFRESH_EXPIRATION_DATETIME, refreshToken.getExpiration());
    }
}