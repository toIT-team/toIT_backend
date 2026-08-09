package com.toit.common.enums;

/**
 * 첨부파일 업로드 확정 상태.
 *
 * <p>업로드는 {@code presign → S3 PUT → confirm} 3단계로 나뉜다. 검증(presign)과 저장(confirm)
 * 사이에 간격이 있어, 그 사이를 비워두면 용량 제한이 보장되지 않는다.
 *
 * <ul>
 *   <li>{@code PENDING}   presign 시점에 만들어 두는 예약. 아직 S3 업로드가 확정되지 않았다.
 *                         <b>용량 합산에는 포함</b>되어 자리를 선점하고, 사용자 조회에서는 제외된다.
 *   <li>{@code CONFIRMED} confirm 으로 업로드가 확정된 상태. 사용자에게 노출된다.
 * </ul>
 *
 * <p>만료된 {@code PENDING} 은 정리 배치가 S3 객체와 함께 회수한다.
 */
public enum UploadStatus {
    PENDING,
    CONFIRMED
}
