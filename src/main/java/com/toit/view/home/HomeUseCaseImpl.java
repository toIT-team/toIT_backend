package com.toit.view.home;

import com.toit.folders.FoldersService;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.foldersview.FoldersViewsService;
import com.toit.foldersview.dto.response.RecentFoldersResponse;
import com.toit.view.home.dto.request.HomeViewRequest;
import com.toit.view.home.dto.response.HomeViewResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeUseCaseImpl implements HomeUseCase {

    private final FoldersViewsService foldersViewsService;
    private final FoldersService foldersService;

    @Override
    public HomeViewResponse getHomeView(Long usersId) {
        List<RecentFoldersResponse> foldersViews = foldersViewsService.getRecentFolders(usersId);
        List<FoldersItemResponse> folders = foldersService.getAllFoldersByUser(usersId);

        return new HomeViewResponse(usersId, folders, foldersViews);
    }
}