package com.toit.contents.attachments;

import com.toit.common.enums.AttachMentsType;

import com.toit.contents.shared.ItemsBase;
import com.toit.user.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "attachments")
@Getter
public class AttachMents extends ItemsBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentsId;

    /**
     * 자료형의 형태
     * ENUM형태("FILE", "IMAGE")
     */
    @Enumerated(EnumType.STRING)
    private AttachMentsType attachmentsType;

    /**
     * Users와 N:1 관계 설정
     */
    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

}
