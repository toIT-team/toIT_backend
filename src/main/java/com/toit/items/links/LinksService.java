package com.toit.items.links;

import com.toit.common.enums.EntityStatus;
import com.toit.exception.items.links.LinksNotFoundException;
import com.toit.exception.items.texts.TextsNotFoundException;
import com.toit.items.links.dto.response.LinksCreateInFoldersResponse;
import com.toit.items.links.dto.response.LinksDeleteInFoldersResponse;
import com.toit.items.links.dto.response.LinksGetInFoldersResponse;
import com.toit.items.links.dto.response.LinksMoveInFoldersResponse;
import com.toit.items.shared.linkpreview.LinkPreview;
import com.toit.items.shared.linkpreview.LinkPreviewExtractor;
import com.toit.folders.FoldersService;
import com.toit.items.texts.Texts;
import com.toit.items.texts.dto.response.TextsDeleteInFoldersResponse;
import com.toit.items.texts.dto.response.TextsMoveInFoldersResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinksService {
    private final FoldersService foldersService;

    private final UsersService usersService;

    private final LinksRepository linksRepository;

    private final LinkPreviewExtractor linkPreviewExtractor;


    /**
     * 보관함에 링크를 추가하는 비즈니스 로직입니다.
     * @param usersId
     * @param foldersIdList
     * @param linksUrl
     * @return
     */
    public LinksCreateInFoldersResponse createLinksInFolders(Long usersId, List<Long> foldersIdList, String linksUrl){
        Users users = usersService.findById(usersId);

        LinkPreview preview = linkPreviewExtractor.extract(linksUrl);
        String linksName = preview.getTitle();
        String textContent = preview.getDescription();
        String linksThumbnail = preview.getThumbnailUrl();
        System.out.println("===== LinkPreview =====");
        System.out.println("resolvedUrl = " + preview.getResolvedUrl());
        System.out.println("title       = " + preview.getTitle());
        System.out.println("description = " + preview.getDescription());
        System.out.println("thumbnail   = " + preview.getThumbnailUrl());
        System.out.println("=======================");

        for (Long foldersId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId); // 권한/존재 검증

            Links item = Links.createLinksInFolders(
                    users,
                    linksName,
                    linksUrl,
                    linksThumbnail,
                    foldersId,
                    textContent
            );
            linksRepository.save(item);
        }


        return new LinksCreateInFoldersResponse(usersId, foldersIdList, linksUrl, preview.getDescription(), preview.getThumbnailUrl(),  preview.getTitle());
    }

    /**
     * 하나의 사용자 폴더 내부 링크 조회
     */
    public List<LinksGetInFoldersResponse> getLinksInFolders(Long usersId, Long foldersId) {

        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        // 링크만 조회
        List<Links> links = linksRepository
                .findLinksInFolders(
                        usersId,
                        foldersId,
                        EntityStatus.ACTIVE
                );

        List<LinksGetInFoldersResponse> result = new ArrayList<>();

        for (Links link : links) {
            result.add(new LinksGetInFoldersResponse(link));
        }
        return result;
    }

    /**
     * 링크 삭제(소프트 삭제)
     */
    public LinksDeleteInFoldersResponse deleteLinksInFolders(Long usersId, Long foldersId, Long linksId){
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        Links links = findById(linksId);
        links.softDelete();
        linksRepository.save(links);
        return new LinksDeleteInFoldersResponse(links);
    }

    /**
     * 링크 보관함 이동
     */
    public LinksMoveInFoldersResponse moveLinksInFolders(Long usersId, Long foldersId, Long moveFoldersId, Long linksId){
        usersService.findById(usersId);
        foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);
        foldersService.findById(moveFoldersId);
        Links links = findById(linksId);
        links.moveFolders(moveFoldersId);
        linksRepository.save(links);
        return new LinksMoveInFoldersResponse(links);
    }


    /**
     * 링크 찾기
     * @param linksId
     * @return
     */
    public Links findById(Long linksId){
        Optional<Links> links = linksRepository.findById(linksId);
        if (links.isPresent()) {
            return links.get();
        } else {
            throw new LinksNotFoundException(linksId + "는 존재하지 않는 링크입니다.");
        }
    }


}
