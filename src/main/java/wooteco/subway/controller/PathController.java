package wooteco.subway.controller;

import javax.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wooteco.subway.dto.PathResponse;
import wooteco.subway.dto.WholeSubwayResponse;
import wooteco.subway.service.PathService;

@RequestMapping("/paths")
@RestController
@Validated
public class PathController {
	private final PathService pathService;

	public PathController(PathService pathService) {
		this.pathService = pathService;
	}

	@GetMapping
	public ResponseEntity<PathResponse> searchPath(@NotBlank String source,
		@NotBlank String target, @NotBlank String type) {
		PathResponse pathResponse = pathService.searchPath(source, target, type);
		return ResponseEntity.ok().body(pathResponse);
	}

	@GetMapping("/detail")
	public ResponseEntity<WholeSubwayResponse> retrieveLines() {
		WholeSubwayResponse wholeSubwayResponse = pathService.wholeLines();
		return ResponseEntity.ok()
			.eTag(String.valueOf(wholeSubwayResponse.hashCode()))
			.body(wholeSubwayResponse);
	}
}

