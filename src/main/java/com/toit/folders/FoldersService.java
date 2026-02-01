package com.toit.folders;

import com.toit.folders.dto.request.FoldersAllRequest;
import com.toit.folders.dto.request.FoldersCreateRequest;
import com.toit.folders.dto.response.FoldersAllResponse;
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
     * @param request
     * @return
     */
    public FoldersCreateResponse createFolders(FoldersCreateRequest request) {
        /** Users 조회 -> Users 예외 처리 */
        Users users = usersService.findById(request.getUsersId());

        Folders folders = new Folders(
                request.getName(),
                request.getMemo(),
                false,
                request.getColor(),
                false,
                LocalDateTime.now(),
                users
        );
        return new FoldersCreateResponse(foldersRepository.save(folders));
    }

    public FoldersAllResponse getAllFoldersByUser(FoldersAllRequest request) {
        Long userId = request.getUsersId();
        usersService.findById(userId);
        List<FoldersItemResponse> folders = foldersRepository.findByUsers_UsersId(userId)
                .stream()
                .map(FoldersItemResponse::new)
                .toList();
        return new FoldersAllResponse(userId, folders);
    }
}
