package at.codecrafters.moviesInfoService.repository;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;


@DataMongoTest( properties = "de.flapdoodle.mongodb.embedded.version=5.0.5")
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class MovieInfoRepositoryTest {

    @Autowired private MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null,"The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo(null,"Dark Knight Rises", 2008, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        movieInfoRepository.saveAll(movieInfos)
                .blockLast(); // nur bei Tests erlaubt
    }

    @Test
    void findAll() {
        Flux<MovieInfo> all =   movieInfoRepository.findAll().log();
        StepVerifier.create(all)
                .assertNext(Assertions::assertNotNull)
                .thenCancel()
                .verify();

    }
}


