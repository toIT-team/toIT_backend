package com.toit.noitce;

import com.toit.admin.Admin;
import com.toit.admin.AdminRepository;
import com.toit.exception.schedules.SchedulesNotFoundException;
import com.toit.noitce.dto.request.NoticeCreateRequest;
import com.toit.noitce.dto.request.NoticeDeleteRequest;
import com.toit.noitce.dto.request.NoticeUpdateRequest;
import com.toit.noitce.dto.response.NoticeReadResponse;
import com.toit.noitce.dto.response.NoticeDeleteResponse;
import com.toit.noitce.dto.response.NoticeUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class NoticeService {

    private final AdminRepository adminRepository;
    private final NoticeRepository noticeRepository;

    //공지사항 검증
    public Notice findByNotices(Long noticesId){
        return noticeRepository.findById(noticesId).
                orElseThrow(()-> new SchedulesNotFoundException(
                        "noticesId 가 " + noticesId +"인 해당 공지사항을 찾을 수 없습니다."));

    }

    //공지사항 목록 조회
    public Page<NoticeReadResponse> getNotices(Pageable pageable) {
        return noticeRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(NoticeReadResponse::from);
    }


    // (관리자용) 공지사항 생성
    public void createNotice(Long adminId, NoticeCreateRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice notice = new Notice(
                request.getTitle(),
                request.getContent(),
                admin,
                LocalDateTime.now()
        );

        noticeRepository.save(notice);
    }


    //공지사항 삭제
    public NoticeDeleteResponse deleteNotice(Long adminId, NoticeDeleteRequest request){
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice findNotice = findByNotices(request.getNoticeId());

        noticeRepository.delete(findNotice);
        return new NoticeDeleteResponse(findNotice.getNoticeId());
    }

    // (관리자용) 공지사항 수정
    public NoticeUpdateResponse updateNotice(Long adminId, NoticeUpdateRequest request){
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Notice findNotice = findByNotices(request.getNoticeId());

        findNotice.update(request.getTitle(), request.getContent());


        noticeRepository.save(findNotice);
        return new NoticeUpdateResponse(findNotice.getNoticeId(),findNotice.getTitle(),findNotice.getContent(),findNotice.getUpdatedAt());
    }


}
