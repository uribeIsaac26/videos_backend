package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.ImageModel;
import com.video.backend.video_backend.dto.ImageRequest;
import com.video.backend.video_backend.entity.Image;
import com.video.backend.video_backend.excepcion.ImageNotFoundException;
import com.video.backend.video_backend.mapper.ImageMapper;
import com.video.backend.video_backend.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    @Transactional
    public Page<ImageModel> findAll(Pageable pageable) {
        return imageRepository.findAll(pageable).map(imageMapper::toModel);
    }

    public Resource findImageById(Integer id) {
        Image image = imageRepository.findById(id).orElseThrow(ImageNotFoundException::new);
        Path imagePath = Paths.get(mediaBasePath)
                .resolve(image.getImagePath())
                .normalize();
        try {
            Resource resource = new UrlResource(imagePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ImageNotFoundException();
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void deleteImage(Integer id) throws IOException {
        Image image = imageRepository.findById(id).orElseThrow(ImageNotFoundException::new);

        Path imagePath = Paths.get(mediaBasePath, image.getImagePath());
        Files.deleteIfExists(imagePath);

        log.info("Imagen eliminada");

        imageRepository.deleteById(id);
    }

    public ImageModel uploadImage(String title, MultipartFile imageFile) {
        imageValidations(imageFile);

        String imageId = UUID.randomUUID().toString();

        String originalFilename = imageFile.getOriginalFilename();
        String originalExtension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";

        String imageFileName = imageId + originalExtension;

        Path imageDirectory = Paths.get(mediaBasePath, "images");
        Path imagePath = imageDirectory.resolve(imageFileName).normalize();

        try {
            Files.createDirectories(imageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de imagenes", e);
        }

        try (InputStream inputStream = imageFile.getInputStream()) {
            Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo de la imagen", e);
        }

        ImageRequest imageRequest = ImageRequest.builder()
                .title(title)
                .imageFileName(imageFileName)
                .size(imageFile.getSize())
                .contentType(imageFile.getContentType())
                .build();

        return saveImage(imageRequest, imagePath);
    }

    private void imageValidations(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es obligatorio");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen valida");
        }
    }

    private ImageModel saveImage(ImageRequest imageRequest, Path imagePath) {
        try {
            Image entity = imageMapper.toEntity(imageRequest);
            Image image = imageRepository.save(entity);
            return imageMapper.toModel(image);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException ex) {
                log.error("Error guardando imagen en db, no se pudo borrar el archivo");
            }
            throw new RuntimeException("Error guardando imagen en la db", e);
        }
    }

    public Page<ImageModel> findByAllTags(List<Integer> tagIds, Pageable pageable) {
        Long tagCount = (long) tagIds.size();
        return imageRepository.findByAllTags(tagIds, tagCount, pageable)
                .map(imageMapper::toModel);
    }
}
