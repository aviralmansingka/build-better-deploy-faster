package com.aviral.conditionsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;

@SpringBootApplication
public class ConditionsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConditionsServiceApplication.class, args);
	}

	@Bean
	@LoadBalanced
	WebClient.Builder builder() {
		return WebClient.builder();
	}
}

@RestController
@RequestMapping("/")
class ConditionsController {
	private final WebClient.Builder builder;
	private final WebClient apClient;
	private final WebClient wxClient;

	public ConditionsController(WebClient.Builder builder) {
		this.builder = builder;
		this.apClient = builder
				.baseUrl("http://airport-service")
				.build();
		this.wxClient = builder
				.baseUrl("http://weather-service")
				.build();
	}

	@GetMapping
	public final String liveness() {
		return "I'm up";
	}

	@GetMapping("/summary")
	public final Flux<METAR> getWeatherForToweredFields() {
		return apClient.get().retrieve().bodyToFlux(Airport.class)
				.flatMap(ap -> wxClient
						.get()
						.uri("/metar/{id}", ap.icao())
						.retrieve()
						.bodyToMono(METAR.class))
				.log();
	}

}

record Airport(String icao) {}

record METAR(String raw, Time time, String flight_rules) {}

record Time(ZonedDateTime dt, String repr) {}
