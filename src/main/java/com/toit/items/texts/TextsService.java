package com.toit.items.texts;

import com.toit.common.enums.EntityStatus;
import com.toit.exception.folders.FoldersNotFoundException;
import com.toit.exception.items.texts.TextsNotFoundException;
import com.toit.folders.Folders;
import com.toit.items.links.Links;
import com.toit.items.links.dto.response.LinksGetInFoldersResponse;
import com.toit.items.texts.dto.response.TextsCreateInFoldersResponse;
import com.toit.items.texts.dto.response.TextsDeleteInFoldersResponse;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import com.toit.folders.FoldersService;
import com.toit.items.texts.dto.response.TextsMoveInFoldersResponse;
import com.toit.items.texts.dto.response.TextsUpdateResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            result.add(new TextsGetInFoldersResponse(item, usersId));
        }
        return result;
    }

    /**
     * 하나의 사용자 폴더 내부 텍스트 삭제
     */
    public TextsDeleteInFoldersResponse deleteTextsInFolders(Long usersId, Long foldersId, Long textsId){
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        Texts texts = findById(textsId);
        texts.softDelete();
        textsRepository.save(texts);
        return new TextsDeleteInFoldersResponse(texts);
    }


    public TextsMoveInFoldersResponse moveTextsInFolders(Long usersId, Long foldersId, Long moveFoldersId, Long textsId){
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        foldersService.findById(moveFoldersId);
        Texts texts = findById(textsId);
        texts.moveFolders(moveFoldersId);
        textsRepository.save(texts);
        return new TextsMoveInFoldersResponse(texts);
    }

    public TextsUpdateResponse updateTexts(Long usersId, Long foldersId, Long textsId, String textContent){
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        Texts texts = findById(textsId);
        texts.updateTextContent(textContent);
        textsRepository.save(texts);
        return new TextsUpdateResponse(texts);
    }

    /**
     * 검색
     */
    public List<TextsGetInFoldersResponse> searchTexts(Long usersId, String keyword){
        String k = (keyword == null) ? "" : keyword.trim();
        if (k.isEmpty()) {
            return List.of();
        }
        List<Texts> texts =
                textsRepository.searchByTextContent(usersId, EntityStatus.ACTIVE, k);

        List<TextsGetInFoldersResponse> responses = new ArrayList<>();

        for (Texts text : texts) {
            responses.add(new TextsGetInFoldersResponse(text, usersId));
        }
        return responses;
    }

    public long countByFoldersId(Long foldersId) {
        return textsRepository.countByStorageIdAndStatus(foldersId, EntityStatus.ACTIVE);
    }

    /**
     * 텍스트 검색
     * @param textsId
     * @return
     */
    public Texts findById(Long textsId){
        Optional<Texts> texts = textsRepository.findById(textsId);
        if (texts.isPresent()) {
            return texts.get();
        } else {
            throw new TextsNotFoundException(textsId + "는 존재하지 않는 노트입니다.");
        }
    }

}
