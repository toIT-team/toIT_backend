package com.toit.contents.attachments;


import com.toit.common.enums.EntityStatus;
import com.toit.folders.FoldersService;
import com.toit.contents.attachments.dto.response.AttachMentsFoldersImagesResponse;
import com.toit.contents.attachments.dto.response.ItemsFoldersInFilesResponse;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachMentsService {
    private final AttachMentsRepository attachMenetsRepository;
    private final UsersService usersService;
    private final FoldersService foldersService;

    /**
     * 하나의 사용자 폴더 내부 이미지 조회
     */
    public List<AttachMentsFoldersImagesResponse> getFoldersImages(Long usersId, Long foldersId) {

        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        List<AttachMents> links = attachMenetsRepository
                .findImagesInFolders(
                        usersId,
                        foldersId,
                        EntityStatus.ACTIVE
                );

        List<AttachMentsFoldersImagesResponse> result = new ArrayList<>();

        for (AttachMents attachments : links) {
            result.add(new AttachMentsFoldersImagesResponse(attachments));
        }
        return result;
    }

    /**
     * 하나의 사용자 폴더 내부 파일 조회
     */
    public List<ItemsFoldersInFilesResponse> getFoldersFiles(Long usersId, Long foldersId) {

        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        // 링크만 조회
        List<AttachMents> links = attachMenetsRepository
                .findFoldersItems(
                        usersId,
                        StorageTarget.FOLDERS,
                        foldersId,
                        ItemsType.FILE,
                        EntityStatus.ACTIVE
                );

        List<ItemsFoldersInFilesResponse> result = new ArrayList<>();

        for (AttachMents item : links) {
            result.add(new ItemsFoldersInFilesResponse(item));
        }
        return result;
    }
}
