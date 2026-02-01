package com.toit.foldersview;

import com.toit.folders.Folders;
import com.toit.user.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "folders_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersViews {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foldersViewsId;


    /**
     * 최근 본 폴더 시간
     */
    @Column(nullable = false)
    private LocalDateTime lastViewedAt;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    @ManyToOne
    @JoinColumn(name = "folders_id", nullable = false)
    private Folders folder;

    public FoldersViews(Folders folder, Users users) {
        this.folder = folder;
        this.users = users;
        this.lastViewedAt = LocalDateTime.now();
    }

    public void updateLastViewed() {
        this.lastViewedAt = LocalDateTime.now();
    }
}
