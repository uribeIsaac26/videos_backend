package com.video.backend.video_backend.service;

import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.repository.VideoRepository;
import com.video.backend.video_backend.util.VideoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoTranscodingService {

    private final VideoRepository videoRepository;

    @Async("transcodingExecutor")
    public void transcode(Path tempPath, Path videoPath, Integer videoId) {
        try {
            boolean alreadyCompatible = isH264Aac(tempPath);
            List<String> ffmpegCmd;

            if (alreadyCompatible) {
                log.info("[video={}] Ya es H.264/AAC, copiando streams sin re-encodear", videoId);
                ffmpegCmd = List.of(
                        "ffmpeg", "-y",
                        "-i", tempPath.toAbsolutePath().toString(),
                        "-c", "copy",
                        "-movflags", "+faststart",
                        videoPath.toAbsolutePath().toString()
                );
            } else {
                log.info("[video={}] Transcodificando a H.264/AAC con preset veryfast", videoId);
                ffmpegCmd = List.of(
                        "ffmpeg", "-y",
                        "-i", tempPath.toAbsolutePath().toString(),
                        "-c:v", "libx264",
                        "-preset", "veryfast",
                        "-threads", "0",
                        "-c:a", "aac",
                        "-movflags", "+faststart",
                        videoPath.toAbsolutePath().toString()
                );
            }

            ProcessBuilder pb = new ProcessBuilder(ffmpegCmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> log.debug("[video={}] ffmpeg: {}", videoId, line));
            }

            boolean finished = process.waitFor(10, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                markError(videoId, "Timeout: la transcodificacion supero los 10 minutos");
                return;
            }

            if (process.exitValue() != 0) {
                markError(videoId, "ffmpeg termino con codigo de error " + process.exitValue());
                return;
            }

            markReady(videoId);
            log.info("[video={}] Transcodificacion completada", videoId);

        } catch (IOException | InterruptedException e) {
            log.error("[video={}] Error durante la transcodificacion: {}", videoId, e.getMessage());
            markError(videoId, e.getMessage());
        } finally {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException e) {
                log.warn("[video={}] No se pudo eliminar el archivo temporal: {}", videoId, tempPath);
            }
        }
    }

    private void markReady(Integer videoId) {
        videoRepository.findById(videoId).ifPresent(v -> {
            v.setStatus(VideoStatus.LISTO);
            videoRepository.save(v);
        });
    }

    public void markRejected(Integer videoId) {
        markError(videoId, "Cola de transcodificacion llena, intente subir el video nuevamente");
    }

    private void markError(Integer videoId, String message) {
        videoRepository.findById(videoId).ifPresent(v -> {
            v.setStatus(VideoStatus.ERROR);
            v.setErrorMessage(message != null && message.length() > 500 ? message.substring(0, 500) : message);
            videoRepository.save(v);
        });
    }

    private boolean isH264Aac(Path filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "quiet",
                    "-show_entries", "stream=codec_name",
                    "-of", "csv=p=0",
                    filePath.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();

            List<String> codecs;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                codecs = reader.lines().map(String::trim).filter(s -> !s.isEmpty()).toList();
            }

            boolean finished = p.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }

            return codecs.stream().anyMatch("h264"::equals) && codecs.stream().anyMatch("aac"::equals);
        } catch (IOException | InterruptedException e) {
            log.warn("No se pudo detectar codec, se asume que necesita transcodificacion: {}", e.getMessage());
            return false;
        }
    }
}
