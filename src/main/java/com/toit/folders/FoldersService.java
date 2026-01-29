package com.toit.folders;

import com.toit.folders.dto.request.FoldersCreateRequest;
import com.toit.folders.dto.response.FoldersCreateResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoldersService {
    private final FoldersRepository foldersRepository;

    private final UsersService usersService;

    public FoldersCreateResponse createFolders(FoldersCreateRequest request) {
        /** Users 조회 -> Users 예외 처리 */
        Users users = usersService.findById(request.getUsersId());

        Folders folders = new Folders(
                request.getName(),
                request.getMemo(),
                false,
                request.getColor(),
                false,
                users
        );
        return new FoldersCreateResponse(foldersRepository.save(folders));
    }
}
