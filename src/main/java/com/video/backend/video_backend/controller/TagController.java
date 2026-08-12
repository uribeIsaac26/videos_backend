package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.TagRequest;
import com.video.backend.video_backend.dto.TagResponse;
import com.video.backend.video_backend.service.TagService;
import com.video.backend.video_backend.util.TagType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<Page<TagResponse>> findAll(@RequestParam TagType type, Pageable pageable){
        return ResponseEntity.ok(tagService.findAll(type, pageable));
    }

    @PostMapping
    public ResponseEntity<TagResponse> save(@RequestBody TagRequest tagRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.save(tagRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> findById(@PathVariable Integer id){
        return ResponseEntity.ok(tagService.findById(id));
    }

    @GetMapping("/by-names")
    public ResponseEntity<List<TagResponse>> findTagsByNombre(@RequestParam(name = "tags") List<String> tags, @RequestParam TagType type){
        return ResponseEntity.ok(tagService.findByName(tags, type));
    }
}
