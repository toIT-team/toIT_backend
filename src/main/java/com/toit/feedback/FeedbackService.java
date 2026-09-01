package com.toit.feedback;

import com.toit.admin.AdminRepository;
import com.toit.notification.push.FcmNotificationService;
import com.toit.notification.push.request.FcmNotificationRequest;
import com.toit.feedback.dto.request.FeedbackCreateRequest;
import com.toit.feedback.dto.request.FeedbackReplyRequest;
import com.toit.feedback.dto.response.FeedbackCreateResponse;
import com.toit.feedback.dto.response.FeedbackListResponse;
import com.toit.feedback.dto.response.FeedbackMyResponse;
import com.toit.feedback.exception.FeedbackNotFoundException;
import com.toit.notification.inbox.NotificationType;
import com.toit.notification.inbox.UserNotification;
import com.toit.notification.inbox.UserNotificationService;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final UsersRepository usersRepository;
    private final AdminRepository adminRepository;
    private final FcmNotificationService fcmNotificationService;
    private final UserNotificationService userNotificationService;

    public FeedbackCreateResponse create(Long usersId, FeedbackCreateRequest request) {
        Users user = usersRepository.findById(usersId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Feedback feedback = new Feedback(request.title(), request.content(), user);
        feedbackRepository.save(feedback);
        return new FeedbackCreateResponse(feedback.getFeedbackId());
    }

    public Page<FeedbackListResponse> getList(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(feedback -> FeedbackListResponse.from(feedback, resolveAdminName(feedback.getAdminId())));
    }

    public List<FeedbackMyResponse> getMyList(Long usersId) {
        return feedbackRepository.findAllByUsers_UsersIdOrderByCreatedAtDesc(usersId)
                .stream()
                .map(feedback -> FeedbackMyResponse.from(feedback, resolveAdminName(feedback.getAdminId())))
                .collect(Collectors.toList());
    }

    /**
     * 내 문의 단건 조회.
     *
     * 남의 문의를 훔쳐보지 못하도록 주인을 확인한다. 없는 번호와 남의 번호를 똑같이
     * 404 로 돌려주는데, 갈라 놓으면 "그 번호가 있긴 하다" 를 알려주는 셈이 된다.
     */
    public FeedbackMyResponse getMyFeedback(Long usersId, Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .filter(f -> f.getUsers() != null && f.getUsers().getUsersId().equals(usersId))
                .orElseThrow(() -> new FeedbackNotFoundException(
                        "feedbackId가 " + feedbackId + "인 문의를 찾을 수 없습니다."));

        return FeedbackMyResponse.from(feedback, resolveAdminName(feedback.getAdminId()));
    }

    private String resolveAdminName(Long adminId) {
        if (adminId == null) return null;
        return adminRepository.findById(adminId)
                .map(admin -> admin.getName())
                .orElse(null);
    }

    public void delete(Long usersId, Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피드백입니다."));

        // 탈퇴 회원의 피드백은 작성자가 익명화(null)되어 남으므로 누구도 삭제할 수 없다.
        if (feedback.getUsers() == null || !feedback.getUsers().getUsersId().equals(usersId)) {
            throw new IllegalArgumentException("본인의 문의만 삭제할 수 있습니다.");
        }

        if (feedback.getReply() != null) {
            throw new IllegalStateException("답변이 등록된 문의는 삭제할 수 없습니다.");
        }

        feedbackRepository.delete(feedback);
    }

    // 피드백/문의 답변 등록
    public void reply(Long feedbackId, Long adminId, FeedbackReplyRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피드백입니다."));

        feedback.addReply(request.getReply(), adminId);
        feedbackRepository.save(feedback);

        // 답변이 달린 그 문의로 바로 보낸다. 목록으로 보내면 사용자가 찾아야 한다.
        String deeplink = "toit://feedback?id=" + feedback.getFeedbackId();

        UserNotification notification = userNotificationService.create(
                feedback.getUsers(),
                NotificationType.FEEDBACK_REPLY,
                feedback.getTitle(),
                deeplink,
                feedback.getFeedbackId()
        );

        boolean isSent = fcmNotificationService.sendToUser(
                feedback.getUsers(),
                new FcmNotificationRequest(
                        // 알림함과 같은 제목을 쓴다. 같은 알림이 두 곳에서 다르게 보이면 안 된다.
                        feedback.getTitle(),
                        "문의하신 내용에 답변이 등록되었습니다.",
                        "feedback_reply",
                        deeplink,
                        notification.getNotificationId()
                )
        );

        if (isSent) {
            userNotificationService.markAsSent(notification);
        }
    }
}
