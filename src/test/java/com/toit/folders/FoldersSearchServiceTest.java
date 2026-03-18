package com.toit.folders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.toit.common.enums.AuthProvider;
import com.toit.common.enums.EntityStatus;
import com.toit.folders.dto.response.FoldersItemResponse;
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
class FoldersSearchServiceTest {

    @InjectMocks
    private FoldersService foldersService;

    @Mock
    private FoldersRepository foldersRepository;

    @Mock
    private UsersService usersService;

    private Users createUser(Long usersId) {
        Users user = new Users(
                "test@toit.com", "tester", "bio",
                AuthProvider.KAKAO, 100L, LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "usersId", usersId);
        return user;
    }

    private Folders createFolder(Users user, Long foldersId, String name) {
        Folders folder = new Folders(name, "메모", false, "pink100", false, LocalDateTime.now(), user);
        ReflectionTestUtils.setField(folder, "foldersId", foldersId);
        return folder;
    }

    @Test
    @DisplayName("한국어 키워드로 보관함을 검색하면 해당 보관함이 반환된다")
    void searchFolders_shouldReturnMatchingFolders_whenKoreanKeyword() {
        Long usersId = 1L;
        String keyword = "여행";
        Users user = createUser(usersId);

        Folders folder = createFolder(user, 10L, "여행 준비");

        when(foldersRepository.searchByName(usersId, EntityStatus.ACTIVE, keyword))
                .thenReturn(List.of(folder));

        List<FoldersItemResponse> result = foldersService.searchFolders(usersId, keyword);

        assertEquals(1, result.size());
        assertEquals("여행 준비", result.get(0).getName());
    }

    @Test
    @DisplayName("키워드와 일치하는 보관함이 없으면 빈 리스트를 반환한다")
    void searchFolders_shouldReturnEmpty_whenNoMatch() {
        Long usersId = 1L;
        String keyword = "없는키워드";

        when(foldersRepository.searchByName(usersId, EntityStatus.ACTIVE, keyword))
                .thenReturn(List.of());

        List<FoldersItemResponse> result = foldersService.searchFolders(usersId, keyword);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("키워드가 null이면 빈 리스트를 반환한다")
    void searchFolders_shouldReturnEmpty_whenKeywordIsNull() {
        List<FoldersItemResponse> result = foldersService.searchFolders(1L, null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("여러 보관함 중 키워드가 포함된 것만 반환된다")
    void searchFolders_shouldReturnOnlyMatching_whenMultipleFolders() {
        Long usersId = 1L;
        String keyword = "여행";
        Users user = createUser(usersId);

        Folders folder1 = createFolder(user, 10L, "여행 준비");
        Folders folder2 = createFolder(user, 11L, "여행지 사진");

        when(foldersRepository.searchByName(usersId, EntityStatus.ACTIVE, keyword))
                .thenReturn(List.of(folder1, folder2));

        List<FoldersItemResponse> result = foldersService.searchFolders(usersId, keyword);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getName().contains(keyword));
        assertTrue(result.get(1).getName().contains(keyword));
    }
}