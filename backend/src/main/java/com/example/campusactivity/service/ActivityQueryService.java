package com.example.campusactivity.service;

import com.example.campusactivity.dto.ActivityCardResponse;
import com.example.campusactivity.dto.ActivityDetailResponse;
import com.example.campusactivity.dto.ActivityStatisticsResponse;
import com.example.campusactivity.dto.CategoryOptionResponse;
import com.example.campusactivity.dto.CheckinRecordResponse;
import com.example.campusactivity.dto.DashboardSummaryResponse;
import com.example.campusactivity.dto.FeedbackResponse;
import com.example.campusactivity.dto.RegistrationRecordResponse;
import com.example.campusactivity.dto.UserOptionResponse;
import com.example.campusactivity.dto.UserActivityHistoryResponse;
import java.util.List;
import com.example.campusactivity.exception.BusinessException;
import com.example.campusactivity.repository.ActivityCategoryRepository;
import com.example.campusactivity.repository.ActivityCheckinRepository;
import com.example.campusactivity.repository.ActivityFeedbackRepository;
import com.example.campusactivity.repository.ActivityRegistrationRepository;
import com.example.campusactivity.repository.ActivityRepository;
import com.example.campusactivity.repository.ActivityStatisticsRepository;
import com.example.campusactivity.repository.SysUserRepository;
import com.example.campusactivity.repository.UserActivityHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityQueryService {

    private final ActivityRepository activityRepository;
    private final ActivityCategoryRepository activityCategoryRepository;
    private final ActivityCheckinRepository activityCheckinRepository;
    private final ActivityFeedbackRepository activityFeedbackRepository;
    private final ActivityRegistrationRepository registrationRepository;
    private final ActivityStatisticsRepository activityStatisticsRepository;
    private final UserActivityHistoryRepository userActivityHistoryRepository;
    private final SysUserRepository sysUserRepository;

    public ActivityQueryService(
            ActivityRepository activityRepository,
            ActivityCategoryRepository activityCategoryRepository,
            ActivityCheckinRepository activityCheckinRepository,
            ActivityFeedbackRepository activityFeedbackRepository,
            ActivityRegistrationRepository registrationRepository,
            ActivityStatisticsRepository activityStatisticsRepository,
            UserActivityHistoryRepository userActivityHistoryRepository,
            SysUserRepository sysUserRepository
    ) {
        this.activityRepository = activityRepository;
        this.activityCategoryRepository = activityCategoryRepository;
        this.activityCheckinRepository = activityCheckinRepository;
        this.activityFeedbackRepository = activityFeedbackRepository;
        this.registrationRepository = registrationRepository;
        this.activityStatisticsRepository = activityStatisticsRepository;
        this.userActivityHistoryRepository = userActivityHistoryRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public List<ActivityCardResponse> listActivities() {
        return activityRepository.findActivityCards();
    }

    public ActivityDetailResponse getActivityDetail(Long activityId) {
        return activityRepository.findDetailByActivityId(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
    }

    public List<RegistrationRecordResponse> listRegistrations(Long activityId) {
        return registrationRepository.findByActivityId(activityId);
    }

    public List<CheckinRecordResponse> listCheckins(Long activityId) {
        return activityCheckinRepository.findByActivityId(activityId);
    }

    public List<FeedbackResponse> listFeedbacks(Long activityId) {
        return activityFeedbackRepository.findByActivityId(activityId);
    }

    public List<ActivityStatisticsResponse> listStatistics() {
        return activityStatisticsRepository.findActivityStatistics();
    }

    public DashboardSummaryResponse getDashboardSummary() {
        long totalActivities = activityRepository.count();
        long publishedActivities = activityRepository.countByStatus("published");
        long finishedActivities = activityRepository.countByStatus("finished");
        long totalRegistrations = registrationRepository.countByStatusIn(List.of("approved", "attended"));
        long totalCheckins = activityCheckinRepository.count();
        Double averageFeedbackRating = activityFeedbackRepository.findAverageRating();
        return new DashboardSummaryResponse(
                totalActivities,
                publishedActivities,
                finishedActivities,
                totalRegistrations,
                totalCheckins,
                averageFeedbackRating == null ? 0.0 : Math.round(averageFeedbackRating * 100.0) / 100.0
        );
    }

    public List<UserActivityHistoryResponse> listUserHistory(Long userId) {
        return userActivityHistoryRepository.findUserHistory(userId);
    }

    public List<CategoryOptionResponse> listCategoryOptions() {
        return activityCategoryRepository.findEnabledOptions();
    }

    public List<UserOptionResponse> listOrganizerOptions() {
        return sysUserRepository.findOrganizerOptions();
    }

    public List<UserOptionResponse> listEnabledUsers() {
        return sysUserRepository.findEnabledUsers();
    }
}
