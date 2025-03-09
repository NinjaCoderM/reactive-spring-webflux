package at.codecrafters.moviesInfoService.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@Document
public class MovieInfo {
    @Id
    private String movieInfoId;
    @NotBlank(message="movieInfo.name must be present")
    private String name;
    @NotNull
    @Positive(message="movieInfo.year must be a positive value")
    private Integer year;
    private List<String> cast;
    private LocalDate release_date;

    @PersistenceCreator
    public MovieInfo(String movieInfoId, String name, Integer year, List<String> cast, LocalDate release_date) {
        this.movieInfoId = movieInfoId;
        this.name = name;
        this.year = year;
        this.cast = cast;
        this.release_date = release_date;
    }
}
