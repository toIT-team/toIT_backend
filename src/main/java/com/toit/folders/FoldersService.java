package com.toit.folders;

import com.toit.folders.dto.request.FoldersCreateRequest;
import com.toit.folders.dto.response.FoldersCreateResponse;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.time.LocalDateTime;
import java.util.List;
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
}
