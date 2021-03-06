package io.stock.evaluation.web.price.api;

import io.stock.evaluation.web.crawling.stock.price.application.CrawlingValuationService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class PriceApiHandler {
    private final CrawlingValuationService crawlingValuationService;

    public PriceApiHandler(CrawlingValuationService crawlingValuationService){
        this.crawlingValuationService = crawlingValuationService;
    }

    public Mono<ServerResponse> getPriceBasicValuation (ServerRequest serverRequest){
        return serverRequest.queryParam("ticker")
                .map(ticker -> {
                    return crawlingValuationService.getPriceBasicValuationData(ticker)
                            .flatMap(cdata -> ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromValue(cdata))
                                        .switchIfEmpty(notFound().build())
                            );
                })
                .orElse(notFound().build());
    }
}
