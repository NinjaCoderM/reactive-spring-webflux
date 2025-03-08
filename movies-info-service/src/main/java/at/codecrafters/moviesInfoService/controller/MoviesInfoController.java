package at.codecrafters.moviesInfoService.controller;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import at.codecrafters.moviesInfoService.service.MovieInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movieinfos")
public class MoviesInfoController {

    private MovieInfoService movieInfoService;

    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody MovieInfo movieInfo) {
        return movieInfoService.addMovieInfo(movieInfo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<MovieInfo> getAllMovieInfos() {
        return movieInfoService.allMovieInfos();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<MovieInfo> getMovieInfoById(@PathVariable("id") String id) {
        return movieInfoService.findMovieInfoById(id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "MovieInfo not found with id: " + id)));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<MovieInfo> updateMovies(@RequestBody MovieInfo movieInfo, @PathVariable String id) {
        return movieInfoService.updateMovieInfo(movieInfo, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovies(@PathVariable String id) {
        return movieInfoService.deleteMovieInfo(id);
    }

}
