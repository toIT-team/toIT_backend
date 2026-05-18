package com.toit.items.attachments.dto.request;

import com.toit.common.enums.AttachMentsType;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttachMentsPresignRequest {
    private List<Long> foldersIdList;
    private AttachMentsType attachmentsType;
    private String textContent;
    private List<AttachMentsPresignBatchFileItem> files;
}
