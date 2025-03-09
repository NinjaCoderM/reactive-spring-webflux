package at.codecrafters.moviesInfoService.service;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import at.codecrafters.moviesInfoService.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoService {

    private final MovieInfoRepository movieInfoRepository;

    public MovieInfoService(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo).log();
    }

    public Flux<MovieInfo> allMovieInfos() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> findMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo movieInfoUpdate, String id) {
        return movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setName(movieInfoUpdate.getName());
                    movieInfo.setCast(movieInfoUpdate.getCast());
                    movieInfo.setYear(movieInfoUpdate.getYear());
                    movieInfo.setRelease_date(movieInfoUpdate.getRelease_date());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }
}
