package com.toit.user;

import com.toit.auth.token.RefreshTokenRepository;
import com.toit.common.S3.config.S3Config;
import com.toit.fcm.FcmTokenRepository;
import com.toit.feedback.FeedbackRepository;
import com.toit.folders.FoldersRepository;
import com.toit.items.attachments.AttachMentsRepository;
import com.toit.items.links.LinksRepository;
import com.toit.items.texts.TextsRepository;
import com.toit.notification.UserNotificationRepository;
import com.toit.schedules.SchedulesRepository;
import com.toit.schedulesalarm.SchedulesAlarmRepository;
import com.toit.user.settings.UsersSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 회원 탈퇴 시 사용자와 관련된 데이터를 완전 삭제(하드 딜리트)한다.
 * <p>
 * 피드백만 예외적으로 내용을 남기고 작성자를 익명화하며,
 * 탈퇴 통계를 위해 개인정보 없는 {@link WithdrawalLog} 를 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsersWithdrawService {

    private final UsersRepository usersRepository;
    private final WithdrawalLogRepository withdrawalLogRepository;
    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final SchedulesRepository schedulesRepository;
    private final FoldersRepository foldersRepository;
    private final TextsRepository textsRepository;
    private final LinksRepository linksRepository;
    private final AttachMentsRepository attachMentsRepository;
    private final UsersSettingsRepository usersSettingsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FeedbackRepository feedbackRepository;
    private final S3Config s3Storage;

    @Transactional
    public void withdraw(Users user) {
        Long usersId = user.getUsersId();

        // 사용자 레코드가 사라지므로 집계용 정보를 먼저 남긴다.
        withdrawalLogRepository.save(new WithdrawalLog(user));

        // FK 제약 때문에 순서가 강제된다: 알림 → 일정 → 보관함 → 자료 → 부가정보/토큰 → 사용자
        schedulesAlarmRepository.deleteAllByUsersId(usersId);
        schedulesRepository.deleteAllByUsersId(usersId);
        foldersRepository.deleteAllByUsersId(usersId);

        // 자료(texts/links/attachments)의 storageId 는 FK가 아니라 보관함과 순서 제약이 없다.
        textsRepository.deleteAllByUsersId(usersId);
        linksRepository.deleteAllByUsersId(usersId);
        attachMentsRepository.deleteAllByUsersId(usersId);

        usersSettingsRepository.deleteAllByUsersId(usersId);
        refreshTokenRepository.deleteAllByUsersId(usersId);
        fcmTokenRepository.deleteAllByUsersId(usersId);
        userNotificationRepository.deleteAllByUsersId(usersId);

        // 피드백은 운영 자료로 남기고 작성자만 끊는다.
        feedbackRepository.anonymizeByUsersId(usersId);

        usersRepository.deleteById(usersId);

        registerS3Cleanup(usersId);

        log.info("회원 탈퇴 완료 - usersId={}", usersId);
    }

    /**
     * S3 삭제는 롤백할 수 없으므로 DB 커밋이 확정된 뒤에만 실행한다.
     * 트랜잭션 안에서 먼저 지우면 이후 롤백 시 파일만 사라지고 레코드가 남아 복구가 불가능해진다.
     * 반대로 커밋 후에 실패하면 접근 경로가 없는 객체만 남으므로 로그를 남기고 넘어간다.
     */
    private void registerS3Cleanup(Long usersId) {
        String prefix = "users/" + usersId + "/";

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteS3Files(prefix);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteS3Files(prefix);
            }
        });
    }

    private void deleteS3Files(String prefix) {
        try {
            s3Storage.deleteByPrefix(prefix);
        } catch (Exception e) {
            log.error("탈퇴 사용자 S3 파일 삭제 실패 - 수동 정리 필요: prefix={}", prefix, e);
        }
    }
}
