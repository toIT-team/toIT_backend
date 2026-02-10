package com.toit.items;


import com.toit.folders.Folders;
import com.toit.folders.FoldersService;
import com.toit.items.dto.response.ItemsTextCreateResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemsService {
    private final ItemsRepository itemsRepository;
    private final UsersService usersService;
    private final FoldersService foldersService;

    public ItemsTextCreateResponse createFoldersText(Long usersId, List<Long> foldersIdList, String textContent){
        Users users = usersService.findById(usersId);

        for (Long foldersId : foldersIdList) {
            foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId); // 권한/존재 검증
            Items item = Items.createTextInFolder(users, foldersId, textContent);
            itemsRepository.save(item);
        }

        return new ItemsTextCreateResponse(usersId, foldersIdList, textContent);
    }
}
