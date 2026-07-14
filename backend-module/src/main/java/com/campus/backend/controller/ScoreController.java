package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Score;
import com.campus.backend.service.ScoreService;
import com.campus.common.dto.ScoreDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/scores")
@AllArgsConstructor
public class ScoreController {
    private final ScoreService scoreService;

    @PostMapping
    public ApiResponse<Score> create(@Valid @RequestBody ScoreDTO dto,
                                     HttpServletRequest request) {
        Long enteredBy = (Long) request.getAttribute("userId");
        return ApiResponse.success(scoreService.create(dto, enteredBy));
    }

    @PutMapping("/{id}")
    public ApiResponse<Score> update(@PathVariable Long id,
                                     @Valid @RequestBody ScoreDTO dto,
                                     HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        return ApiResponse.success(scoreService.updateScore(id, dto, operatorId));
    }

    @GetMapping
    public ApiResponse<PageResult<Score>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) BigDecimal minScore,
            @RequestParam(required = false) BigDecimal maxScore,
            @RequestParam(required = false) Long classId) {
        IPage<Score> result = scoreService.pageList(page, size, examId, courseId, minScore, maxScore, classId);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody String status) {
        scoreService.updateStatus(id, status);
        return ApiResponse.success();
    }
}
