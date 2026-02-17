package com.toit.items.attachments.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class AttachMentsImagesCreateReqeust {
    /** 사용자 ID */
    private Long usersId;

    /** 보관함 ID - 저장할 보관함 ID, 여러 개의 보관함이 선택될 수 있음*/
    private List<Long> foldersIdList;

    /** 텍스트 내용 */
    private String textContent;

    private MultipartFile image;

    public AttachMentsImagesCreateReqeust(Long usersId, List<Long> foldersIdList, String textContent, MultipartFile image){
        this.usersId = usersId;
        this.foldersIdList = foldersIdList;
        this.textContent = textContent;
        this.image = image;
    }

}
