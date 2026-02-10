package com.toit.items;


import com.toit.folders.FoldersService;
import com.toit.items.dto.response.ItemsTextCreateResponse;
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
     * @param textContent -> 링크 본문
     * @param linkThumbnail -> 링크 썸네일
     * @return
     */
    public ItemsTextCreateResponse createFoldersLink(Long usersId, List<Long> foldersIdList, String filePath, String textContent, String linkThumbnail){
        Users users = usersService.findById(usersId);

        for (Long foldersId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId); // 권한/존재 검증
            Items item = Items.createLinkInFolder(users, foldersId, textContent, filePath, linkThumbnail);

            /**
             * textContent의 썸네일 경로 추출 코드 필요
             */

            itemsRepository.save(item);
        }


        return new ItemsTextCreateResponse(usersId, foldersIdList, textContent);
    }
}
