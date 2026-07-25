package com.toit.admin.dto.response;

import com.toit.admin.Admin;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AdminMemberResponse {

    private final Long adminId;
    private final String email;
    private final String name;
    private final LocalDateTime createdAt;

    public static AdminMemberResponse from(Admin admin) {
        return new AdminMemberResponse(
                admin.getAdminId(),
                admin.getEmail(),
                admin.getName(),
                admin.getCreatedAt()
        );
    }
}
