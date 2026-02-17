package com.toit.items.texts;

import com.toit.common.enums.EntityStatus;
import com.toit.items.texts.dto.response.TextsCreateInFoldersResponse;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import com.toit.folders.FoldersService;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TextsService {
    private final TextsRepository textsRepository;

    private final UsersService usersService;

    private final FoldersService foldersService;

    /**
     * 보관함에 텍스트를 추가하는 비즈니스 로직입니다.
     * @param usersId
     * @param foldersIdList
     * @param textContent
     * @return
     */
    public TextsCreateInFoldersResponse createTextsInFolders(Long usersId, List<Long> foldersIdList, String textContent){
        Users users = usersService.findById(usersId);

        for (Long foldersId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId); // 권한/존재 검증
            Texts texts = Texts.createTextsInFolders(users, foldersId, textContent);
            textsRepository.save(texts);
        }

        return new TextsCreateInFoldersResponse(usersId, foldersIdList, textContent);
    }

    /**
     * 하나의 사용자 폴더 내부 텍스트 조회
     */
    public List<TextsGetInFoldersResponse> getTextsInFolders(Long usersId, Long foldersId) {
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);

        List<Texts> links = textsRepository
                .findTextsInFolders(
                        usersId,
                        foldersId,
                        EntityStatus.ACTIVE
                );

        List<TextsGetInFoldersResponse> result = new ArrayList<>();

        for (Texts item : links) {
            result.add(new TextsGetInFoldersResponse(item));
        }
        return result;
    }

}
