package com.toit.folders.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoldersAllResponse {
    private Long userId;
    private List<FoldersItemResponse> folders;

    public FoldersAllResponse(Long userId, List<FoldersItemResponse> folders) {
        this.userId = userId;
        this.folders = folders;
    }
}
