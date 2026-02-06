package com.toit.folders;

import com.toit.folders.dto.response.FoldersCreateResponse;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.folders.dto.response.FoldersUpdateResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import com.toit.view.folders.dto.response.PageFoldersMemoResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoldersService {
    private final FoldersRepository foldersRepository;

    private final UsersService usersService;


    /**
     * <h2>Folders 보관함 하나 생성 비즈니스 로직</h2>
     * @param usersId
     * @param name
     * @param memo
     * @param color
     * @return FoldersCreateResponse
     */
    public FoldersCreateResponse createFolders(Long usersId, String name, String memo, String color) {
        // Users 조회 -> Users 예외 처리
        Users users = usersService.findById(usersId);

        Folders folders = new Folders(
                name,
                memo,
                false,
                color,
                false,
                LocalDateTime.now(),
                users
        );
        return new FoldersCreateResponse(foldersRepository.save(folders));
    }

    /**
     *
     */
    public FoldersUpdateResponse updateFolders(Long usersId, Long foldersId, String name, String memo, String color, Integer iconIdx){
        usersService.findById(usersId);
        Folders folders = findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        folders.update(name, memo, color, iconIdx);
        foldersRepository.save(folders);
        return new FoldersUpdateResponse(folders, usersId);
    }


    /**
     * <h2>Folders 보관함 전체 조회</h2>
     *
     * <p>한 명의 사용자가 접근 가능한 모든 보관함을 조회합니다.</p>
     * @param usersId
     * @return
     */
    public List<FoldersItemResponse> getAllFoldersByUser(Long usersId) {
        usersService.findById(usersId);
        List<FoldersItemResponse> folders = foldersRepository.findByUsers_UsersId(usersId)
                .stream()
                .map(FoldersItemResponse::new)
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
     * <h2>하나의 Folders 사용자와 연관관계 있는지 보기 - 예외 검사</h2>
     * 리턴값은 Folders
     */
    public Folders findByFoldersIdAndUsers_UsersId(Long usersId, Long foldersId){
        Optional<Folders> folders = foldersRepository.findByFoldersIdAndUsers_UsersId(foldersId, usersId);
        if (folders.isPresent()) {
            return folders.get();
        } else {
            throw new IllegalArgumentException("존재하지 않는 보관함입니다. foldersId=" + foldersId);
        }
    }

}
