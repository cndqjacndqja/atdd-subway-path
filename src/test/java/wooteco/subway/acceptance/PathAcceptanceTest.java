package wooteco.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.ui.dto.LineRequest;
import wooteco.subway.ui.dto.SectionRequest;


class PathAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("존재하지 않는 노선을 생성한다.")
    void findShortestPath() {
        // given
        createStation("강남역");
        createStation("교대역");
        createStation("역삼역");
        createStation("선릉역");

        LineRequest lineRequest = new LineRequest("3호선", "bg-orange-600", 1L, 2L, 3);
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        SectionRequest sectionRequest = new SectionRequest(2L, 3L, 4);
        RestAssured.given().log().all()
            .body(sectionRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines/" + 1L + "/sections")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SectionRequest sectionRequest2 = new SectionRequest(3L, 4L, 5);
        RestAssured.given().log().all()
            .body(sectionRequest2)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines/" + 1L + "/sections")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // when
        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .queryParam("source", 1)
            .queryParam("target", 6)
            .queryParam("age", 20)
            .when()
            .get("/paths")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();
    }

    void createStation(String name) {
        Map<String, String> params1 = new HashMap<>();
        params1.put("name", name);
        RestAssured.given().log().all()
            .body(params1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/stations")
            .then().log().all()
            .extract();
    }
}
