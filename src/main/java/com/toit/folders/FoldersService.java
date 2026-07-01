package com.toit.folders;

import com.toit.common.enums.EntityStatus;
import com.toit.exception.folders.FoldersNotFoundException;
import com.toit.folders.dto.response.FoldersCreateResponse;
import com.toit.folders.dto.response.FoldersDeleteResponse;
import com.toit.folders.dto.response.FoldersFavoriteResponse;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.folders.dto.response.FoldersUpdateResponse;
import com.toit.common.S3.config.S3Config;
import com.toit.items.attachments.AttachMents;
import com.toit.items.attachments.AttachMentsRepository;
import com.toit.items.links.Links;
import com.toit.items.links.LinksRepository;
import com.toit.items.texts.Texts;
import com.toit.items.texts.TextsRepository;
import com.toit.schedules.Schedules;
import com.toit.schedules.SchedulesRepository;
import com.toit.user.Users;
import com.toit.user.UsersService;
import com.toit.view.pagefolders.dto.response.PageFoldersMemoResponse;
import com.toit.folders.dto.response.RecentFoldersResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FoldersService {
    private final FoldersRepository foldersRepository;
    private final UsersService usersService;
    private final TextsRepository textsRepository;
    private final LinksRepository linksRepository;
    private final AttachMentsRepository attachMentsRepository;
    private final SchedulesRepository schedulesRepository;
    private final S3Config s3Storage;

    /**
     * <h2>Folders 보관함 전체 조회</h2>
     *
     * <p>한 명의 사용자가 접근 가능한 모든 보관함을 조회합니다.</p>
     * @param usersId
     * @return
     */
    public List<FoldersItemResponse> getAllFoldersByUser(Long usersId) {
        usersService.findById(usersId);
        List<FoldersItemResponse> folders = foldersRepository.findByUsers_UsersIdAndStatus(usersId, EntityStatus.ACTIVE)
                .stream()
                .map(FoldersItemResponse::new)
                .sorted(Comparator.comparing(f -> !Boolean.TRUE.equals(f.getIsDefault())))
                .toList();
        return folders;
    }


    /**
     * <h2>하나의 Folders 메모 보기</h2>
     */
    public PageFoldersMemoResponse getOneFoldersMemobByUser(Long usersId, Long foldersId){
        usersService.findById(usersId);
        Folders folders = findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        return new PageFoldersMemoResponse(folders);
    }


    /**
     * <h2>Folders 보관함 하나 생성 비즈니스 로직</h2>
     * @param usersId
     * @param name
     * @param memo
     * @param color
     * @return FoldersCreateResponse
     */
    public FoldersCreateResponse createFolders(Long usersId, String name, String memo, String color, Integer iconIdx) {
        // Users 조회 -> Users 예외 처리
        Users users = usersService.findById(usersId);

        long folderCount = foldersRepository.countByUsers_UsersIdAndStatus(usersId, EntityStatus.ACTIVE);
        if (folderCount >= 100) {
            throw new IllegalStateException("보관함은 최대 100개까지 생성할 수 있습니다.");
        }

        Folders folders = new Folders(
                name,
                memo,
                false,
                color,
                false,
                LocalDateTime.now(),
                users,
                iconIdx
        );
        return new FoldersCreateResponse(foldersRepository.save(folders));
    }

    /**
     * <h2>Folders 보관함 하나 수정</h2>
     * <p>수정은 Folders 전부 값 수정</p>
     */
    public FoldersUpdateResponse updateFolders(Long usersId, Long foldersId, String name, String memo, String color, Integer iconIdx){
        usersService.findById(usersId);
        Folders folders = findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        folders.update(name, memo, color, iconIdx);
        foldersRepository.save(folders);
        return new FoldersUpdateResponse(folders);
    }

    /**
     * <h2>Folders 보관함 하나 삭제</h2>
     * <p>삭제 시 Hard 삭제가 되는 것이 아닌 소프트 삭제 실행</p>
     * <p>응답값은 Folders 하나의 아이템으로 지정</p>
     */
    public FoldersDeleteResponse deleteFolders(Long usersId, Long foldersId){
        usersService.findById(usersId);
        Folders folder = findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        List<Schedules> schedules = schedulesRepository.findByFolders_FoldersIdAndStatus(foldersId, EntityStatus.ACTIVE);
        schedules.forEach(Schedules::detachFolder);
        schedulesRepository.saveAll(schedules);

        List<Texts> texts = textsRepository.findByStorageIdAndStatus(foldersId, EntityStatus.ACTIVE);
        texts.forEach(Texts::softDelete);
        textsRepository.saveAll(texts);

        List<Links> links = linksRepository.findByStorageIdAndStatus(foldersId, EntityStatus.ACTIVE);
        links.forEach(Links::softDelete);
        linksRepository.saveAll(links);

        List<AttachMents> attachMents = attachMentsRepository.findByStorageIdAndStatus(foldersId, EntityStatus.ACTIVE);
        for (AttachMents attachment : attachMents) {
            String objectKey = attachment.getObjectKey();
            attachment.softDelete();
            attachMentsRepository.flush();
            long activeCount = attachMentsRepository.countByObjectKeyAndStatus(objectKey, EntityStatus.ACTIVE);
            if (activeCount == 0) {
                s3Storage.delete(objectKey);
            }
        }
        attachMentsRepository.saveAll(attachMents);

        folder.softDelete();
        return new FoldersDeleteResponse(foldersRepository.save(folder));
    }

    /**
     * <h2>하나의 Folders 사용자와 연관관계 있는지 보기 - 예외 검사</h2>
     * 리턴값은 Folders
     */
    public Folders findByFoldersIdAndUsers_UsersId(Long usersId, Long foldersId){
        Optional<Folders> folders = foldersRepository.findByFoldersIdAndUsers_UsersId(foldersId, usersId);
        if (folders.isPresent()) {
            return folders.get();
        } else {
            throw new FoldersNotFoundException(foldersId + "은 존재하지 않는 보관함입니다.");
        }
    }

    public Folders findById(Long foldersId){
        Optional<Folders> folders = foldersRepository.findById(foldersId);
        if (folders.isPresent()) {
            return folders.get();
        } else {
            throw new FoldersNotFoundException(foldersId + "은 존재하지 않는 보관함입니다.");
        }
    }


    /**
     * <h2>즐겨찾기 토글</h2>
     * <p>요청된 isFavorite 값이 true이면 false로, false이면 true로 전환합니다.</p>
     */
    public FoldersFavoriteResponse toggleFavorite(Long usersId, Long foldersId, Boolean isFavorite) {
        usersService.findById(usersId);
        Folders folders = findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        if (folders.getIsFavorite().equals(isFavorite)) {
            throw new IllegalArgumentException("요청한 isFavorite 값이 현재 상태와 다릅니다.");
        }
        folders.toggleFavorite();
        foldersRepository.save(folders);
        return new FoldersFavoriteResponse(folders);
    }

    /**
     * /page/items 진입 시 Folders.lastViewedAt 업데이트
     */
    public void updateLastViewedAt(Long usersId, Long foldersId) {
        Folders folders = findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        folders.updateLastViewedAt();
        foldersRepository.save(folders);
    }

    /**
     * 최근 본 보관함 최대 4개 (lastViewedAt 내림차순)
     */
    public List<RecentFoldersResponse> getRecentFolders(Long usersId) {
        return foldersRepository
                .findRecentViewedFolders(usersId, EntityStatus.ACTIVE, PageRequest.of(0, 4))
                .stream()
                .map(RecentFoldersResponse::new)
                .toList();
    }

    public List<FoldersItemResponse> searchFolders(Long usersId, String keyword) {
        String k = (keyword == null) ? "" : keyword.trim();
        if (k.isEmpty()) {
            return List.of();
        }
        List<Folders> folders =
                foldersRepository.searchByName(usersId, EntityStatus.ACTIVE, k);

        List<FoldersItemResponse> responses = new ArrayList<>();

        for (Folders folder : folders) {
            responses.add(new FoldersItemResponse(folder));
        }

        return responses;
    }

}
