package com.toit.items.shared;

import com.toit.common.enums.EntityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

@MappedSuperclass
@Getter
public abstract class ItemsBase {
    /**
     * 폴더 식별자 값
     */
    @Column(nullable = false)
    protected Long storageId;

    /**
     * 텍스트내용
     */
    @Column(nullable = true, length = 1000)
    protected String textContent;

    /**
     * 자료 서비스 상태
     * ENUM 형태(ACTIVE, DELETED)
     * ACTIVITY일 경우 활성화, DELETED 경우 비홣성화
     */
    @Enumerated(EnumType.STRING)
    protected EntityStatus status;
    /**
     * 자료 생성 후 시간 즉시 생성
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    /**
     * 자료 서비스 상태가 DELETED 되면 업데이트
     */
    @Column(nullable = true)
    private LocalDateTime deletedAt;
}
