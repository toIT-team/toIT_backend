package com.toit.items.attachments.dto.response;

// [측정 전용] S3 presigned URL 방식 이미지 조회 응답 (p50/p95/p99 비교용, 측정 종료로 비활성화)
// import com.toit.items.attachments.AttachMents;
// import lombok.Getter;
//
// /**
//  * [측정 전용] S3 presigned URL 방식 이미지 조회 응답.
//  * CloudFront 전환 전/후 응답시간(p50/p95/p99) 비교용. 측정 종료 후 삭제 예정.
//  */
// @Getter
// public class BenchS3ImageResponse {
//     private final Long attachmentsId;
//     private final String objectKey;
//     private final String presignedUrl;
//     private final Double attachmentsSize;
//     private final String fileName;
//
//     public BenchS3ImageResponse(AttachMents attachments, String presignedUrl) {
//         this.attachmentsId = attachments.getAttachmentsId();
//         this.objectKey = attachments.getObjectKey();
//         this.presignedUrl = presignedUrl;
//         this.attachmentsSize = attachments.getAttachmentsSize();
//         this.fileName = attachments.getFileName();
//     }
// }
