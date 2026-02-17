package com.toit.items.attachments;


import com.toit.common.enums.AttachMentsType;
import com.toit.common.enums.EntityStatus;
import com.toit.folders.FoldersService;
import com.toit.items.attachments.dto.response.AttachMentsImagesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
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
     * 하나의 사용자 폴더 내부 FILES 조회
     */
    public List<AttachMentsFilesGetInFoldersResponse> getFilesInFolders(Long usersId, Long foldersId) {

        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        // 링크만 조회
        List<AttachMents> attachments = attachMenetsRepository
                .findAttachMentsInFolders(
                        usersId,
                        AttachMentsType.FILE,
                        foldersId,
                        EntityStatus.ACTIVE
                );

        List<AttachMentsFilesGetInFoldersResponse> attachmentsFilesResponse = new ArrayList<>();

        for (AttachMents attachmentsfiles : attachments) {
            attachmentsFilesResponse.add(new AttachMentsFilesGetInFoldersResponse(attachmentsfiles));
        }
        return attachmentsFilesResponse;
    }

    /**
     * 하나의 사용자 폴더 내부 IMAGE 조회
     */
    public List<AttachMentsImagesGetInFoldersResponse> getImagesInFolders(Long usersId, Long foldersId) {

        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        List<AttachMents> attachments = attachMenetsRepository
                .findAttachMentsInFolders(
                        usersId,
                        AttachMentsType.IMAGE,
                        foldersId,
                        EntityStatus.ACTIVE
                );

        List<AttachMentsImagesGetInFoldersResponse> attachmentsImagesResponse = new ArrayList<>();

        for (AttachMents attachmentsimages : attachments) {
            attachmentsImagesResponse.add(new AttachMentsImagesGetInFoldersResponse(attachmentsimages));
        }
        return attachmentsImagesResponse;
    }


}
