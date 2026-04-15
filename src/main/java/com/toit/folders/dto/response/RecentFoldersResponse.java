package com.toit.folders.dto.response;

import com.toit.folders.Folders;
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

    public RecentFoldersResponse(Folders folders) {
        this.folderId = folders.getFoldersId();
        this.name = folders.getName();
        this.lastViewedAt = folders.getLastViewedAt();
    }
}