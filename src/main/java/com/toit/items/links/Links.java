package com.toit.items.links;

import com.toit.common.enums.EntityStatus;
import com.toit.items.shared.ItemsBase;
import com.toit.user.Users;
import jakarta.persistence.Column;
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
        name = "links",
        indexes = {
                // 통합검색: 사용자별 활성 항목만 훑도록 스캔 대상 축소
                @Index(name = "idx_links_users_status", columnList = "users_id, status")
        }
)
@Getter
public class Links extends ItemsBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linksId;

    /**
     * 링크 제목
     * 길이 제한 255
     */
    @Column(nullable = true)
    private String linksName;

    /**
     * 링크 URL
     * 길이 제한 2000
     */
    @Column(nullable = true, length = 2000)
    private String linksUrl;

    /**
     * 썸네일 URL
     * 길이 제한 2000
     */
    @Column(nullable = true, length = 2000)
    private String linksThumbnail;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    public static Links createLinksInFolders(
            Users users,
            String linksName,
            String linksUrl,
            String linksThumbnail,
            Long storageId,
            String textContent

    ) {
        Links links = new Links();
        links.users = users;
        links.linksName = linksName;
        links.linksUrl = linksUrl;
        links.linksThumbnail = linksThumbnail;
        links.textContent = textContent;
        links.storageId = storageId;
        links.status = EntityStatus.ACTIVE;
        links.createdAt = LocalDateTime.now();
        return links;
    }

    public void softDelete() {
        this.status = EntityStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void moveFolders(Long moveFoldersId){
        this.storageId = moveFoldersId;
    }

    public void updateDetails(String linksName, String textContent) {
        this.linksName = linksName;
        this.textContent = textContent;
    }

}
