package com.toit.items.attachments;

import com.toit.common.S3.config.S3Config;
import com.toit.common.enums.UploadStatus;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 만료된 업로드 예약(PENDING)을 회수하는 배치.
 *
 * <p>presign 은 용량을 선점하기 위해 PENDING 행을 남긴다. 정상 흐름이면 confirm 이 CONFIRMED 로
 * 전환하지만, 앱 종료나 네트워크 단절로 confirm 이 오지 않으면 PENDING 이 그대로 남는다.
 * PENDING 도 {@code status = ACTIVE} 라 용량 합산에 포함되므로, 회수하지 않으면
 * <b>사용자에게 보이지 않는 채로 5GB 가 차오른다.</b>
 *
 * <p>S3 객체를 먼저 지우고 DB 행을 지운다. 순서를 바꾸면 objectKey 를 잃어버려
 * S3 삭제 실패 시 회수 불가능한 고아 객체가 된다.
 *
 * <p><b>한계.</b> {@code @Scheduled} 는 인스턴스마다 독립 실행되므로 서버를 늘리면
 * 같은 예약을 여러 인스턴스가 동시에 회수하려 한다. 현재는 단일 인스턴스라 드러나지 않으며,
 * 스케줄러 중복 실행 문제는 알림 스케줄러와 함께 별도로 다룬다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentReservationCleaner {

    /**
     * presign URL 유효시간(5분)에 여유를 더한 값.
     * 이 시간이 지나도 confirm 이 오지 않으면 업로드가 완료될 가능성이 없다고 본다.
     */
    private static final long EXPIRE_MINUTES = 10;

    private static final long FIXED_DELAY_MS = 5 * 60 * 1000L;

    private final AttachMentsRepository attachMentsRepository;
    private final S3Config s3Storage;

    @PostConstruct
    void logRegistration() {
        log.info("[예약 정리] 배치 등록 | 주기={}분 | 만료기준={}분",
                FIXED_DELAY_MS / 60000, EXPIRE_MINUTES);
    }

    @Scheduled(fixedDelay = FIXED_DELAY_MS)
    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(EXPIRE_MINUTES);

        List<AttachMents> expired = attachMentsRepository
                .findByUploadStatusAndReservedAtBefore(UploadStatus.PENDING, cutoff);

        if (expired.isEmpty()) {
            // 회수 대상이 없어도 흔적을 남긴다. 로그가 아예 없으면
            // "배치가 도는데 대상이 없는 것"과 "배치가 안 도는 것"을 구분할 수 없다.
            log.debug("[예약 정리] 회수 대상 없음 | cutoff={}", cutoff);
            return;
        }

        int s3Deleted = 0;
        int s3Failed = 0;

        for (AttachMents reservation : expired) {
            // S3 삭제가 실패해도 DB 행은 지운다.
            // 예약을 남겨두면 용량을 계속 점유하는데, S3 객체는 회원 탈퇴 시
            // prefix 단위 정리(S3Config.deleteByPrefix)로 회수될 여지가 있다.
            try {
                s3Storage.delete(reservation.getObjectKey());
                s3Deleted++;
            } catch (Exception e) {
                s3Failed++;
                log.warn("[예약 정리] S3 객체 삭제 실패. objectKey={}, attachmentsId={}",
                        reservation.getObjectKey(), reservation.getAttachmentsId(), e);
            }
        }

        attachMentsRepository.deleteAll(expired);

        log.info("[예약 정리] 만료 예약 {}건 회수 | cutoff={} | s3삭제={} | s3실패={}",
                expired.size(), cutoff, s3Deleted, s3Failed);
    }
}
