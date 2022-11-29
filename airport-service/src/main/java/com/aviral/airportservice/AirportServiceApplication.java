package com.aviral.airportservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class AirportServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AirportServiceApplication.class, args);
  }

  @Bean
  WebClient webClient() {
    return WebClient.create("https://avwx.rest/api/station");
  }

  @Bean
  CommandLineRunner loadData(AirportService service, AirportRepository repo) {
    return args -> {
      repo.deleteAll()
          .thenMany(Flux.just("KSTL", "KSUS", "KCPS", "KALN", "KBLV"))
          .map(service::retrieveAirport)
          .flatMap(repo::saveAll)
          .subscribe(System.out::println);
    };
  }
}

@RestController
@RequestMapping("/")
class AirportController {
  private final AirportService service;

  @Value("${test.message:constant}")
  private String message;

  AirportController(AirportService service) {
    this.service = service;
  }

  @GetMapping("/hello")
  public final Mono<String> hello() {
    return Mono.just("Hello, " + message + "\n");
  }

  @GetMapping
  public final Flux<Airport> airports() {
    return service.getAllAirports().log();
  }

  @GetMapping("/list")
  public final Flux<String> airportList() {
    return service.getAllAirports().map(ap -> ap.icao() + ", " + ap.name() + "\n").log();
  }

  @GetMapping("/airport/{id}")
  public final Mono<Airport> airportById(@PathVariable String id) {
    return service.getAirportById(id).log();
  }
}

@Service
class AirportService {
  @Value("${avwx.token:No Valid Token}")
  private String token;

  private final WebClient webClient;
  private final AirportRepository airportRepository;

  AirportService(WebClient webClient, AirportRepository airportRepository) {
    this.webClient = webClient;
    this.airportRepository = airportRepository;
  }

  public Flux<Airport> getAllAirports() {
    return airportRepository.findAll();
  }

  public Mono<Airport> getAirportById(String id) {
    return airportRepository.findById(id);
  }

  public Mono<Airport> retrieveAirport(String id) {
    return webClient
        .get()
        .uri("/{id}?token={token}", id, token)
        .retrieve()
        .bodyToMono(Airport.class)
        .log();
  }
}

interface AirportRepository extends ReactiveCrudRepository<Airport, String> {
}

@Document
record Airport(
    @Id String icao,
    String city,
    String state,
    String elevation_ft,
    String name,
    double latitude,
    double longitude,
    Iterable<Runway> runways) {
}

record Runway(String ident1, String ident2, int length_ft, int width_ft) {
}
