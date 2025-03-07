package at.codecrafters.moviesInfoService.repository;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

@Testcontainers
@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryTest {

    @Autowired private MovieInfoRepository movieInfoRepository;

    //@Container
    @ServiceConnection
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.5"));

    @Test
    @DisplayName("MongoDB container is created and running")
    void testContainerIsRunning(){
        Assertions.assertTrue(mongoDBContainer.isCreated(), "MongoDB container is not created");
        Assertions.assertTrue(mongoDBContainer.isRunning(), "MongoDB container is not running");

    }

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


