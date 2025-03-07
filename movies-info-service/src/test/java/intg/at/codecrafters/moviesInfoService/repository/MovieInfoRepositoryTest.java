package at.codecrafters.moviesInfoService.repository;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Testcontainers
@DataMongoTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieInfoRepositoryTest {

    @Autowired private MovieInfoRepository movieInfoRepository;

    private String remId;

    //@Container
    @ServiceConnection
    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.5"));

    @Test
    @DisplayName("MongoDB container is created and running")
    @Order(1)
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

    @Order(2)
    @Test
    @DisplayName("Test findAll Method of MovieInfoRepository")
    void findAll() {
        Flux<MovieInfo> all =   movieInfoRepository.findAll().log();
        StepVerifier.create(all)
                .assertNext(Assertions::assertNotNull)
                .thenCancel()
                .verify();
        remId = Objects.requireNonNull(all.blockFirst()).getMovieInfoId();
        System.out.println("ID stored " + remId);
    }

    @Test
    @Order(3)
    @DisplayName("Test findById Method of MovieInfoRepository")
    void findById() {
        System.out.println("ID used " + remId);
        Mono<MovieInfo> movieInfoMono =   movieInfoRepository.findById(remId).log();
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> Assertions.assertEquals(remId, movieInfo.getMovieInfoId(), "remId should match " + remId))
                .verifyComplete();

    }

    @Test
    @Order(4)
    @DisplayName("Test save Method of MovieInfoRepository")
    void save() {
        var movieInfo = new MovieInfo(null, "Batman Begins xxx", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        Mono<MovieInfo> responseMovieInfo = movieInfoRepository.save(movieInfo).log();
        StepVerifier.create(responseMovieInfo)
                .assertNext(responseMovieInf -> Assertions.assertEquals(movieInfo.getName(), responseMovieInf.getName(), "Name should match " + movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    @Order(5)
    @DisplayName("Test update Method of MovieInfoRepository")
    void update() {
        System.out.println("ID used " + remId);
        Mono<MovieInfo> movieInfoMono =   movieInfoRepository.findById(remId).log();
        var movieInfo = movieInfoMono.block();
        String oldName = movieInfo.getName();
        System.out.println("oldName " + oldName);
        movieInfo.setName("New Name");
        Mono<MovieInfo> responseMovieInfo = movieInfoRepository.save(movieInfo).log();
        StepVerifier.create(responseMovieInfo)
                .assertNext(responseMovieInf -> Assertions.assertEquals(movieInfo.getName(), responseMovieInf.getName(), "Name should match " + movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    @Order(6)
    @DisplayName("Test delete Method of MovieInfoRepository")
    void delete() {
        System.out.println("ID used " + remId);
        Mono<MovieInfo> movieInfoMono =   movieInfoRepository.findById(remId).log();
        var movieInfo = movieInfoMono.block();

        movieInfoRepository.deleteById(Objects.requireNonNull(movieInfo).getMovieInfoId()).block();

        StepVerifier.create(movieInfoRepository.findById(remId).log())
                .verifyComplete();
    }
}


