package com.toit.items;


import com.toit.folders.FoldersService;
import com.toit.items.dto.response.ItemsTextCreateResponse;
import com.toit.items.dto.response.itemsLinkCreateResponse;
import com.toit.items.linkpreview.LinkPreview;
import com.toit.items.linkpreview.LinkPreviewExtractor;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemsService {
    private final ItemsRepository itemsRepository;
    private final UsersService usersService;
    private final FoldersService foldersService;
    private final LinkPreviewExtractor linkPreviewExtractor;
    /**
     * 보관함에 텍스트를 추가하는 비즈니스 로직입니다.
     * 데이터로는 items의 컬럼 textContent에만 텍스트 내용이 들어갑니다.
     * @param usersId
     * @param foldersIdList
     * @param textContent
     * @return
     */
    public ItemsTextCreateResponse createFoldersText(Long usersId, List<Long> foldersIdList, String textContent){
        Users users = usersService.findById(usersId);

        for (Long foldersId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId); // 권한/존재 검증
            Items item = Items.createTextInFolder(users, foldersId, textContent);
            itemsRepository.save(item);
        }

        return new ItemsTextCreateResponse(usersId, foldersIdList, textContent);
    }

    /**
     * 보관함에 링크를 추가하는 비즈니스 로직입니다.
     * 데이터로는 items의 컬럼 name에 제목, textContent에 링크 주소, filaPath에 링크 주소가 들어가게 됩니다.
     * @param usersId
     * @param foldersIdList
     * @param filePath -> 링크 URL
     * @return
     */
    public itemsLinkCreateResponse createFoldersLink(Long usersId, List<Long> foldersIdList, String filePath){
        Users users = usersService.findById(usersId);
        /**
         * textContent의 썸네일 경로 추출 코드 필요
         */
        LinkPreview preview = linkPreviewExtractor.extract(filePath);
        String name = preview.getTitle();
        String textContent = preview.getDescription();
        String linkThumbnail = preview.getThumbnailUrl();
        System.out.println("===== LinkPreview =====");
        System.out.println("resolvedUrl = " + preview.getResolvedUrl());
        System.out.println("title       = " + preview.getTitle());
        System.out.println("description = " + preview.getDescription());
        System.out.println("thumbnail   = " + preview.getThumbnailUrl());
        System.out.println("=======================");

        for (Long foldersId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId); // 권한/존재 검증
            // textContent가 URL이라고 가정
            Items item = Items.createLinkInFolder(
                    users,
                    foldersId,
                    filePath, // 링크 URL
                    textContent, // 링크 본문
                    linkThumbnail, // 링크 썸네일
                    name // 링크 제목
            );
            itemsRepository.save(item);
        }


        return new itemsLinkCreateResponse(usersId, foldersIdList, filePath, preview.getDescription(), preview.getThumbnailUrl(),  preview.getTitle());
    }
}
