package com.example.referral.service;

import com.example.referral.model.Referral;
import com.example.referral.model.User;
import com.example.referral.repository.ReferralRepository;
import com.example.referral.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReferralRepository referralRepository;
    private final UserRepository userRepository;

    public ReportService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> generateReferralReport() {
        List<Referral> allReferrals = referralRepository.findAll();
        List<User> allUsers = userRepository.findAll();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalReferrals", allReferrals.size());
        metrics.put("successfulReferrals", countSuccessfulReferrals(allReferrals));
        metrics.put("referralsPerUser", generateReferralsPerUser(allReferrals));

        List<Map<String, Object>> referralDetails = allReferrals.stream()
            .map(referral -> {
                Map<String, Object> details = new HashMap<>();
                details.put("id", referral.getId());
                details.put("referrerEmail", referral.getReferrer().getEmail());
                details.put("refereeEmail", referral.getReferee().getEmail());
                details.put("status", referral.getStatus());
                details.put("createdAt", referral.getCreatedAt());
                details.put("updatedAt", referral.getUpdatedAt());
                return details;
            })
            .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("referrals", referralDetails);
        report.put("metrics", metrics);

        return report;
    }

    public byte[] generateCsvReport() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {

            // Write CSV header
            writer.write("Referrer Email,Referee Email,Status,Created At,Completed At\n");

            // Write data rows
            List<Referral> referrals = referralRepository.findAll();
            for (Referral referral : referrals) {
                writer.write(String.format("%s,%s,%s,%s,%s\n",
                        referral.getReferrer().getEmail(),
                        referral.getReferee().getEmail(),
                        referral.getStatus(),
                        referral.getCreatedAt(),
                        referral.getUpdatedAt()));
            }

            writer.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV report", e);
        }
    }

    private long countSuccessfulReferrals(List<Referral> referrals) {
        return referrals.stream()
                .filter(referral -> referral.getStatus() == Referral.Status.COMPLETED)
                .count();
    }

    private List<Map<String, Object>> generateReferralsPerUser(List<Referral> referrals) {
        return referrals.stream()
                .collect(Collectors.groupingBy(Referral::getReferrer))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> userStats = new HashMap<>();
                    userStats.put("email", entry.getKey().getEmail());
                    userStats.put("totalReferrals", entry.getValue().size());
                    userStats.put("successfulReferrals", countSuccessfulReferrals(entry.getValue()));
                    return userStats;
                })
                .collect(Collectors.toList());
    }
} 