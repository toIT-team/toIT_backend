package com.toit.items.attachments;

import com.toit.common.enums.AttachMentsType;

import com.toit.common.enums.EntityStatus;
import com.toit.common.enums.UploadStatus;
import com.toit.items.shared.ItemsBase;
import com.toit.user.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(
        name = "attachments",
        indexes = {
                // 통합검색: 사용자별 활성 항목만 훑도록 스캔 대상 축소
                @Index(
                        name = "idx_attachments_users_status",
                        columnList = "users_id, status, attachments_type"
                )
        }
)
@Getter
public class AttachMents extends ItemsBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentsId;

    @Enumerated(EnumType.STRING)
    private AttachMentsType attachmentsType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String objectKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String presignedUrl;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String attachmentsExtension;

    @Column(nullable = false)
    private Double attachmentsSize;

    private String fileName;

    /**
     * 업로드 확정 상태. presign 시점에 PENDING 으로 예약해 용량을 선점하고,
     * confirm 에서 CONFIRMED 로 전환한다.
     *
     * <p>용량 합산(sumAttachmentsSizeByUsersId)에는 PENDING 도 포함되어야 하고,
     * 사용자 노출 조회에서는 CONFIRMED 만 보여야 한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadStatus uploadStatus;

    /**
     * 예약 시각. 정리 배치가 만료된 PENDING 을 판별하는 기준.
     */
    private LocalDateTime reservedAt;

    // Flutter 미사용으로 주석 처리 (DB 컬럼 유지)
    // private Long imagesWidth;
    // private Long imagesHeight;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    public static AttachMents createFilesInFolders(
            Users users,
            Long foldersId,
            String objectKey,
            String presignedUrl,
            String attachmentsExtension,
            Double attachmentsSize,
            String fileName,
            String textContent
    ) {
        AttachMents attachments = new AttachMents();
        attachments.attachmentsType = AttachMentsType.FILE;
        attachments.objectKey = objectKey;
        attachments.presignedUrl = presignedUrl;
        attachments.attachmentsExtension = attachmentsExtension;
        attachments.attachmentsSize = attachmentsSize;
        attachments.fileName = fileName;
        attachments.storageId = foldersId;
        attachments.textContent = textContent;
        attachments.status = EntityStatus.ACTIVE;
        attachments.uploadStatus = UploadStatus.CONFIRMED;
        attachments.createdAt = LocalDateTime.now();
        attachments.users = users;
        // attachments.imagesWidth = null;
        // attachments.imagesHeight = null;
        return attachments;
    }

    public static AttachMents createImagesInFolders(
            Users users,
            Long foldersId,
            String objectKey,
            String presignedUrl,
            String attachmentsExtension,
            Double attachmentsSize,
            String fileName,
            String textContent,
            Long width,
            Long height
    ) {
        AttachMents attachments = new AttachMents();
        attachments.attachmentsType = AttachMentsType.IMAGE;
        attachments.objectKey = objectKey;
        attachments.presignedUrl = presignedUrl;
        attachments.attachmentsExtension = attachmentsExtension;
        attachments.attachmentsSize = attachmentsSize;
        attachments.fileName = fileName;
        attachments.storageId = foldersId;
        attachments.textContent = textContent;
        attachments.status = EntityStatus.ACTIVE;
        attachments.uploadStatus = UploadStatus.CONFIRMED;
        attachments.createdAt = LocalDateTime.now();
        attachments.users = users;
        // attachments.imagesWidth = width;
        // attachments.imagesHeight = height;
        return attachments;
    }

    /**
     * presign 시점의 예약 행을 만든다.
     *
     * <p>아직 S3 업로드가 끝나지 않은 상태이므로 {@code PENDING} 이며, 사용자 조회에서는 제외된다.
     * 다만 {@code status} 는 {@code ACTIVE} 라 <b>용량 합산에는 포함</b>되어 자리를 선점한다.
     * 이것이 없으면 presign 을 여러 번 호출하는 것만으로 용량 제한을 넘길 수 있다.
     *
     * <p>{@code presignedUrl} 은 더 이상 사용하지 않지만 컬럼이 {@code nullable=false} 라 빈 값으로 채운다.
     */
    public static AttachMents reserve(
            Users users,
            Long foldersId,
            AttachMentsType attachmentsType,
            String objectKey,
            String attachmentsExtension,
            Double attachmentsSize,
            String fileName,
            String textContent
    ) {
        AttachMents attachments = new AttachMents();
        attachments.attachmentsType = attachmentsType;
        attachments.objectKey = objectKey;
        attachments.presignedUrl = "";
        attachments.attachmentsExtension = attachmentsExtension;
        attachments.attachmentsSize = attachmentsSize;
        attachments.fileName = fileName;
        attachments.storageId = foldersId;
        attachments.textContent = textContent;
        attachments.status = EntityStatus.ACTIVE;
        attachments.uploadStatus = UploadStatus.PENDING;
        attachments.reservedAt = LocalDateTime.now();
        attachments.createdAt = LocalDateTime.now();
        attachments.users = users;
        return attachments;
    }

    /**
     * S3 업로드가 끝난 예약을 확정한다.
     *
     * <p>새 행을 만드는 것이 아니라 presign 때 만들어 둔 예약 행의 상태만 바꾼다.
     * 여기서 새로 저장하면 예약분과 확정분이 이중으로 잡혀 용량이 두 배가 된다.
     */
    public void confirm() {
        if (this.uploadStatus != UploadStatus.PENDING) {
            throw new IllegalStateException("이미 확정된 첨부파일입니다. attachmentsId=" + this.attachmentsId);
        }
        this.uploadStatus = UploadStatus.CONFIRMED;
        this.reservedAt = null;
    }

    public void softDelete() {
        this.status = EntityStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void moveFolders(Long moveFoldersId) {
        this.storageId = moveFoldersId;
    }

    public void updateFileName(String fileName) {
        if (this.attachmentsType != AttachMentsType.FILE) {
            throw new IllegalStateException("FILE 타입의 첨부파일만 이름을 수정할 수 있습니다.");
        }
        this.fileName = fileName;
    }

    public void updateTextContent(String textContent) {
        this.textContent = textContent;
    }
}
