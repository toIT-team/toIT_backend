package com.toit.contents.links;

import com.toit.common.enums.EntityStatus;
import com.toit.contents.links.dto.response.LinksCreateInFoldersResponse;
import com.toit.contents.links.dto.response.LinksGetInFoldersResponse;
import com.toit.contents.shared.linkpreview.LinkPreview;
import com.toit.contents.shared.linkpreview.LinkPreviewExtractor;
import com.toit.folders.FoldersService;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
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


}
