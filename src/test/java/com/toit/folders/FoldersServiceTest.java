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
import com.toit.folders.dto.response.FoldersCreateResponse;
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
        Long usersId = 1L;
        String name = "여행";
        String memo = "여행 준비물";
        String color = "pink100";

        Users user = new Users(
                "test@toit.com",
                "tester",
                "bio",
                AuthProvider.KAKAO,
                100L,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "usersId", usersId);

        Folders savedFolder = new Folders(
                name,
                memo,
                false,
                color,
                false,
                LocalDateTime.now(),
                user
        );
        ReflectionTestUtils.setField(savedFolder, "foldersId", 10L);

        when(usersService.findById(usersId)).thenReturn(user);
        when(foldersRepository.save(any(Folders.class))).thenReturn(savedFolder);

        FoldersCreateResponse response = foldersService.createFolders(usersId, name, memo, color);

        verify(usersService).findById(usersId);
        verify(foldersRepository).save(any(Folders.class));
        assertEquals(usersId, response.getUsersId());
        assertEquals(10L, response.getFoldersId());
        assertEquals(name, response.getName());
        assertEquals(memo, response.getMemo());
        assertEquals(color, response.getColor());
        assertFalse(response.getIsDefault());
        assertFalse(response.getIsFavorite());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("보관함 생성 시 존재하지 않는 사용자면 예외를 던진다")
    void createFolders_shouldThrowException_whenUserNotFound() {
        Long invalidUsersId = 999L;

        when(usersService.findById(invalidUsersId))
                .thenThrow(new UsersNotFoundException(invalidUsersId + "은 존재하지 않는 사용자입니다."));

        assertThrows(UsersNotFoundException.class,
                () -> foldersService.createFolders(invalidUsersId, "여행", "메모", "pink100"));

        verify(foldersRepository, never()).save(any(Folders.class));
    }
}
