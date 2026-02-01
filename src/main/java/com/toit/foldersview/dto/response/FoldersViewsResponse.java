package com.toit.foldersview.dto.response;

import com.toit.foldersview.FoldersViews;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersViewsResponse {
    private Long foldersViewsId;
    private Long usersId;
    private Long foldersId;
    private LocalDateTime lastViewedAt;

    public FoldersViewsResponse(FoldersViews views) {
        this.foldersViewsId = views.getFoldersViewsId();
        this.usersId = views.getUsers().getUsersId();
        this.foldersId = views.getFolder().getFoldersId();
        this.lastViewedAt = views.getLastViewedAt();
    }
}
