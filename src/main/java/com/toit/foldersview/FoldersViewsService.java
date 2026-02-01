package com.toit.foldersview;

import com.toit.folders.Folders;
import com.toit.folders.FoldersRepository;
import com.toit.foldersview.dto.request.FoldersViewsRequest;
import com.toit.foldersview.dto.response.FoldersViewsResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoldersViewsService {

    private final FoldersViewsRepository foldersViewsRepository;
    private final UsersService usersService;
    private final FoldersRepository foldersRepository;

    /**
     * 사용자가 본 폴더가 이미 리소스가 있으면 본 시간 업데이트
     * 사용자가 처음 본 폴더이면 리소스 생성
     * @param request
     */
    public FoldersViewsResponse recordFoldersViews(FoldersViewsRequest request) {
        Long usersId = request.getUsersId();
        Long foldersId = request.getFoldersId();
        Users user = usersService.findById(usersId);
        Folders folder = foldersRepository.findById(foldersId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다. folderId=" + foldersId));

        Optional<FoldersViews> optionalViews =
                foldersViewsRepository.findByUsers_UsersIdAndFolder_FoldersId(usersId, foldersId);

        FoldersViews result;

        if (optionalViews.isPresent()) {
            result = optionalViews.get();
            result.updateLastViewed(); // 더티체킹이 일어남
        } else {
            result = foldersViewsRepository.save(new FoldersViews(folder, user));
        }
        return new FoldersViewsResponse(result);
    }
}
