package com.toit.feedback;

import com.toit.feedback.dto.request.FeedbackCreateRequest;
import com.toit.feedback.dto.response.FeedbackCreateResponse;
import com.toit.feedback.dto.response.FeedbackListResponse;
import com.toit.user.Users;
import com.toit.user.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UsersRepository usersRepository;

    public FeedbackCreateResponse create(Long usersId, FeedbackCreateRequest request) {
        Users user = usersRepository.findById(usersId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Feedback feedback = new Feedback(request.title(), request.content(), user);
        feedbackRepository.save(feedback);

        return new FeedbackCreateResponse(feedback.getFeedbackId());
    }

    public Page<FeedbackListResponse> getList(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(FeedbackListResponse::from);
    }
}
