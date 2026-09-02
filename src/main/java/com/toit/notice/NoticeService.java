package com.toit.notice;

import com.toit.admin.Admin;
import com.toit.admin.AdminRepository;
import com.toit.common.enums.EntityStatus;
import com.toit.notice.exception.NoticeNotFoundException;
import com.toit.notification.push.FcmNotificationService;
import com.toit.notification.push.request.FcmNotificationRequest;
import com.toit.notice.dto.request.NoticeCreateRequest;
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
                .orElseThrow(() -> new NoticeNotFoundException(
                        "noticesId가 " + noticesId + "인 공지사항을 찾을 수 없습니다."));
    }

    /**
     * 공지사항 단건 조회.
     *
     * 알림함의 딥링크(toit://notice?id=7)가 한 건을 가리키므로, 목록을 받아 거기서
     * 찾는 대신 바로 가져올 수 있어야 한다.
     */
    public NoticeReadResponse getNotice(Long noticeId) {
        return NoticeReadResponse.from(findByNotices(noticeId));
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

        // 알림함에만 남기고 푸시는 안 보낸다.
        userNotificationService.createAllAsSent(
                usersRepository.findAllByStatus(EntityStatus.ACTIVE),
                NotificationType.NOTICE,
                savedNotice.getTitle(),
                "toit://notice?id=" + savedNotice.getNoticeId(),
                savedNotice.getNoticeId()
        );

        // 푸시는 잠시 멈춰 둔다. 지금 구조로 켜면 두 가지가 걸린다.
        //   - 관리자 요청이 전체 발송을 마칠 때까지 안 끝난다. 한 건씩 순서대로
        //     보내므로 사용자가 1,000명이면 5분 넘게 붙잡힌다
        //   - sendToUserIgnoringAppAlarmEnabled 라 알림을 꺼둔 사람에게도 간다
        //
        // 둘을 정리한 뒤 되살린다.
        //
        // for (UserNotification notification : notifications) {
        //     boolean isSent = fcmNotificationService.sendToUserIgnoringAppAlarmEnabled(
        //             notification.getUsers(),
        //             new FcmNotificationRequest(
        //                     savedNotice.getTitle(),
        //                     savedNotice.getContent(),
        //                     "notice",
        //                     notification.getDeeplink(),
        //                     notification.getNotificationId()
        //             )
        //     );
        //
        //     if (isSent) {
        //         userNotificationService.markAsSent(notification);
        //     }
        // }
    }

    // 공지사항 삭제
    public NoticeDeleteResponse deleteNotice(Long adminId, Long noticeId) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice findNotice = findByNotices(noticeId);

        noticeRepository.delete(findNotice);
        return new NoticeDeleteResponse(findNotice.getNoticeId());
    }

    // 관리자용 공지사항 수정
    public NoticeUpdateResponse updateNotice(Long adminId, Long noticeId, NoticeUpdateRequest request) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice findNotice = findByNotices(noticeId);

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
