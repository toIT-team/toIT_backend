package com.toit.texts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.toit.common.enums.AuthProvider;
import com.toit.common.enums.EntityStatus;
import com.toit.folders.FoldersService;
import com.toit.items.texts.Texts;
import com.toit.items.texts.TextsRepository;
import com.toit.items.texts.TextsService;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TextsServiceTest {

    @InjectMocks
    private TextsService textsService;

    @Mock
    private TextsRepository textsRepository;

    @Mock
    private UsersService usersService;

    @Mock
    private FoldersService foldersService;

    @Test
    @DisplayName("한국어 키워드로 검색하면 해당 텍스트가 반환된다")
    void searchTexts_shouldReturnMatchingTexts_whenKoreanKeyword() {
        Long usersId = 1L;
        String keyword = "여행";

        Users user = new Users(
                "test@toit.com", "tester", "bio",
                AuthProvider.KAKAO, 100L, LocalDateTime.now()
        );

        Texts texts = Texts.createTextsInFolders(user, 10L, "여행 준비물 목록");
        ReflectionTestUtils.setField(texts, "textsId", 1L);

        when(textsRepository.searchByTextContent(usersId, EntityStatus.ACTIVE, keyword))
                .thenReturn(List.of(texts));

        List<TextsGetInFoldersResponse> result = textsService.searchTexts(usersId, keyword);

        assertEquals(1, result.size());
        assertEquals("여행 준비물 목록", result.get(0).getTextContent());
    }

    @Test
    @DisplayName("키워드와 일치하는 텍스트가 없으면 빈 리스트를 반환한다")
    void searchTexts_shouldReturnEmpty_whenNoMatch() {
        Long usersId = 1L;
        String keyword = "없는키워드";

        when(textsRepository.searchByTextContent(usersId, EntityStatus.ACTIVE, keyword))
                .thenReturn(List.of());

        List<TextsGetInFoldersResponse> result = textsService.searchTexts(usersId, keyword);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("키워드가 null이면 빈 리스트를 반환한다")
    void searchTexts_shouldReturnEmpty_whenKeywordIsNull() {
        List<TextsGetInFoldersResponse> result = textsService.searchTexts(1L, null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("여러 텍스트 중 키워드가 포함된 것만 반환된다")
    void searchTexts_shouldReturnOnlyMatching_whenMultipleTexts() {
        Long usersId = 1L;
        String keyword = "여행";

        Users user = new Users(
                "test@toit.com", "tester", "bio",
                AuthProvider.KAKAO, 100L, LocalDateTime.now()
        );

        Texts matching1 = Texts.createTextsInFolders(user, 10L, "여행 준비물 목록");
        Texts matching2 = Texts.createTextsInFolders(user, 10L, "여행지 추천");
        ReflectionTestUtils.setField(matching1, "textsId", 1L);
        ReflectionTestUtils.setField(matching2, "textsId", 2L);

        when(textsRepository.searchByTextContent(usersId, EntityStatus.ACTIVE, keyword))
                .thenReturn(List.of(matching1, matching2));

        List<TextsGetInFoldersResponse> result = textsService.searchTexts(usersId, keyword);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getTextContent().contains(keyword));
        assertTrue(result.get(1).getTextContent().contains(keyword));
    }
}