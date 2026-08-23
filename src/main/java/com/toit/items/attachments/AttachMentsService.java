package com.toit.items.attachments;

import com.toit.common.S3.attachmentprocessor.StorageResult;
import com.toit.common.S3.attachmentprocessor.AttachmentPayload;
import com.toit.common.S3.attachmentprocessor.AttachmentProcessor;
import com.toit.common.S3.attachmentprocessor.AttachmentProcessorRouter;
import com.toit.common.S3.attachmentprocessor.S3KeyFactory;
import com.toit.common.cloudfront.CloudFrontSigner;
import com.toit.items.attachments.storage.dto.StorageUsageResponse;
import com.toit.common.S3.attachmentvaildator.AttachmentValidator;
import com.toit.common.S3.config.S3Config;
import com.toit.common.enums.AttachMentsType;
import com.toit.common.enums.EntityStatus;
import com.toit.common.enums.UploadStatus;
import com.toit.items.attachments.exception.AttachmentsNotFoundException;
import com.toit.folders.FoldersService;
import com.toit.items.attachments.dto.request.AttachMentsConfirmRequest;
import com.toit.items.attachments.dto.request.AttachMentsPresignRequest;
import com.toit.items.attachments.dto.response.AttachMentsConfirmResponse;
import com.toit.items.attachments.dto.response.AttachMentsDeleteInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesCreateInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesCreateInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsMoveInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsPresignResponse;
import com.toit.items.attachments.dto.response.AttachMentsUpdateFileNameResponse;
import lombok.extern.slf4j.Slf4j;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    private final CloudFrontSigner cloudFrontSigner;
    private final S3KeyFactory s3KeyFactory;

    private static final int PRESIGN_TTL_SECONDS = 300;

    /**
     * 이미지 저장 메소드
     */
    public List<AttachMentsImagesCreateInFoldersResponse> createImagesInFolders(Long usersId, List<Long> foldersIdList, String textContent, List<MultipartFile> images) {
        Users users = usersService.findById(usersId);
        for (Long folderId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, folderId);
        }

        // 전체 이미지 합산 용량으로 스토리지 검사
        long totalSize = images.stream().mapToLong(MultipartFile::getSize).sum();
        for (MultipartFile image : images) {
            attachmentValidator.validateImageContentType(image.getContentType());
            attachmentValidator.validateFileSize(image.getSize());
        }
        validateStorageLimit(usersId, totalSize);

        AttachmentProcessor processor = processorRouter.getProcessor(AttachMentsType.IMAGE);
        List<AttachMentsImagesCreateInFoldersResponse> responses = new ArrayList<>();

        for (MultipartFile image : images) {
            long recvStart = System.currentTimeMillis();
            AttachmentPayload payload = processor.getSizeWithDimensions(image);
            long receiveMs = System.currentTimeMillis() - recvStart;

            StorageResult result = processor.S3Store(usersId, payload);

            log.info("[이미지 업로드] file={} | receiveMs={}ms | s3Ms={}ms | presignMs={}ms",
                    image.getOriginalFilename(), receiveMs, result.getS3UploadMs(), result.getPresignMs());

            for (Long folderId : foldersIdList) {
                AttachMents entity = AttachMents.createImagesInFolders(
                        users, folderId,
                        result.getObjectKey(), result.getPresignedUrl(),
                        payload.getAttachmentsExtension(), payload.getAttachmentsSize(),
                        payload.getFileName(), textContent,
                        payload.getWidth(), payload.getHeight()
                );
                AttachMents saved = attachMentsRepository.save(entity);
                responses.add(new AttachMentsImagesCreateInFoldersResponse(saved, result.getPresignedUrl()));
            }
        }

        return responses;
    }

    /**
     * 파일 저장 메소드
     */
    public List<AttachMentsFilesCreateInFoldersResponse> createFilesInFolders(Long usersId, List<Long> foldersIdList, String textContent, MultipartFile file) {
        Users users = usersService.findById(usersId);
        for (Long folderId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, folderId);
        }

        attachmentValidator.validateFilesContentType(file.getContentType(), file.getOriginalFilename());
        attachmentValidator.validateFileSize(file.getSize());
        validateStorageLimit(usersId, file.getSize());

        AttachmentProcessor processor = processorRouter.getProcessor(AttachMentsType.FILE);

        long recvStart = System.currentTimeMillis();
        AttachmentPayload payload = processor.getSizeWithDimensions(file);
        long receiveMs = System.currentTimeMillis() - recvStart;

        StorageResult result = processor.S3Store(usersId, payload);

        log.info("[파일 업로드] receiveMs={}ms | s3Ms={}ms | presignMs={}ms",
                receiveMs, result.getS3UploadMs(), result.getPresignMs());

        List<AttachMentsFilesCreateInFoldersResponse> responses = new ArrayList<>();

        for (Long folderId : foldersIdList) {
            AttachMents entity =
                    AttachMents.createFilesInFolders(
                            users,
                            folderId,
                            result.getObjectKey(),
                            result.getPresignedUrl(),
                            payload.getAttachmentsExtension(),
                            payload.getAttachmentsSize(),
                            payload.getFileName(),
                            textContent
                    );

            AttachMents saved = attachMentsRepository.save(entity);
            responses.add(new AttachMentsFilesCreateInFoldersResponse(saved, result.getPresignedUrl()));
        }
        return responses;
    }

    /**
     * 하나의 사용자 폴더 내부 FILES 조회
     */
    public List<AttachMentsFilesGetInFoldersResponse> getFilesInFolders(Long usersId, Long foldersId) {
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        List<AttachMents> attachments = attachMenetsRepository
                .findAttachMentsInFolders(usersId, AttachMentsType.FILE, foldersId, EntityStatus.ACTIVE);

        List<AttachMentsFilesGetInFoldersResponse> attachmentsFilesResponse = new ArrayList<>();
        for (AttachMents a : attachments) {
            // String signedUrl = cloudFrontSigner.generateSignedUrl(a.getObjectKey());
            String signedUrl = s3Storage.presignGetUrl(a.getObjectKey(), java.time.Duration.ofDays(7));
            attachmentsFilesResponse.add(new AttachMentsFilesGetInFoldersResponse(a, signedUrl));
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
                .findAttachMentsInFolders(usersId, AttachMentsType.IMAGE, foldersId, EntityStatus.ACTIVE);

        List<AttachMentsImagesGetInFoldersResponse> attachmentsImagesResponse = new ArrayList<>();
        for (AttachMents a : attachments) {
            // Flutter가 objectKey로 CloudFront URL을 직접 생성한다
            attachmentsImagesResponse.add(new AttachMentsImagesGetInFoldersResponse(a));
        }
        return attachmentsImagesResponse;
    }

    // [측정 전용] S3 presigned URL 방식 이미지 조회 (p50/p95/p99 비교용, 측정 종료로 비활성화)
    // public List<com.toit.items.attachments.dto.response.BenchS3ImageResponse> getImagesInFoldersS3Bench(Long usersId, Long foldersId) {
    //     usersService.findById(usersId);
    //     foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
    //
    //     List<AttachMents> attachments = attachMenetsRepository
    //             .findAttachMentsInFolders(usersId, AttachMentsType.IMAGE, foldersId, EntityStatus.ACTIVE);
    //
    //     List<com.toit.items.attachments.dto.response.BenchS3ImageResponse> res = new ArrayList<>();
    //     for (AttachMents a : attachments) {
    //         String presignedUrl = s3Storage.presignGetUrl(a.getObjectKey(), java.time.Duration.ofDays(7));
    //         res.add(new com.toit.items.attachments.dto.response.BenchS3ImageResponse(a, presignedUrl));
    //     }
    //     return res;
    // }

    /**
     * 이미지 및 파일 삭제
     */
    public AttachMentsDeleteInFoldersResponse deleteAttachments(Long usersId, Long attachmentsId) {
        AttachMents attachment = attachMentsRepository
                .findByAttachmentsIdAndUsers_UsersId(attachmentsId, usersId)
                .orElseThrow(() -> new RuntimeException("첨부파일 없음"));

        String objectKey = attachment.getObjectKey();

        attachment.softDelete();
        attachMentsRepository.flush();

        long activeCount = attachMentsRepository.countByObjectKeyAndStatus(objectKey, EntityStatus.ACTIVE);
        if (activeCount == 0) {
            s3Storage.delete(objectKey);
        }

        return new AttachMentsDeleteInFoldersResponse(attachmentsId);
    }

    public AttachMentsMoveInFoldersResponse moveAttachmentsInFolders(Long usersId, Long foldersId, Long moveFoldersId, Long attachmentsId) {
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        foldersService.findById(moveFoldersId);
        AttachMents attachments = findById(attachmentsId);
        attachments.moveFolders(moveFoldersId);
        attachMentsRepository.save(attachments);
        return new AttachMentsMoveInFoldersResponse(moveFoldersId, foldersId, attachmentsId);
    }

    public AttachMentsUpdateFileNameResponse updateFileName(Long usersId, Long foldersId, Long attachmentsId, String fileName, String textContent) {
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        AttachMents attachments = attachMentsRepository
                .findByAttachmentsIdAndUsers_UsersId(attachmentsId, usersId)
                .orElseThrow(() -> new AttachmentsNotFoundException(attachmentsId + "는 존재하지 않는 이미지/파일입니다."));

        if (attachments.getAttachmentsType() == AttachMentsType.FILE) {
            attachments.updateFileName(fileName);
        }
        attachments.updateTextContent(textContent);
        attachMentsRepository.save(attachments);

        // String signedUrl = cloudFrontSigner.generateSignedUrl(attachments.getObjectKey());
        String signedUrl = s3Storage.presignGetUrl(attachments.getObjectKey(), java.time.Duration.ofDays(7));
        return new AttachMentsUpdateFileNameResponse(attachments, signedUrl);
    }

    /**
     * 파일 검색
     */
    public List<AttachMentsFilesGetInFoldersResponse> searchFiles(Long usersId, String keyword) {
        usersService.findById(usersId);

        String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) return List.of();

        // [성능개선] N+1 제거 - 보관함 이름을 JOIN으로 한 번에 가져와 DTO로 직접 조회.
        // (기존: 행마다 foldersService.findById 호출 = N+1, presigned URL은 목록에서 불필요해 제거)
        return attachMentsRepository.searchFilesWithFolder(usersId, EntityStatus.ACTIVE, AttachMentsType.FILE, k);
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

    /**
     * Flutter S3 직접 업로드용 presigned PUT URL 발급 (단건/다건 통합)
     * IMAGE: 최대 3개, FILE: 1개
     */
    @Transactional
    public List<AttachMentsPresignResponse> presignUpload(Long usersId, AttachMentsPresignRequest request) {
        if (request.getFiles() == null || request.getFiles().isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (request.getAttachmentsType() == AttachMentsType.IMAGE && request.getFiles().size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3개까지 업로드할 수 있습니다.");
        }
        if (request.getAttachmentsType() == AttachMentsType.FILE && request.getFiles().size() > 1) {
            throw new IllegalArgumentException("파일은 1개씩 업로드할 수 있습니다.");
        }

        // 사용자 행을 잠가 같은 사용자의 동시 요청을 직렬화한다.
        // 뒤에 온 요청은 앞선 요청이 커밋한 뒤에야 사용량을 읽으므로 한도를 넘길 수 없다.
        // 이 조회보다 앞에 일반 조회를 넣지 말 것 — 스냅샷이 먼저 확정되어 락이 무력해진다.
        Users users = usersService.findByIdForUpdate(usersId);
        for (Long folderId : request.getFoldersIdList()) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, folderId);
        }

        long totalSize = request.getFiles().stream().mapToLong(f -> f.getFileSize()).sum();
        validateStorageLimit(usersId, totalSize);

        for (var file : request.getFiles()) {
            if (request.getAttachmentsType() == AttachMentsType.IMAGE) {
                attachmentValidator.validateImageContentType(file.getContentType());
            } else {
                attachmentValidator.validateFilesContentType(file.getContentType(), file.getFileName());
            }
            attachmentValidator.validateFileSize(file.getFileSize());
        }

        long totalStart = System.currentTimeMillis();

        List<AttachMentsPresignResponse> responses = new ArrayList<>();
        long s3PresignMs = 0;
        long reserveMs = 0;
        int reservedCount = 0;
        for (var file : request.getFiles()) {
            String ext = extractExtension(file.getFileName());

            long t = System.currentTimeMillis();
            String objectKey = request.getAttachmentsType() == AttachMentsType.IMAGE
                    ? s3KeyFactory.imageKey(usersId, ext)
                    : s3KeyFactory.fileKey(usersId, ext);
            String uploadUrl = s3Storage.presignPutUrl(objectKey, file.getContentType(),
                    java.time.Duration.ofSeconds(PRESIGN_TTL_SECONDS));
            s3PresignMs += System.currentTimeMillis() - t;

            // 용량을 선점하기 위해 예약 행을 남긴다.
            // confirm 이 폴더마다 행을 만들므로 예약도 같은 개수로 맞춰야 용량 계산이 일치한다.
            long r = System.currentTimeMillis();
            for (Long folderId : request.getFoldersIdList()) {
                attachMentsRepository.save(AttachMents.reserve(
                        users,
                        folderId,
                        request.getAttachmentsType(),
                        objectKey,
                        file.getContentType(),
                        (double) file.getFileSize(),
                        file.getFileName(),
                        request.getTextContent()
                ));
                reservedCount++;
            }
            reserveMs += System.currentTimeMillis() - r;

            responses.add(new AttachMentsPresignResponse(objectKey, uploadUrl, PRESIGN_TTL_SECONDS));
        }

        log.info("[presign] total={}ms | s3PresignGenerate={}ms | reserve={}ms | fileCount={} | reservedRows={}",
                System.currentTimeMillis() - totalStart, s3PresignMs, reserveMs,
                request.getFiles().size(), reservedCount);

        return responses;
    }

    /**
     * Flutter S3 직접 업로드 완료 후 DB 저장
     */
    @Transactional
    public List<AttachMentsConfirmResponse> confirmUpload(Long usersId, AttachMentsConfirmRequest request) {
        if (request.getFiles() == null || request.getFiles().isEmpty()) {
            throw new IllegalArgumentException("확인할 파일이 없습니다.");
        }

        usersService.findById(usersId);

        long totalStart = System.currentTimeMillis();
        long dbMs = 0;

        List<AttachMentsConfirmResponse> responses = new ArrayList<>();

        for (var file : request.getFiles()) {
            long t = System.currentTimeMillis();
            // presign 이 만들어 둔 예약 행을 찾아 상태만 전환한다.
            // 폴더는 예약 시점에 정해지므로 confirm 의 foldersIdList 는 사용하지 않는다.
            List<AttachMents> reserved = attachMentsRepository
                    .findByObjectKeyAndUsers_UsersIdAndUploadStatus(
                            file.getObjectKey(), usersId, UploadStatus.PENDING);
            dbMs += System.currentTimeMillis() - t;

            if (reserved.isEmpty()) {
                // presign 없이 호출됐거나, 예약이 만료되어 정리 배치가 회수한 경우
                throw new IllegalStateException(
                        "업로드 예약을 찾을 수 없습니다. 다시 시도해 주세요. objectKey=" + file.getObjectKey());
            }

            for (AttachMents attachments : reserved) {
                attachments.confirm();
                // presign 이후 사용자가 메모를 수정했을 수 있어 확정 시점 값으로 갱신
                attachments.updateTextContent(request.getTextContent());
                responses.add(new AttachMentsConfirmResponse(attachments));
            }
        }

        log.info("[confirm] total={}ms | db={}ms | fileCount={} | confirmedRows={}",
                System.currentTimeMillis() - totalStart, dbMs, request.getFiles().size(), responses.size());

        return responses;
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) return "bin";
        int idx = filename.lastIndexOf('.');
        if (idx == -1 || idx == filename.length() - 1) return "bin";
        return filename.substring(idx + 1).toLowerCase();
    }

    private void validateStorageLimit(Long usersId, long newFileSize) {
        List<Object[]> result = attachMentsRepository.sumAttachmentsSizeByUsersId(usersId, EntityStatus.ACTIVE);
        Object[] row = result.isEmpty() ? new Object[]{0, 0} : result.get(0);
        double usedBytes = ((Number) row[0]).doubleValue() + ((Number) row[1]).doubleValue();
        if (usedBytes + newFileSize > StorageUsageResponse.LIMIT_BYTES) {
            throw new IllegalStateException("스토리지 용량이 초과되었습니다. (최대 5GB)");
        }
    }

    public long countByFoldersId(Long foldersId) {
        return attachMentsRepository.countByStorageIdAndStatus(foldersId, EntityStatus.ACTIVE);
    }

    public AttachMents findById(Long attachmentsId) {
        Optional<AttachMents> attachments = attachMentsRepository.findById(attachmentsId);
        if (attachments.isPresent()) {
            return attachments.get();
        } else {
            throw new AttachmentsNotFoundException(attachments + "는 존재하지 않는 링크입니다.");
        }
    }
}