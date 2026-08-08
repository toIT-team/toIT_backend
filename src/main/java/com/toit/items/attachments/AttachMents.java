package com.toit.items.attachments;

import com.toit.common.enums.AttachMentsType;

import com.toit.common.enums.EntityStatus;
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
        attachments.createdAt = LocalDateTime.now();
        attachments.users = users;
        // attachments.imagesWidth = width;
        // attachments.imagesHeight = height;
        return attachments;
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
