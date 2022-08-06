package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static nextstep.subway.acceptance.LineSteps.지하철_노선에_지하철_구간_생성_요청;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static nextstep.subway.domain.PathType.DISTANCE;
import static nextstep.subway.domain.PathType.DURATION;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 경로 검색")
class PathAcceptanceTest extends AcceptanceTest {
    private Long 교대역;
    private Long 강남역;
    private Long 양재역;
    private Long 남부터미널역;
    private Long 이호선;
    private Long 신분당선;
    private Long 삼호선;

    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        교대역 = 지하철역_생성_요청(관리자, "교대역").jsonPath().getLong("id");
        강남역 = 지하철역_생성_요청(관리자, "강남역").jsonPath().getLong("id");
        양재역 = 지하철역_생성_요청(관리자, "양재역").jsonPath().getLong("id");
        남부터미널역 = 지하철역_생성_요청(관리자, "남부터미널역").jsonPath().getLong("id");

        이호선 = 지하철_노선_생성_요청("2호선", "green", 교대역, 강남역, 10, 5);
        신분당선 = 지하철_노선_생성_요청("신분당선", "red", 강남역, 양재역, 10, 1);
        삼호선 = 지하철_노선_생성_요청("3호선", "orange", 교대역, 남부터미널역, 2, 2);

        지하철_노선에_지하철_구간_생성_요청(관리자, 삼호선, createSectionCreateParams(남부터미널역, 양재역, 3));
    }

    @DisplayName("두 역의 최단 거리 경로를 조회한다.")
    @Test
    void findPathByDistance() {
        // when
        ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역);

        // then
        assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(교대역, 남부터미널역, 양재역);
    }

    /**
     * When 출발역에서 도착역까지의 최소 시간 기준으로 경로 조회를 요청
     * Then 최소 시간 기준 경로를 응답
     * And 총 거리와 소요 시간을 함께 응답함
     */
    @Test
    void 두_역의_최소_시간_경로를_조회한다() {
        // when
        var 최소_시간_경로_조회_응답 = 두_역의_최소_시간_경로_조회를_요청(교대역, 양재역);

        // then
        최소_시간_경로_조회_응답_확인(최소_시간_경로_조회_응답);

        // and
        총_거리와_소요_시간_응답_확인(최소_시간_경로_조회_응답);
    }

    /**
     * When 출발역에서 도착역까지의 최단 거리 경로 조회를 요청
     * Then 최단 거리 경로를 응답
     * And 총 거리와 소요 시간을 함께 응답함
     * And 지하철 이용 요금도 함께 응답함
     */
    @Test
    void 두_역의_최단_거리_경로와_요금을_조회한다() {
        // when
        var 최단_거리_경로_조회_응답 = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역);

        // then
        최단_거리_경로_조회_응답_확인(최단_거리_경로_조회_응답);

        // and
        총_거리와_소요_시간_응답_확인(최단_거리_경로_조회_응답);

        // and
        지하철_이용_요금_응답_확인(최단_거리_경로_조회_응답);
    }

    private ExtractableResponse<Response> 두_역의_최단_거리_경로_조회를_요청(Long source, Long target) {
        return RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/paths?source={sourceId}&target={targetId}&pathType={pathType}", source, target, DISTANCE)
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> 두_역의_최소_시간_경로_조회를_요청(Long source, Long target) {
        return RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/paths?source={sourceId}&target={targetId}&pathType={pathType}", source, target, DURATION)
                .then().log().all().extract();
    }

    private void 최소_시간_경로_조회_응답_확인(ExtractableResponse<Response> 최소_시간_경로_조회_응답) {
        assertThat(최소_시간_경로_조회_응답.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 최단_거리_경로_조회_응답_확인(ExtractableResponse<Response> 최단_거리_경로_조회_응답) {
        assertThat(최단_거리_경로_조회_응답.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 총_거리와_소요_시간_응답_확인(ExtractableResponse<Response> 최소_시간_경로_조회_응답) {
        assertThat(최소_시간_경로_조회_응답.jsonPath().getLong("distance")).isEqualTo(5);
        assertThat(최소_시간_경로_조회_응답.jsonPath().getLong("duration")).isEqualTo(2);
    }

    private void 지하철_이용_요금_응답_확인(ExtractableResponse<Response> 최단_거리_경로_조회_응답) {
        assertThat(최단_거리_경로_조회_응답.jsonPath().getLong("fare")).isEqualTo(1_250);
    }

    private Long 지하철_노선_생성_요청(String name, String color, Long upStation, Long downStation, int distance, int duration) {
        Map<String, String> lineCreateParams;
        lineCreateParams = new HashMap<>();
        lineCreateParams.put("name", name);
        lineCreateParams.put("color", color);
        lineCreateParams.put("upStationId", upStation + "");
        lineCreateParams.put("downStationId", downStation + "");
        lineCreateParams.put("distance", distance + "");
        lineCreateParams.put("duration", duration + "");

        return LineSteps.지하철_노선_생성_요청(관리자, lineCreateParams).jsonPath().getLong("id");
    }

    private Map<String, String> createSectionCreateParams(Long upStationId, Long downStationId, int distance) {
        Map<String, String> params = new HashMap<>();
        params.put("upStationId", upStationId + "");
        params.put("downStationId", downStationId + "");
        params.put("distance", distance + "");
        return params;
    }
}