package nextstep.member.acceptance;

import static nextstep.member.acceptance.MemberSteps.*;
import static nextstep.member.acceptance.TokenSteps.*;
import static org.assertj.core.api.Assertions.assertThat;

import nextstep.subway.utils.AcceptanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class MemberAcceptanceTest extends AcceptanceTest {
    public static final String EMAIL = "email@email.com";
    public static final String PASSWORD = "password";
    public static final int AGE = 20;

    @DisplayName("회원가입을 한다.")
    @Test
    void createMember() {
        // when
        var response = 회원_생성_요청(EMAIL, PASSWORD, AGE);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @DisplayName("회원 정보를 조회한다.")
    @Test
    void getMember() {
        // given
        var createResponse = 회원_생성_요청(EMAIL, PASSWORD, AGE);

        // when
        var response = 회원_정보_조회_요청(createResponse);

        // then
        회원_정보_조회됨(response, EMAIL, AGE);

    }

    @DisplayName("회원 정보를 수정한다.")
    @Test
    void updateMember() {
        // given
        var createResponse = 회원_생성_요청(EMAIL, PASSWORD, AGE);

        // when
        var response = 회원_정보_수정_요청(createResponse, "new" + EMAIL, "new" + PASSWORD, AGE);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("회원 정보를 삭제한다.")
    @Test
    void deleteMember() {
        // given
        var createResponse = 회원_생성_요청(EMAIL, PASSWORD, AGE);

        // when
        var response = 회원_삭제_요청(createResponse);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    /**
     * Given: 회원가입을 한다.
     * And: 로그인을 요청해서 토큰을 발급한다.
     * When: 회원 정보를 조회한다.
     * Then: 성공(200 OK) 응답을 받는다.
     * And: 이메일을 검증한다.
     * And: 나이를 검증한다.
     */

    @DisplayName("내 정보를 조회한다.")
    @Test
    void getMyInfo() {
        // Given
        회원_생성_요청(EMAIL, PASSWORD, AGE);
        var tokenResponse = 로그인_요청(EMAIL, PASSWORD);

        // When
        var meResponse = 내_정보_조회_요청(tokenResponse);

        // Then
        회원_정보_조회됨(meResponse, EMAIL, AGE);
    }
}