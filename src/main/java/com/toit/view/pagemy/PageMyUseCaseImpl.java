package com.toit.view.pagemy;

import com.toit.user.Users;
import com.toit.user.UsersService;
import com.toit.view.pagemy.dto.response.PageMyViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageMyUseCaseImpl implements PageMyUseCase {

    private final UsersService usersService;

    @Override
    public PageMyViewResponse getMyView(Long usersId) {
        Users user = usersService.findById(usersId);
        return new PageMyViewResponse(user);
    }
}
