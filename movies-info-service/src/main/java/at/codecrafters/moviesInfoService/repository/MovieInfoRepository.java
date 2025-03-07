package at.codecrafters.moviesInfoService.repository;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {
}
