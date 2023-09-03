package nextstep.subway.acceptance;

import static nextstep.subway.acceptance.step.LineSteps.지하철_노선_목록을_조회한다;
import static nextstep.subway.acceptance.step.LineSteps.지하철_노선을_삭제한다;
import static nextstep.subway.acceptance.step.LineSteps.지하철_노선을_생성한다;
import static nextstep.subway.acceptance.step.LineSteps.지하철_노선을_수정한다;
import static nextstep.subway.acceptance.step.LineSteps.지하철_노선을_조회한다;
import static nextstep.subway.acceptance.step.SectionSteps.지하철_노선_구간을_등록한다;
import static nextstep.subway.acceptance.step.StationStep.지하철역을_생성한다;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import nextstep.utils.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {
    private Long 강남역;
    private Long 양재역;

    @BeforeEach
    void setUpData() {
        강남역 = 지하철역을_생성한다("강남역").jsonPath().getLong("id");
        양재역 = 지하철역을_생성한다("양재역").jsonPath().getLong("id");
    }

    /**
     * When: 지하철 노선을 생성하면
     * Then: 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @DisplayName("지하철 노선 생성")
    @Test
    void createLine() {
        // when
        String lineName = "신분당선";
        ExtractableResponse<Response> responseOfCreate = 지하철_노선을_생성한다(강남역, 양재역, lineName, 10, 10);

        // then
        assertThat(responseOfCreate.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        long createdLineId = Id_추출(responseOfCreate);
        ExtractableResponse<Response> responseOfRead = 지하철_노선을_조회한다(createdLineId);

        String findLineName = 지하철_노선_이름을_추출한다(responseOfRead);
        assertThat(findLineName).isEqualTo(lineName);
    }

    private long Id_추출(ExtractableResponse<Response> responseOfCreateStation) {
        return responseOfCreateStation.jsonPath().getLong("id");
    }

    private String 지하철_노선_이름을_추출한다(ExtractableResponse<Response> responseOfRead) {
        return responseOfRead.jsonPath().getString("name");
    }

    /**
     * Given: 2개의 지하철 노선을 생성하고
     * When: 지하철 노선 목록을 조회하면
     * Then: 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다
     */
    @DisplayName("지하철 노선 목록 조회")
    @Test
    void findAllLines() {
        // given
        지하철_노선을_생성한다(강남역, 양재역, "신분당선", 10, 10);

        long 가양역 = Id_추출(지하철역을_생성한다("가양역"));
        long 여의도역 = Id_추출(지하철역을_생성한다("여의도역"));
        지하철_노선을_생성한다(가양역, 여의도역, "9호선", 10, 10);

        // when
        ExtractableResponse<Response> response = 지하철_노선_목록을_조회한다();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철_노선_목록_이름을_추출한다(response)).hasSize(2);
    }

    private List<String> 지하철_노선_목록_이름을_추출한다(ExtractableResponse<Response> response) {
        return response.jsonPath().getList("name", String.class);
    }

    /**
     * Given:
     * Given: 지하철 노선을 생성하고
     * And : 구간을 2개 추가한 후
     * When: 생성한 지하철 노선을 조회하면
     * Then: 생성한 지하철 노선의 정보를 응답받을 수 있다
     */
    @DisplayName("지하철 노선 조회")
    @Test
    void findLine() {
        // given
        long 신논현역 = Id_추출(지하철역을_생성한다("신논현역"));
        long 양재시민의숲역 = Id_추출(지하철역을_생성한다("양재시민의숲역"));

        long lineId = Id_추출(지하철_노선을_생성한다(강남역, 양재시민의숲역, "신분당선", 10, 10));

        지하철_노선_구간을_등록한다(lineId, 강남역, 양재역, 5, 5);
        지하철_노선_구간을_등록한다(lineId, 신논현역, 강남역, 10, 10);

        // when
        ExtractableResponse<Response> responseOfFindLine = 지하철_노선을_조회한다(lineId);

        // then
        assertThat(responseOfFindLine.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(Id_추출(responseOfFindLine)).isEqualTo(lineId);

        List<Long> upStationIds = 응답_결과에서_구간의_상행역_Id를_추출한다(responseOfFindLine);
        assertThat(upStationIds).containsExactly(신논현역, 강남역, 양재역);
    }

    private List<Long> 응답_결과에서_구간의_상행역_Id를_추출한다(ExtractableResponse<Response> responseOfCreateStation) {
        return responseOfCreateStation.jsonPath().getList("sections.upStationId", Long.class);
    }

    /**
     * Given: 지하철 노선을 생성하고
     * When: 생성한 지하철 노선을 수정하면
     * Then: 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철 노선 수정")
    @Test
    void updateLine() {
        // given
        ExtractableResponse<Response> responseOfCreateLine = 지하철_노선을_생성한다(강남역, 양재역, "신분당선", 10, 10);

        // when
        long lineId = Id_추출(responseOfCreateLine);
        String lineNameForUpdate = "구분당선";
        String lineColorForUpdate = "bg-sky-500";
        ExtractableResponse<Response> responseOfUpdateLine = 지하철_노선을_수정한다(lineId, lineNameForUpdate, lineColorForUpdate);

        // then
        assertThat(responseOfUpdateLine.statusCode()).isEqualTo(HttpStatus.OK.value());

        ExtractableResponse<Response> responseOfShowLine = 지하철_노선을_조회한다(lineId);
        assertThat(지하철_노선_이름을_추출한다(responseOfShowLine)).isEqualTo(lineNameForUpdate);
        assertThat(지하철_노선_색상을_추출한다(responseOfShowLine)).isEqualTo(lineColorForUpdate);
    }

    private String 지하철_노선_색상을_추출한다(ExtractableResponse<Response> responseOfShowLine) {
        return responseOfShowLine.jsonPath().getString("color");
    }

    /**
     * Given: 지하철 노선을 생성하고
     * When: 생성한 지하철 노선을 삭제하면
     * Then: 해당 지하철 노선 정보는 삭제된다
     */
    @DisplayName("지하철 노선 삭제")
    @Test
    void deleteLine() {
        // given
        ExtractableResponse<Response> responseOfCreateLine = 지하철_노선을_생성한다(강남역, 양재역, "신분당선", 10, 10);

        // when
        long lineId = Id_추출(responseOfCreateLine);
        ExtractableResponse<Response> responseOfDelete = 지하철_노선을_삭제한다(lineId);

        // then
        assertThat(responseOfDelete.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        ExtractableResponse<Response> responseAfterDelete = 지하철_노선을_조회한다(lineId);
        assertThat(responseAfterDelete.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
