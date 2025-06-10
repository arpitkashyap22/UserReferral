package com.example.referral.controller;

import com.example.referral.model.User;
import com.example.referral.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Analytics and reporting APIs")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/referrals")
    @Operation(summary = "Get referral report", description = "Retrieves analytics data about referrals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getReferralReport() {
        Map<String, Object> report = reportService.generateReferralReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/referrals/csv")
    @Operation(summary = "Download referral report as CSV", description = "Generates and downloads a CSV report of referral data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CSV report generated successfully",
            content = @Content(mediaType = "text/csv")),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getCsvReport() {
        byte[] csvData = reportService.generateCsvReport();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=referral-report.csv")
                .body(csvData);
    }
} 