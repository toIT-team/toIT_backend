package com.toit.items.attachments;

import com.toit.common.S3.attachmentprocessor.StorageResult;
import com.toit.common.S3.attachmentprocessor.AttachmentPayload;
import com.toit.common.S3.attachmentprocessor.AttachmentProcessor;
import com.toit.common.S3.attachmentprocessor.AttachmentProcessorRouter;
import com.toit.common.S3.attachmentvaildator.AttachmentValidator;
import com.toit.common.S3.config.S3Config;
import com.toit.common.enums.AttachMentsType;
import com.toit.common.enums.EntityStatus;
import com.toit.exception.items.attachments.AttachmentFileNameUpdateNotAllowedException;
import com.toit.exception.items.attachments.AttachmentsNotFoundException;
import com.toit.folders.FoldersService;
import com.toit.items.attachments.dto.response.AttachMentsDeleteInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesCreateInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesCreateInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsMoveInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsUpdateFileNameResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachMentsService {
    private final AttachMentsRepository attachMenetsRepository;
    private final UsersService usersService;
    private final FoldersService foldersService;

    private final AttachmentValidator attachmentValidator;
    private final AttachmentProcessorRouter processorRouter;

    private final AttachMentsRepository attachMentsRepository;

    private final S3Config s3Storage;



    /**
     * 이미지 저장 메소드
     * @param usersId
     * @param foldersIdList
     * @param image
     * @return
     */
    public List<AttachMentsImagesCreateInFoldersResponse> createImagesInFolders(Long usersId, List<Long> foldersIdList, String textContent, MultipartFile image) {
        Users users = usersService.findById(usersId);
        for (Long folderId : foldersIdList){
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, folderId);
        }

        /* 확장자 검사 */
        attachmentValidator.validateImageContentType(image.getContentType());

        /** IMAGE 타입 Processor 가져오기 */
        AttachmentProcessor processor = processorRouter.getProcessor(AttachMentsType.IMAGE);

        /** 파일 처리 (bytes + width + height + 확장자 추출) */
        AttachmentPayload payload = processor.getSizeWithDimensions(image);

        /**
         * 이미지 똑같은 걸 여러 개의 폴더에 저장할 필요 없음 그래서 하나만 저장하고 DB에는 다 같은 걸로 진행
         */
        StorageResult result = processor.S3Store(usersId, payload);

        List<AttachMentsImagesCreateInFoldersResponse> responses = new ArrayList<>();


        for (Long folderId : foldersIdList) {

            AttachMents entity =
                    AttachMents.createImagesInFolders(
                            users,
                            folderId,
                            result.getObjectKey(),
                            result.getPresignedUrl(),
                            payload.getAttachmentsExtension(),
                            payload.getAttachmentsSize(),
                            payload.getFileName(),
                            textContent,
                            payload.getWidth(),
                            payload.getHeight()
                    );

            AttachMents saved = attachMentsRepository.save(entity);

            responses.add(
                    new AttachMentsImagesCreateInFoldersResponse(saved)
            );
        }

        return responses;
    }

    /**
     * 파일 저장 메소드
     * @param usersId
     * @param foldersIdList
     * @param textContent
     * @param file
     * @return
     */
    public List<AttachMentsFilesCreateInFoldersResponse> createFilesInFolders(Long usersId, List<Long> foldersIdList, String textContent, MultipartFile file) {
        Users users = usersService.findById(usersId);
        for (Long folderId : foldersIdList){
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, folderId);
        }

        /* 확장자 검사 */
        attachmentValidator.validateFilesContentType(file.getContentType());

        /** File 타입 Processor 가져오기 */
        AttachmentProcessor processor = processorRouter.getProcessor(AttachMentsType.FILE);

        /** 파일 처리 (bytes + width + height + 확장자 추출) */
        AttachmentPayload payload = processor.getSizeWithDimensions(file);

        /**
         * 파일 똑같은 걸 여러 개의 폴더에 저장할 필요 없음 그래서 하나만 저장하고 DB에는 다 같은 걸로 진행
         */
        StorageResult result = processor.S3Store(usersId, payload);

        List<AttachMentsFilesCreateInFoldersResponse> responses = new ArrayList<>();

        for (Long folderId : foldersIdList) {
            AttachMents entity =
                    AttachMents.createImagesInFolders(
                            users,
                            folderId,
                            result.getObjectKey(),
                            result.getPresignedUrl(),
                            payload.getAttachmentsExtension(),
                            payload.getAttachmentsSize(),
                            payload.getFileName(),
                            textContent,
                            payload.getWidth(),
                            payload.getHeight()
                    );

            AttachMents saved = attachMentsRepository.save(entity);

            responses.add(
                    new AttachMentsFilesCreateInFoldersResponse(saved)
            );
        }
        return responses;
    }

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

    /**
     * 이미지 및 파일 삭제
     */
    public AttachMentsDeleteInFoldersResponse deleteAttachments(Long usersId, Long attachmentsId) {

        AttachMents attachment = attachMentsRepository
                .findByAttachmentsIdAndUsers_UsersId(attachmentsId, usersId)
                .orElseThrow(() -> new RuntimeException("첨부파일 없음"));

        String objectKey = attachment.getObjectKey();

        /**
         * DB에서 소프트 삭제
         */
        attachment.softDelete();
        attachMentsRepository.flush();

        /**
         * 같은 objectKey를 참조하는 ACTIVE 레코드가 남아있나?
         */
        long activeCount = attachMentsRepository.countByObjectKeyAndStatus(objectKey, EntityStatus.ACTIVE);

        /**
         *  아무도 안 쓰면 S3에서도 삭제
         */
        if (activeCount == 0) {
            s3Storage.delete(objectKey);
        }

        return new AttachMentsDeleteInFoldersResponse(usersId, attachmentsId);
    }

    public AttachMentsMoveInFoldersResponse moveAttachmentsInFolders(Long usersId, Long foldersId, Long moveFoldersId, Long attachmentsId){
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        foldersService.findById(moveFoldersId);
        AttachMents attachments = findById(attachmentsId);
        attachments.moveFolders(moveFoldersId);
        attachMentsRepository.save(attachments);
        return new AttachMentsMoveInFoldersResponse(usersId, foldersId, moveFoldersId, attachmentsId);
    }

    public AttachMentsUpdateFileNameResponse updateFileName(Long usersId, Long foldersId, Long attachmentsId, String fileName) {
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        AttachMents attachments = attachMentsRepository
                .findByAttachmentsIdAndUsers_UsersId(attachmentsId, usersId)
                .orElseThrow(() -> new AttachmentsNotFoundException(attachmentsId + "는 존재하지 않는 이미지/파일입니다."));

        if (attachments.getAttachmentsType() != AttachMentsType.FILE) {
            throw new AttachmentFileNameUpdateNotAllowedException("attachmentsType이 FILE인 경우에만 fileName을 수정할 수 있습니다.");
        }

        attachments.updateFileName(fileName);
        attachMentsRepository.save(attachments);
        return new AttachMentsUpdateFileNameResponse(attachments);
    }


    /**
     * 파일 검색
     */
    public List<AttachMentsFilesGetInFoldersResponse> searchFiles(Long usersId, String keyword) {
        usersService.findById(usersId);

        String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) return List.of();

        List<AttachMents> files = attachMentsRepository.searchByTypeAndFileName(usersId, EntityStatus.ACTIVE, AttachMentsType.FILE, k);

        List<AttachMentsFilesGetInFoldersResponse> res = new ArrayList<>(files.size());
        for (AttachMents a : files) {
            res.add(new AttachMentsFilesGetInFoldersResponse(a));
        }
        return res;
    }

    /**
     * 이미지 검색
     */
    public List<AttachMentsImagesGetInFoldersResponse> searchImages(Long usersId, String keyword) {
        usersService.findById(usersId);

        String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) return List.of();

        List<AttachMents> images = attachMentsRepository.searchByTypeAndFileName(usersId, EntityStatus.ACTIVE, AttachMentsType.IMAGE, k);

        List<AttachMentsImagesGetInFoldersResponse> res = new ArrayList<>(images.size());
        for (AttachMents a : images) {
            res.add(new AttachMentsImagesGetInFoldersResponse(a));
        }
        return res;
    }



    public long countByFoldersId(Long foldersId) {
        return attachMentsRepository.countByStorageIdAndStatus(foldersId, EntityStatus.ACTIVE);
    }

    public AttachMents findById(Long attachmentsId){
        Optional<AttachMents> attachments = attachMentsRepository.findById(attachmentsId);
        if (attachments.isPresent()) {
            return attachments.get();
        } else {
            throw new AttachmentsNotFoundException(attachments + "는 존재하지 않는 링크입니다.");
        }
    }

}
