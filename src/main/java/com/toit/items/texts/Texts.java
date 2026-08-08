package com.toit.items.texts;

import com.toit.common.enums.EntityStatus;
import com.toit.items.shared.ItemsBase;
import com.toit.user.Users;
import jakarta.persistence.Entity;
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
        name = "texts",
        indexes = {
                // 통합검색: 사용자별 활성 항목만 훑도록 스캔 대상 축소
                @Index(name = "idx_texts_users_status", columnList = "users_id, status")
        }
)
@Getter
public class Texts extends ItemsBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long textsId;

    /**
     * Users와 N:1 관계 설정
     */
    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    public static Texts createTextsInFolders(
            Users users,
            Long storageId,
            String textContent
    ) {
        Texts texts = new Texts();
        texts.users = users;
        texts.textContent = textContent;
        texts.storageId = storageId;
        texts.status = EntityStatus.ACTIVE;
        texts.createdAt = LocalDateTime.now();
        return texts;
    }

    public void softDelete() {
        this.status = EntityStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void moveFolders(Long moveFoldersId){
        this.storageId = moveFoldersId;
    }

    public void updateTextContent(String textContent) {
        this.textContent = textContent;
    }

}
