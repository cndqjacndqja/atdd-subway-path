package wooteco.subway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static wooteco.subway.service.ServiceTestFixture.deleteAllLine;
import static wooteco.subway.service.ServiceTestFixture.deleteAllStation;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.JdbcLineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.JdbcSectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.dao.JdbcStationDao;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Station;
import wooteco.subway.service.dto.LineServiceRequest;
import wooteco.subway.service.dto.LineResponse;
import wooteco.subway.service.dto.StationResponse;

@JdbcTest
class LineServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private LineDao lineDao;
    private StationDao stationDao;
    private LineService lineService;

    @BeforeEach
    void setUp() {
        lineDao = new JdbcLineDao(jdbcTemplate);
        SectionDao sectionDao = new JdbcSectionDao(jdbcTemplate);
        stationDao = new JdbcStationDao(jdbcTemplate);
        lineService = new LineService(lineDao, sectionDao, stationDao);

        deleteAllLine(lineDao);
        deleteAllStation(stationDao);
    }

    @Test
    void save() {
        // given
        Long savedId1 = stationDao.save(new Station("강남역"));
        Long savedId2 = stationDao.save(new Station("역삼역"));
        LineServiceRequest line = new LineServiceRequest("1호선", "bg-red-600", savedId1,
            savedId2, 5);

        // when
        LineResponse savedLine = lineService.save(line);
        Line result = lineDao.findById(savedLine.getId()).get();

        // then
        assertThat(line.getName()).isEqualTo(result.getName());
    }

    @Test
    void validateDuplication() {
        // given
        Long savedId1 = stationDao.save(new Station("강남역"));
        Long savedId2 = stationDao.save(new Station("역삼역"));
        LineServiceRequest line1 = new LineServiceRequest("1호선", "bg-red-600", savedId1,
            savedId2, 5);
        LineServiceRequest line2 = new LineServiceRequest("1호선", "bg-red-600", savedId1,
            savedId2, 5);

        // when
        lineService.save(line1);

        // then
        assertThatThrownBy(() -> lineService.save(line2))
            .hasMessage("중복된 이름이 존재합니다.")
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAll() {
        // given
        Long savedId1 = stationDao.save(new Station("강남역"));
        Long savedId2 = stationDao.save(new Station("역삼역"));
        LineServiceRequest line1 = new LineServiceRequest("1호선", "bg-red-600", savedId1,
            savedId2, 5);
        LineServiceRequest line2 = new LineServiceRequest("2호선", "bg-green-600", savedId1,
            savedId2, 5);

        // when
        lineService.save(line1);
        lineService.save(line2);

        // then
        List<String> names = lineService.findAll()
            .stream()
            .map(LineResponse::getName)
            .collect(Collectors.toList());

        assertThat(names)
            .hasSize(2)
            .contains(line1.getName(), line2.getName());
    }

    @Test
    void delete() {
        // given
        Long savedId1 = stationDao.save(new Station("강남역"));
        Long savedId2 = stationDao.save(new Station("역삼역"));
        LineServiceRequest line = new LineServiceRequest("1호선", "bg-red-600", savedId1,
            savedId2, 5);
        LineResponse savedLine = lineService.save(line);

        // when
        lineService.deleteById(savedLine.getId());

        // then
        List<Long> lineIds = lineService.findAll()
            .stream()
            .map(LineResponse::getId)
            .collect(Collectors.toList());

        assertThat(lineIds).doesNotContain(savedLine.getId());
    }

    @Test
    void update() {
        // given
        Long savedId1 = stationDao.save(new Station("강남역"));
        Long savedId2 = stationDao.save(new Station("역삼역"));
        LineServiceRequest originLine = new LineServiceRequest("1호선", "bg-red-600",
            savedId1,
            savedId2, 5);
        LineResponse savedLine = lineService.save(originLine);

        // when
        LineServiceRequest newLineEntity = new LineServiceRequest("2호선", "bg-green-600");
        lineService.updateById(savedLine.getId(), newLineEntity);
        Line lineEntity = lineDao.findById(savedLine.getId()).get();

        // then
        assertThat(lineEntity.getName()).isEqualTo(newLineEntity.getName());
    }

    @Test
    void findById() {
        // given
        Long savedId1 = stationDao.save(new Station("강남역"));
        Long savedId2 = stationDao.save(new Station("역삼역"));
        LineServiceRequest originLine = new LineServiceRequest("1호선", "bg-red-600",
            savedId1,
            savedId2, 5);
        LineResponse savedLine = lineService.save(originLine);

        // when
        LineResponse lineResponse = lineService.findById(savedLine.getId());

        // then
        List<StationResponse> stations = lineResponse.getStations();
        List<String> names = stations.stream()
            .map(StationResponse::getName)
            .collect(Collectors.toList());
        assertAll(
            () -> assertThat(lineResponse.getName()).isEqualTo("1호선"),
            () -> assertThat(lineResponse.getColor()).isEqualTo("bg-red-600"),
            () -> assertThat(names).containsExactly("강남역", "역삼역")
        );
    }
}
