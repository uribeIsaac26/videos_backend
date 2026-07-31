package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.dto.MangaModel;
import com.video.backend.video_backend.dto.MangaPageResponse;
import com.video.backend.video_backend.dto.MangaSummaryModel;
import com.video.backend.video_backend.dto.MangaTagResponse;
import com.video.backend.video_backend.entity.Manga;
import com.video.backend.video_backend.entity.MangaMetadataValue;
import com.video.backend.video_backend.entity.MangaPage;
import com.video.backend.video_backend.util.TipoMetadatoManga;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", imports = TipoMetadatoManga.class,
        unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MangaMapper {

    @Mapping(target = "portadaUrl", expression = "java(portadaUrl(manga))")
    MangaSummaryModel toSummary(Manga manga);

    @Mapping(target = "portadaUrl", expression = "java(portadaUrl(manga))")
    @Mapping(target = "artists", expression = "java(valoresPorTipo(manga, TipoMetadatoManga.ARTIST))")
    @Mapping(target = "groups", expression = "java(valoresPorTipo(manga, TipoMetadatoManga.GROUP))")
    @Mapping(target = "parodys", expression = "java(valoresPorTipo(manga, TipoMetadatoManga.PARODY))")
    @Mapping(target = "tags", expression = "java(toTagResponses(manga))")
    @Mapping(target = "paginas", expression = "java(toPaginaResponses(manga))")
    MangaModel toModel(Manga manga);

    default String portadaUrl(Manga manga) {
        return "/api/mangas/" + manga.getId() + "/paginas/1/imagen";
    }

    default List<String> valoresPorTipo(Manga manga, TipoMetadatoManga tipo) {
        return manga.getMetadataValues().stream()
                .filter(valor -> valor.getTipo() == tipo)
                .map(MangaMetadataValue::getValor)
                .toList();
    }

    default List<MangaTagResponse> toTagResponses(Manga manga) {
        return manga.getMangaTags().stream()
                .map(mangaTag -> new MangaTagResponse(mangaTag.getTag().getName(), mangaTag.getEsMale(), mangaTag.getEsFemale()))
                .toList();
    }

    default List<MangaPageResponse> toPaginaResponses(Manga manga) {
        return manga.getPaginas().stream()
                .sorted(Comparator.comparing(MangaPage::getOrden))
                .map(pagina -> new MangaPageResponse(
                        pagina.getOrden(),
                        pagina.getAncho(),
                        pagina.getAlto(),
                        "/api/mangas/" + manga.getId() + "/paginas/" + pagina.getOrden() + "/imagen"))
                .toList();
    }
}
