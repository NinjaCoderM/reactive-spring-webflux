package at.codecrafters.moviesInfoService.controller;

import at.codecrafters.moviesInfoService.domain.MovieInfo;
import at.codecrafters.moviesInfoService.service.MovieInfoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movieinfos")
public class MoviesInfoController {

    private final MovieInfoService movieInfoService;

    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.addMovieInfo(movieInfo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year, @RequestParam(value = "name", required = false) String name) {
        if (year != null) {
            if(name != null) {
                return movieInfoService.getMovieInfoByYear(year)
                        .filter(movieInfo -> movieInfo.getName().equals(name));
            }
            return movieInfoService.getMovieInfoByYear(year);
        }
        if (name != null) {
            return movieInfoService.findMovieInfoByName(name);
        }
        return movieInfoService.allMovieInfos();
    }

//    @GetMapping("/{id}")
//    @ResponseStatus(HttpStatus.OK)
//    public Mono<MovieInfo> getMovieInfoById(@PathVariable("id") String id) {
//        return movieInfoService.findMovieInfoById(id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "MovieInfo not found with id: " + id)));
//    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable("id") String id) {
        return movieInfoService.findMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

//    @PutMapping("/{id}")
//    @ResponseStatus(HttpStatus.OK)
//    public Mono<MovieInfo> updateMovies(@RequestBody MovieInfo movieInfo, @PathVariable String id) {
//        return movieInfoService.updateMovieInfo(movieInfo, id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "MovieInfo not found with id: " + id)));
//    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo, @PathVariable String id){
        return movieInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovies(@PathVariable String id) {
        return movieInfoService.deleteMovieInfo(id);
    }

}
