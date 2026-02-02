package com.toit.foldersview;

import com.toit.folders.Folders;
import com.toit.folders.FoldersRepository;
import com.toit.foldersview.dto.request.FoldersViewsRequest;
import com.toit.foldersview.dto.response.FoldersViewsResponse;
import com.toit.foldersview.dto.response.RecentFoldersResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
     * @param usersId
     * @param foldersId
     * @return FoldersViewsResponse
     */
    public FoldersViewsResponse recordFoldersViews(Long usersId, Long foldersId) {
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

    /**
     *  Pageable을 사용해서 최대 4개까지 뽑아서 보내줌
     *  4개는 내림차순으로 정렬해서 보내주게 됨
     *  @param usersId
     */
    public List<RecentFoldersResponse> getRecentFolders(Long usersId) {

        // 사용자 존재 검증
        usersService.findById(usersId);

        return foldersViewsRepository
                .findRecentFoldersByUser(usersId, PageRequest.of(0, 4))
                .stream()
                .map(RecentFoldersResponse::new)
                .toList();
    }
}
