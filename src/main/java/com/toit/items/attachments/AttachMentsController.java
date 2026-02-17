package com.toit.items.attachments;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Items Management", description = "보관함 내부 화면(page) 전용 API")
@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachMentsController {
    private final AttachMentsService attachMentsService;




}
