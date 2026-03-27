package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.entity.Tag;
import lombok.*;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.MediaType;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoModel {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private String videoUrl;
    private Set<TagResponse> tags;
}
