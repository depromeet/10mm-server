package com.depromeet.domain.ranking.api;

import com.depromeet.domain.ranking.application.RankingService;
import com.depromeet.domain.ranking.dto.response.RankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "10. [랭킹]", description = "랭킹 관련 API")
@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "랭킹 조회", description = "랭킹을 조회합니다.")
    @GetMapping
    public List<RankingResponse> rankingFindAll() {
        return rankingService.findAllRanking();
    }
}
