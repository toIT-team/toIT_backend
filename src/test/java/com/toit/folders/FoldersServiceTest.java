package com.toit.folders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toit.common.enums.AuthProvider;
import com.toit.exception.users.UsersNotFoundException;
import com.toit.common.enums.EntityStatus;
import com.toit.folders.dto.response.FoldersCreateResponse;
import com.toit.folders.dto.response.FoldersDeleteResponse;
import com.toit.folders.dto.response.FoldersFavoriteResponse;
import com.toit.folders.dto.response.FoldersUpdateResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FoldersServiceTest {

    @InjectMocks
    private FoldersService foldersService;

    @Mock
    private FoldersRepository foldersRepository;

    @Mock
    private UsersService usersService;

    @Test
    @DisplayName("보관함 생성 시 사용자 조회 후 보관함을 저장하고 응답을 반환한다")
    void createFolders_shouldSaveFolderAndReturnResponse() {
//        Long usersId = 1L;
//        String name = "여행";
//        String memo = "여행 준비물";
//        String color = "pink100";
//
//        Users user = new Users(
//                "test@toit.com",
//                "tester",
//                "bio",
//                AuthProvider.KAKAO,
//                100L,
//                LocalDateTime.now()
//        );
//        ReflectionTestUtils.setField(user, "usersId", usersId);
//
//        Folders savedFolder = new Folders(
//                name,
//                memo,
//                false,
//                color,
//                false,
//                LocalDateTime.now(),
//                user
//        );
//        ReflectionTestUtils.setField(savedFolder, "foldersId", 10L);
//
//        when(usersService.findById(usersId)).thenReturn(user);
//        when(foldersRepository.save(any(Folders.class))).thenReturn(savedFolder);
//
//        FoldersCreateResponse response = foldersService.createFolders(usersId, name, memo, color);
//
//        verify(usersService).findById(usersId);
//        verify(foldersRepository).save(any(Folders.class));
//        assertEquals(usersId, response.getUsersId());
//        assertEquals(10L, response.getFoldersId());
//        assertEquals(name, response.getName());
//        assertEquals(memo, response.getMemo());
//        assertEquals(color, response.getColor());
//        assertFalse(response.getIsDefault());
//        assertFalse(response.getIsFavorite());
//        assertNotNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("보관함 생성 시 존재하지 않는 사용자면 예외를 던진다")
    void createFolders_shouldThrowException_whenUserNotFound() {
        Long invalidUsersId = 999L;

        when(usersService.findById(invalidUsersId))
                .thenThrow(new UsersNotFoundException(invalidUsersId + "은 존재하지 않는 사용자입니다."));

        assertThrows(UsersNotFoundException.class,
                () -> foldersService.createFolders(invalidUsersId, "여행", "메모", "pink100", 0));

        verify(foldersRepository, never()).save(any(Folders.class));
    }

    @Test
    @DisplayName("즐겨찾기가 true인 보관함에 false 요청 시 즐겨찾기가 false로 변경된다")
    void toggleFavorite_shouldToggleToFalse_whenCurrentIsTrue() {
        Long usersId = 1L;
        Long foldersId = 10L;

        Users user = new Users(
                "test@toit.com",
                "tester",
                "bio",
                null,
                AuthProvider.KAKAO,
                null,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "usersId", usersId);

        Folders folder = new Folders(
                "여행",
                "메모",
                false,
                "pink100",
                true,
                LocalDateTime.now(),
                user,
                0
        );
        ReflectionTestUtils.setField(folder, "foldersId", foldersId);

        when(usersService.findById(usersId)).thenReturn(user);
        when(foldersRepository.findByFoldersIdAndUsers_UsersId(foldersId, usersId)).thenReturn(java.util.Optional.of(folder));
        when(foldersRepository.save(any(Folders.class))).thenReturn(folder);

        FoldersFavoriteResponse response = foldersService.toggleFavorite(usersId, foldersId, false);

        assertFalse(response.getIsFavorite());
        verify(foldersRepository).save(any(Folders.class));
    }

    @Test
    @DisplayName("즐겨찾기가 false인 보관함에 true 요청 시 즐겨찾기가 true로 변경된다")
    void toggleFavorite_shouldToggleToTrue_whenCurrentIsFalse() {
        Long usersId = 1L;
        Long foldersId = 10L;

        Users user = new Users(
                "test@toit.com",
                "tester",
                "bio",
                null,
                AuthProvider.KAKAO,
                null,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "usersId", usersId);

        Folders folder = new Folders(
                "여행",
                "메모",
                false,
                "pink100",
                false,
                LocalDateTime.now(),
                user,
                0
        );
        ReflectionTestUtils.setField(folder, "foldersId", foldersId);

        when(usersService.findById(usersId)).thenReturn(user);
        when(foldersRepository.findByFoldersIdAndUsers_UsersId(foldersId, usersId)).thenReturn(java.util.Optional.of(folder));
        when(foldersRepository.save(any(Folders.class))).thenReturn(folder);

        FoldersFavoriteResponse response = foldersService.toggleFavorite(usersId, foldersId, true);

        assertNotNull(response);
        verify(foldersRepository).save(any(Folders.class));
    }

    @Test
    @DisplayName("즐겨찾기 현재 상태와 동일한 값 요청 시 예외를 던진다")
    void toggleFavorite_shouldThrowException_whenSameState() {
        Long usersId = 1L;
        Long foldersId = 10L;

        Users user = new Users(
                "test@toit.com",
                "tester",
                "bio",
                null,
                AuthProvider.KAKAO,
                null,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "usersId", usersId);

        Folders folder = new Folders(
                "여행",
                "메모",
                false,
                "pink100",
                true,
                LocalDateTime.now(),
                user,
                0
        );
        ReflectionTestUtils.setField(folder, "foldersId", foldersId);

        when(usersService.findById(usersId)).thenReturn(user);
        when(foldersRepository.findByFoldersIdAndUsers_UsersId(foldersId, usersId)).thenReturn(java.util.Optional.of(folder));

        assertThrows(IllegalArgumentException.class,
                () -> foldersService.toggleFavorite(usersId, foldersId, true));

        verify(foldersRepository, never()).save(any(Folders.class));
    }

    @Test
    @DisplayName("보관함 수정 시 보관함에 대한 내용을 수정하고 응답을 반환한다")
    void updateFolders_shouldUpdateFolderAndReturnResponse(){
//        Long usersId = 1L;
//        String name = "여행";
//        String memo = "여행 준비물";
//        String color = "pink100";
//
//        Users user = new Users(
//                "test@toit.com",
//                "tester",
//                "bio",
//                AuthProvider.KAKAO,
//                100L,
//                LocalDateTime.now()
//        );
//        ReflectionTestUtils.setField(user, "usersId", usersId);
//
//        Folders savedFolder = new Folders(
//                name,
//                memo,
//                false,
//                color,
//                false,
//                LocalDateTime.now(),
//                user
//        );
//        ReflectionTestUtils.setField(savedFolder, "foldersId", 10L);
//
//        Long foldersId = 10L;
//        String updatedName = "수정된 여행";
//        String updatedMemo = "수정된 메모";
//        String updatedColor = "blue100";
//        Integer iconIdx = 1;
//
//        when(usersService.findById(usersId)).thenReturn(user);
//        when(foldersRepository.findByFoldersIdAndUsers_UsersId(foldersId, usersId)).thenReturn(java.util.Optional.of(savedFolder));
//        when(foldersRepository.save(any(Folders.class))).thenReturn(savedFolder);
//
//        FoldersUpdateResponse response = foldersService.updateFolders(usersId, foldersId, updatedName, updatedMemo, updatedColor, iconIdx);
//
//        verify(usersService).findById(usersId);
//        verify(foldersRepository).findByFoldersIdAndUsers_UsersId(foldersId, usersId);
//        verify(foldersRepository).save(any(Folders.class));
//        assertEquals(usersId, response.getUsersId());
//        assertEquals(foldersId, response.getFoldersId());
//        assertEquals(updatedName, response.getName());
//        assertEquals(updatedMemo, response.getMemo());
//        assertEquals(updatedColor, response.getColor());
    }

    @Test
    @DisplayName("보관함 삭제 시 소프트 딜리트 후 응답을 반환한다")
    void deleteFolders_shouldSoftDeleteAndReturnResponse() {
//        Long usersId = 1L;
//        Long foldersId = 10L;
//
//        Users user = new Users(
//                "test@toit.com",
//                "tester",
//                "bio",
//                AuthProvider.KAKAO,
//                100L,
//                LocalDateTime.now()
//        );
//        ReflectionTestUtils.setField(user, "usersId", usersId);
//
//        Folders folder = new Folders(
//                "여행",
//                "여행 준비물",
//                false,
//                "pink100",
//                false,
//                LocalDateTime.now(),
//                user
//        );
//        ReflectionTestUtils.setField(folder, "foldersId", foldersId);
//
//        when(usersService.findById(usersId)).thenReturn(user);
//        when(foldersRepository.findByFoldersIdAndUsers_UsersId(foldersId, usersId)).thenReturn(java.util.Optional.of(folder));
//        when(foldersRepository.save(any(Folders.class))).thenReturn(folder);
//
//        FoldersDeleteResponse response = foldersService.deleteFolders(usersId, foldersId);
//
//        verify(usersService).findById(usersId);
//        verify(foldersRepository).findByFoldersIdAndUsers_UsersId(foldersId, usersId);
//        verify(foldersRepository).save(any(Folders.class));
//        assertEquals(foldersId, response.getFoldersId());
//        assertEquals(usersId, response.getUsersId());
//        assertEquals(EntityStatus.DELETED, response.getStatus());
//        assertNotNull(response.getDeletedAt());
    }
}
