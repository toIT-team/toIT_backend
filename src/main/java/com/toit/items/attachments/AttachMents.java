package com.toit.items.attachments;

import com.toit.common.enums.AttachMentsType;

import com.toit.items.shared.ItemsBase;
import com.toit.user.Users;
import jakarta.persistence.Column;
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
     * 같이 씀 - 파일 및 이미지 오브젝트 키(S3)
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String objectKey;

    /**
     * 같이 씀 - 파일 및 이미지 미리보기 및 다운로드 경로
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String presignedUrl;

    /**
     * 같이 씀 - 파일 및 이미지 확장자
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String attachmentsExtension;

    /**
     * 같이 씀 - 파일 및 이미지 DATA 크기
     */
    @Column(nullable = false)
    private Double attachmentsSize;

    /**
     * attachments_type이 FILE일 경우 - 파일의 이름
     * 길이 제한 255
     */
    private String fileName;

    /**
     * attachments_type이 IMAGE인 경우 - 이미지의 넓이
     */
    private Double imagesWidth;

    /**
     * attachments_type이 IMAGE인 경우 - 이미지의 높이
     */
    private Double imagesHeight;


    /**
     * Users와 N:1 관계 설정
     */
    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

}
