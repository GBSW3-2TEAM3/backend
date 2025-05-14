package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
public class MonthlyActivityResponse {
    @Schema(description = "산책 활동이 있었던 날짜 목록")
    private Set<LocalDate> activeDates;
}