package com.toit.foldersview.dto.response;

import com.toit.foldersview.FoldersViews;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentFoldersResponse {

    private Long folderId;
    private String name;
    private LocalDateTime lastViewedAt;

    public RecentFoldersResponse(FoldersViews views) {
        this.folderId = views.getFolder().getFoldersId();
        this.name = views.getFolder().getName();
        this.lastViewedAt = views.getLastViewedAt();
    }
}
