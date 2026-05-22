package com.example.campusactivity.service;

import com.example.campusactivity.auth.AuthContextHolder;
import com.example.campusactivity.auth.AuthenticatedUser;
import com.example.campusactivity.dto.ActivityCheckinRequest;
import com.example.campusactivity.dto.ActivityCheckinResponse;
import com.example.campusactivity.dto.ActivityDetailResponse;
import com.example.campusactivity.dto.CreateFeedbackRequest;
import com.example.campusactivity.dto.CreateActivityRequest;
import com.example.campusactivity.dto.FeedbackResponse;
import com.example.campusactivity.dto.RegisterActivityRequest;
import com.example.campusactivity.dto.RegistrationRecordResponse;
import com.example.campusactivity.dto.UpdateActivityRequest;
import com.example.campusactivity.entity.Activity;
import com.example.campusactivity.entity.ActivityCategory;
import com.example.campusactivity.entity.ActivityCheckin;
import com.example.campusactivity.entity.ActivityFeedback;
import com.example.campusactivity.entity.ActivityRegistration;
import com.example.campusactivity.entity.SysUser;
import com.example.campusactivity.exception.BusinessException;
import com.example.campusactivity.exception.ForbiddenException;
import com.example.campusactivity.repository.ActivityCategoryRepository;
import com.example.campusactivity.repository.ActivityCheckinRepository;
import com.example.campusactivity.repository.ActivityFeedbackRepository;
import com.example.campusactivity.repository.ActivityRegistrationRepository;
import com.example.campusactivity.repository.ActivityRepository;
import com.example.campusactivity.repository.SysUserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ActivityCommandService {

    private static final Set<String> ACTIVITY_STATUSES = Set.of("draft", "published", "cancelled", "finished");
    private static final Set<String> CHECKIN_RESULTS = Set.of("normal", "late", "manual");
    private static final Set<String> ALLOWED_REGISTRATION_STATUSES = Set.of("approved", "attended");
    private static final Set<String> ORGANIZER_ROLE_CODES = Set.of("organizer", "admin");

    private final ActivityRepository activityRepository;
    private final ActivityCategoryRepository categoryRepository;
    private final SysUserRepository userRepository;
    private final ActivityRegistrationRepository registrationRepository;
    private final ActivityCheckinRepository checkinRepository;
    private final ActivityFeedbackRepository feedbackRepository;

    public ActivityCommandService(
            ActivityRepository activityRepository,
            ActivityCategoryRepository categoryRepository,
            SysUserRepository userRepository,
            ActivityRegistrationRepository registrationRepository,
            ActivityCheckinRepository checkinRepository,
            ActivityFeedbackRepository feedbackRepository
    ) {
        this.activityRepository = activityRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.checkinRepository = checkinRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public ActivityDetailResponse createActivity(CreateActivityRequest request) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        validateActivityTimeWindow(request.startTime(), request.endTime(), request.registrationDeadline());
        validateActivityStatus(request.status());

        ActivityCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("活动分类不存在"));
        SysUser organizer = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));

        if (!currentUser.canPublishActivities() || !ORGANIZER_ROLE_CODES.contains(organizer.getRole().getRoleCode())) {
            throw new BusinessException("只有组织者或管理员可以创建活动");
        }

        Activity activity = new Activity();
        activity.setCategory(category);
        activity.setOrganizer(organizer);
        activity.setTitle(request.title());
        activity.setDescription(request.description());
        activity.setLocation(request.location());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setRegistrationDeadline(request.registrationDeadline());
        activity.setCapacity(request.capacity());
        activity.setStatus(request.status());

        Activity saved = activityRepository.save(activity);
        return toActivityDetailResponse(saved);
    }

    @Transactional
    public RegistrationRecordResponse registerActivity(Long activityId, RegisterActivityRequest request) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        SysUser user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));

        if (!"published".equals(activity.getStatus())) {
            throw new BusinessException("当前活动不允许报名");
        }
        if (LocalDateTime.now().isAfter(activity.getRegistrationDeadline())) {
            throw new BusinessException("报名截止时间已过");
        }
        if (!"student".equals(currentUser.roleCode())) {
            throw new ForbiddenException("只有学生账号可以报名活动");
        }
        if (registrationRepository.existsByActivityActivityIdAndUserUserId(activityId, currentUser.userId())) {
            throw new BusinessException("你已报名当前活动");
        }

        long approvedCount = registrationRepository.countByActivityActivityIdAndStatusIn(activityId, List.copyOf(ALLOWED_REGISTRATION_STATUSES));
        if (approvedCount >= activity.getCapacity()) {
            throw new BusinessException("活动名额已满");
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivity(activity);
        registration.setUser(user);
        registration.setStatus("approved");
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setRemark(request.remark());

        return new RegistrationRecordResponse(
                registrationRepository.saveAndFlush(registration).getRegistrationId(),
                user.getUserId(),
                user.getRealName(),
                user.getStudentNo(),
                registration.getStatus(),
                registration.getRegisteredAt()
        );
    }

    @Transactional
    public ActivityDetailResponse updateActivity(Long activityId, UpdateActivityRequest request) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        validateActivityTimeWindow(request.startTime(), request.endTime(), request.registrationDeadline());
        validateActivityStatus(request.status());

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        ActivityCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("活动分类不存在"));
        ensureCanManageActivity(currentUser, activity);

        activity.setCategory(category);
        activity.setTitle(request.title());
        activity.setDescription(request.description());
        activity.setLocation(request.location());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setRegistrationDeadline(request.registrationDeadline());
        activity.setCapacity(request.capacity());
        activity.setStatus(request.status());

        return toActivityDetailResponse(activityRepository.save(activity));
    }

    @Transactional
    public ActivityCheckinResponse checkIn(Long activityId, ActivityCheckinRequest request) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        validateCheckinResult(request.checkinResult());

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        ensureCanManageActivity(currentUser, activity);
        SysUser operator = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));
        ActivityRegistration registration = registrationRepository
                .findByActivityActivityIdAndUserUserId(activityId, request.userId())
                .orElseThrow(() -> new BusinessException("该用户没有当前活动的报名记录"));

        if (!Set.of("approved", "attended").contains(registration.getStatus())) {
            throw new BusinessException("当前报名状态不允许签到");
        }
        if ("cancelled".equals(activity.getStatus())) {
            throw new BusinessException("活动已取消，不能签到");
        }
        if (checkinRepository.existsByRegistrationRegistrationId(registration.getRegistrationId())) {
            throw new BusinessException("该用户已经签到");
        }

        ActivityCheckin checkin = new ActivityCheckin();
        checkin.setRegistration(registration);
        checkin.setOperator(operator);
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setCheckinResult(request.checkinResult());
        checkin.setNote(request.note());

        ActivityCheckin saved = checkinRepository.saveAndFlush(checkin);
        registration.setStatus("attended");

        return new ActivityCheckinResponse(
                saved.getCheckinId(),
                registration.getRegistrationId(),
                activity.getActivityId(),
                registration.getUser().getUserId(),
                registration.getUser().getRealName(),
                saved.getCheckinTime(),
                saved.getCheckinResult(),
                operator.getUserId(),
                saved.getNote()
        );
    }

    @Transactional
    public FeedbackResponse createFeedback(Long activityId, CreateFeedbackRequest request) {
        AuthenticatedUser currentUser = AuthContextHolder.require();
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        SysUser user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));
        ActivityRegistration registration = registrationRepository
                .findByActivityActivityIdAndUserUserId(activityId, currentUser.userId())
                .orElseThrow(() -> new BusinessException("该用户没有当前活动的报名记录"));

        if (!"student".equals(currentUser.roleCode())) {
            throw new ForbiddenException("只有学生账号可以提交活动反馈");
        }

        if (!"finished".equals(activity.getStatus())) {
            throw new BusinessException("只有已结束活动才能提交反馈");
        }
        if (!"attended".equals(registration.getStatus())) {
            throw new BusinessException("只有已参加活动的用户才能提交反馈");
        }
        if (feedbackRepository.existsByActivityActivityIdAndUserUserId(activityId, currentUser.userId())) {
            throw new BusinessException("你已经提交过该活动反馈");
        }

        ActivityFeedback feedback = new ActivityFeedback();
        feedback.setActivity(activity);
        feedback.setUser(user);
        feedback.setRating(request.rating());
        feedback.setContent(request.content());
        feedback.setCreatedAt(LocalDateTime.now());

        ActivityFeedback saved = feedbackRepository.saveAndFlush(feedback);
        return new FeedbackResponse(
                saved.getFeedbackId(),
                user.getUserId(),
                user.getRealName(),
                saved.getRating(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    private void validateActivityTimeWindow(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime registrationDeadline) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("活动结束时间必须晚于开始时间");
        }
        if (registrationDeadline.isAfter(startTime)) {
            throw new BusinessException("报名截止时间必须早于或等于活动开始时间");
        }
    }

    private void validateActivityStatus(String status) {
        if (!ACTIVITY_STATUSES.contains(status)) {
            throw new BusinessException("非法的活动状态");
        }
    }

    private void validateCheckinResult(String checkinResult) {
        if (!CHECKIN_RESULTS.contains(checkinResult)) {
            throw new BusinessException("非法的签到结果");
        }
    }

    private void ensureCanManageActivity(AuthenticatedUser currentUser, Activity activity) {
        boolean isOwner = activity.getOrganizer().getUserId().equals(currentUser.userId());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new ForbiddenException("只有活动发起者或管理员可以执行该操作");
        }
    }

    private ActivityDetailResponse toActivityDetailResponse(Activity activity) {
        return new ActivityDetailResponse(
                activity.getActivityId(),
                activity.getCategory().getCategoryId(),
                activity.getCategory().getCategoryName(),
                activity.getOrganizer().getUserId(),
                activity.getOrganizer().getRealName(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getLocation(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getRegistrationDeadline(),
                activity.getCapacity(),
                activity.getStatus()
        );
    }
}
