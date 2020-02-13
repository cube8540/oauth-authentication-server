package cube8540.oauth.authentication.users.endpoint;

import cube8540.oauth.authentication.message.ResponseMessage;
import cube8540.oauth.authentication.message.SuccessResponseMessage;
import cube8540.oauth.authentication.users.application.UserManagementService;
import cube8540.oauth.authentication.users.application.UserProfile;
import cube8540.oauth.authentication.users.application.UserRegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("유저 관리 API 엔드 포인트 테스트")
class UserManagementAPIEndpointTest {

    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "Password1234!@#$";

    private UserManagementService service;
    private UserManagementAPIEndpoint endpoint;

    @BeforeEach
    void setup() {
        this.service = mock(UserManagementService.class);
        this.endpoint = new UserManagementAPIEndpoint(service);
    }

    @Nested
    @DisplayName("이메일 카운팅")
    class CountingEmail {
        private long randomCount;

        @BeforeEach
        void setup() {
            this.randomCount = (long) (Math.random() * 100);

            when(service.countUser(EMAIL)).thenReturn(randomCount);
        }

        @Test
        @DisplayName("요청 받은 이메일을 카운팅 해야 한다.")
        void shouldCountingRequestingEmail() {
            endpoint.countAccountEmail(EMAIL);

            verify(service, times(1)).countUser(EMAIL);
        }

        @Test
        @DisplayName("HTTP 상태 코드는 200 이어야 한다.")
        void shouldHttpStatusIs200() {
            ResponseEntity<ResponseMessage> response = endpoint.countAccountEmail(EMAIL);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("응답 바디에는 이메일의 갯수를 포함해야 한다.")
        void shouldResponseBodyContainsEmailCount() {
            ResponseEntity<ResponseMessage> response = endpoint.countAccountEmail(EMAIL);

            assertNotNull(response.getBody());
            assertEquals(randomCount, ((SuccessResponseMessage<?>) response.getBody()).getData());
        }
    }

    @Nested
    @DisplayName("유저 등록")
    class RegisterUser {
        private UserRegisterRequest registerRequest;
        private UserProfile userProfile;

        @BeforeEach
        void setup() {
            this.registerRequest = new UserRegisterRequest(EMAIL, PASSWORD);
            this.userProfile = new UserProfile(EMAIL, LocalDateTime.now());

            when(service.registerUser(registerRequest)).thenReturn(userProfile);
        }

        @Test
        @DisplayName("요청 받은 정보를 등록해야 한다.")
        void shouldRegisterRequestingInformation() {
            ArgumentCaptor<UserRegisterRequest> requestCaptor = ArgumentCaptor.forClass(UserRegisterRequest.class);

            endpoint.registerUserAccounts(registerRequest);
            verify(service, times(1)).registerUser(requestCaptor.capture());
            assertEquals(registerRequest, requestCaptor.getValue());
        }

        @Test
        @DisplayName("HTTP 상태 코드는 CREATED어야 한다.")
        void shouldHttpStatusCodeIsCreated() {
            ResponseEntity<ResponseMessage> response = endpoint.registerUserAccounts(registerRequest);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("응답 바디에는 등록된 유저의 정보가 포함되어 있어야 한다.")
        void shouldResponseBodyContainsRegisterUserProfile() {
            ResponseEntity<ResponseMessage> response = endpoint.registerUserAccounts(registerRequest);

            assertNotNull(response.getBody());
            assertEquals(userProfile, ((SuccessResponseMessage<?>) response.getBody()).getData());
        }
    }
}