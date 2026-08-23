package com.toit.pagesearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toit.user.exception.UsersNotFoundException;
import com.toit.folders.FoldersService;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.items.attachments.AttachMentsService;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.links.LinksService;
import com.toit.items.links.dto.response.LinksGetInFoldersResponse;
import com.toit.items.texts.TextsService;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import com.toit.schedules.SchedulesService;
import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.user.UsersService;
import com.toit.view.pagesearch.PageSearchUseCaseImpl;
import com.toit.view.pagesearch.dto.response.PageSearchResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PageSearchUseCaseImplTest {

    @InjectMocks
    private PageSearchUseCaseImpl pageSearchUseCase;

    @Mock
    private FoldersService foldersService;

    @Mock
    private UsersService usersService;

    @Mock
    private SchedulesService schedulesService;

    @Mock
    private TextsService textsService;

    @Mock
    private LinksService linksService;

    @Mock
    private AttachMentsService attachMentsService;

    @Test
    @DisplayName("키워드가 null이면 빈 결과를 반환한다")
    void search_shouldReturnEmpty_whenKeywordIsNull() {
        PageSearchResponse response = pageSearchUseCase.search(1L, null);

        assertTrue(response.getFolders().isEmpty());
        assertTrue(response.getSchedules().isEmpty());
        assertTrue(response.getLinks().isEmpty());
        assertTrue(response.getTexts().isEmpty());
//        assertTrue(response.getImages().isEmpty());
        assertTrue(response.getFiles().isEmpty());

        verify(usersService, never()).findById(1L);
    }

    @Test
    @DisplayName("키워드가 빈 문자열이면 빈 결과를 반환한다")
    void search_shouldReturnEmpty_whenKeywordIsBlank() {
        PageSearchResponse response = pageSearchUseCase.search(1L, "   ");

        assertTrue(response.getFolders().isEmpty());
        assertTrue(response.getSchedules().isEmpty());
        assertTrue(response.getLinks().isEmpty());
        assertTrue(response.getTexts().isEmpty());
//        assertTrue(response.getImages().isEmpty());
        assertTrue(response.getFiles().isEmpty());

        verify(usersService, never()).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 검색하면 예외가 발생한다")
    void search_shouldThrowException_whenUserNotFound() {
        Long invalidUsersId = 999L;
        String keyword = "여행";

        when(usersService.findById(invalidUsersId))
                .thenThrow(new UsersNotFoundException(invalidUsersId + "은 존재하지 않는 사용자입니다."));

        assertThrows(UsersNotFoundException.class,
                () -> pageSearchUseCase.search(invalidUsersId, keyword));
    }

    @Test
    @DisplayName("키워드로 검색하면 각 카테고리에 결과가 담겨 반환된다")
    void search_shouldReturnResults_whenKeywordMatches() {
        Long usersId = 1L;
        String keyword = "여행";

        when(foldersService.searchFolders(usersId, keyword))
                .thenReturn(List.of(org.mockito.Mockito.mock(FoldersItemResponse.class)));
        when(schedulesService.searchSchedules(usersId, keyword))
                .thenReturn(List.of(org.mockito.Mockito.mock(ScheduleViewResponse.class)));
        when(linksService.searchLinks(usersId, keyword))
                .thenReturn(List.of(org.mockito.Mockito.mock(LinksGetInFoldersResponse.class)));
        when(textsService.searchTexts(usersId, keyword))
                .thenReturn(List.of(org.mockito.Mockito.mock(TextsGetInFoldersResponse.class)));
//        when(attachMentsService.searchImages(usersId, keyword))
//                .thenReturn(List.of(org.mockito.Mockito.mock(AttachMentsImagesGetInFoldersResponse.class)));
        when(attachMentsService.searchFiles(usersId, keyword))
                .thenReturn(List.of(org.mockito.Mockito.mock(AttachMentsFilesGetInFoldersResponse.class)));

        PageSearchResponse response = pageSearchUseCase.search(usersId, keyword);

        assertEquals(1, response.getFolders().size());
        assertEquals(1, response.getSchedules().size());
        assertEquals(1, response.getLinks().size());
        assertEquals(1, response.getTexts().size());
//        assertEquals(1, response.getImages().size());
        assertEquals(1, response.getFiles().size());
    }

    @Test
    @DisplayName("검색 결과가 없으면 각 카테고리가 빈 리스트로 반환된다")
    void search_shouldReturnEmptyLists_whenNoResultsFound() {
        Long usersId = 1L;
        String keyword = "없는키워드";

        when(foldersService.searchFolders(usersId, keyword)).thenReturn(List.of());
        when(schedulesService.searchSchedules(usersId, keyword)).thenReturn(List.of());
        when(linksService.searchLinks(usersId, keyword)).thenReturn(List.of());
        when(textsService.searchTexts(usersId, keyword)).thenReturn(List.of());
//        when(attachMentsService.searchImages(usersId, keyword)).thenReturn(List.of());
        when(attachMentsService.searchFiles(usersId, keyword)).thenReturn(List.of());

        PageSearchResponse response = pageSearchUseCase.search(usersId, keyword);

        assertTrue(response.getFolders().isEmpty());
        assertTrue(response.getSchedules().isEmpty());
        assertTrue(response.getLinks().isEmpty());
        assertTrue(response.getTexts().isEmpty());
//        assertTrue(response.getImages().isEmpty());
        assertTrue(response.getFiles().isEmpty());
    }
}
