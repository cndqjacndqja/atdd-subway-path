package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static wooteco.subway.acceptance.AcceptanceTestFixture.createStation;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.service.dto.LineResponse;
import wooteco.subway.ui.dto.LineRequest;

@DisplayName("지하철 노선 관련 기능")
class LineAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("존재하지 않는 노선을 생성한다.")
    void createLine() {
        // given
        Long station1 = createStation("강남역");
        Long station2 = createStation("역삼역");
        LineRequest lineRequest = new LineRequest("3호선", "bg-orange-600", station1, station2, 4, 0);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        // then
        LineResponse result = response.jsonPath().getObject("", LineResponse.class);
        assertThat(result).extracting(
                LineResponse::getName, LineResponse::getColor, i -> i.getStations().size())
                .containsExactly( "3호선", "bg-orange-600", 2);
        assertThat(response.header("Location")).isNotBlank();
    }

    @Test
    @DisplayName("이미 존재하는 노선을 생성할 수 없다.")
    void createLineWithDuplicateName() {
        // given
        Long station1 = createStation("강남역");
        Long station2 = createStation("역삼역");
        LineRequest lineRequest = new LineRequest("3호선", "bg-orange-600", station1, station2, 4, 0);
        RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then()
            .log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("노선들을 조회한다.")
    void getLines() {
        // given
        Long station1 = createStation("강남역");
        Long station2 = createStation("역삼역");
        LineRequest lineRequest = new LineRequest("3호선", "bg-orange-600", station1, station2, 4, 0);

        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .get("/lines")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> expectedLineIds = Arrays.asList(createResponse1).stream()
            .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
            .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
            .map(LineResponse::getId)
            .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    @Test
    @DisplayName("존재하는 노선을 제거한다. 상태코드는 200 이어야 한다.")
    void deleteStation() {
        // given
        Long station1 = createStation("강남역");
        Long station2 = createStation("역삼역");
        LineRequest lineRequest = new LineRequest("3호선", "bg-orange-600", station1, station2, 4, 0);
        ExtractableResponse<Response> createResponse = RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .delete(uri)
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("존재하지 않는 노선을 제거한다. 상태코드는 204 이어야 한다.")
    void deleteNonStation() {
        RestAssured.given().log().all()
            .when()
            .delete("lines/1")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("존재하는 노선을 수정한다. 상태코드는 200이어야 한다.")
    void updateLine() {
        // given
        Long station1 = createStation("강남역");
        Long station2 = createStation("역삼역");
        LineRequest lineRequest = new LineRequest("3호선", "bg-orange-600", station1, station2, 4, 0);
        ExtractableResponse<Response> createResponse = RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        LineRequest lineRequest2 = new LineRequest("2호선", "bg-green-600", station1, station2, 4, 0);

        // when
        String uri = createResponse.header("Location");

        // then
        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(lineRequest2)
            .when()
            .put(uri)
            .then().log().all()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("존재하지 않는 노선을 수정한다. 상태코드는 204이어야 한다.")
    void updateNonLine() {
        // given
        LineRequest lineRequest1 = new LineRequest("3호선", "bg-orange-600", 1L, 2L, 4, 0);

        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(lineRequest1)
            .when()
            .put("/lines/1")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("존재하지 않는 노선을 수정한다. 상태코드는 204이어야 한다.")
    void updateLineById() {
        // given
        LineRequest lineRequest1 = new LineRequest("3호선", "bg-orange-600", 1L, 2L, 4, 0);

        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(lineRequest1)
            .when()
            .put("/lines/1")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("노선 Id를 입력 받아 조회한다.")
    void findLineById() {
        // given
        Long station1 = createStation("강남역");
        Long station2 = createStation("역삼역");
        Long lineId = AcceptanceTestFixture.createLine("3호선", "bg-orange-600", station1, station2, 4);

        // when
        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/lines/" + lineId)
            .then().log().all()
            .statusCode(HttpStatus.OK.value());
    }
}
