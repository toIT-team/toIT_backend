package com.toit.contents.attachments;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class AttachMentsController {
    private final AttachMentsService attachMentsService;




}
