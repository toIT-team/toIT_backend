package com.toit.notice;

import com.toit.admin.Admin;
import com.toit.admin.AdminRepository;
import com.toit.schedules.exception.SchedulesNotFoundException;
import com.toit.notification.push.FcmNotificationService;
import com.toit.notification.push.request.FcmNotificationRequest;
import com.toit.notice.dto.request.NoticeCreateRequest;
import com.toit.notice.dto.request.NoticeDeleteRequest;
import com.toit.notice.dto.request.NoticeUpdateRequest;
import com.toit.notice.dto.response.NoticeDeleteResponse;
import com.toit.notice.dto.response.NoticeReadResponse;
import com.toit.notice.dto.response.NoticeUpdateResponse;
import com.toit.notification.inbox.NotificationType;
import com.toit.notification.inbox.UserNotification;
import com.toit.notification.inbox.UserNotificationService;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final AdminRepository adminRepository;
    private final NoticeRepository noticeRepository;
    private final UsersRepository usersRepository;
    private final UserNotificationService userNotificationService;
    private final FcmNotificationService fcmNotificationService;

    // 공지사항 검증
    public Notice findByNotices(Long noticesId) {
        return noticeRepository.findById(noticesId)
                .orElseThrow(() -> new SchedulesNotFoundException(
                        "noticesId가 " + noticesId + "인 공지사항을 찾을 수 없습니다."));
    }

    // 공지사항 목록 조회
    public Page<NoticeReadResponse> getNotices(Pageable pageable) {
        return noticeRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(NoticeReadResponse::from);
    }

    // 관리자용 공지사항 생성
    public void createNotice(Long adminId, NoticeCreateRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice notice = new Notice(
                request.getTitle(),
                request.getContent(),
                admin,
                LocalDateTime.now()
        );

        Notice savedNotice = noticeRepository.save(notice);

        List<Users> users = usersRepository.findAll();
        List<UserNotification> notifications = userNotificationService.createAll(
                users,
                NotificationType.NOTICE,
                savedNotice.getTitle(),
                "toit://notice?id=" + savedNotice.getNoticeId(),
                savedNotice.getNoticeId()
        );

        for (UserNotification notification : notifications) {
            boolean isSent = fcmNotificationService.sendToUserIgnoringAppAlarmEnabled(
                    notification.getUsers(),
                    new FcmNotificationRequest(
                            savedNotice.getTitle(),
                            savedNotice.getContent(),
                            "notice",
                            notification.getDeeplink(),
                            notification.getNotificationId()
                    )
            );

            if (isSent) {
                userNotificationService.markAsSent(notification);
            }
        }
    }

    // 공지사항 삭제
    public NoticeDeleteResponse deleteNotice(Long adminId, NoticeDeleteRequest request) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice findNotice = findByNotices(request.getNoticeId());

        noticeRepository.delete(findNotice);
        return new NoticeDeleteResponse(findNotice.getNoticeId());
    }

    // 관리자용 공지사항 수정
    public NoticeUpdateResponse updateNotice(Long adminId, NoticeUpdateRequest request) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice findNotice = findByNotices(request.getNoticeId());

        findNotice.update(request.getTitle(), request.getContent());

        noticeRepository.save(findNotice);
        return new NoticeUpdateResponse(
                findNotice.getNoticeId(),
                findNotice.getTitle(),
                findNotice.getContent(),
                findNotice.getUpdatedAt()
        );
    }
}
