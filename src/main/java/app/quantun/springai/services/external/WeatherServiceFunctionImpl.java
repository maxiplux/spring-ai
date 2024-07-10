package app.quantun.springai.services.external;

import app.quantun.springai.dto.WeatherRequest;
import app.quantun.springai.dto.WeatherResponse;
import app.quantun.springai.services.WeatherServiceFunction;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.function.Function;


@RequiredArgsConstructor
@AllArgsConstructor
@Slf4j
public class WeatherServiceFunctionImpl implements Function<WeatherRequest, WeatherResponse>  {



    private String apiNinjaWeatherUrl;


    private String apiNinjasKey;



    @Override
    public WeatherResponse apply(WeatherRequest weatherRequest) {
        RestClient restClient = RestClient.builder()
                .baseUrl(apiNinjaWeatherUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.set("X-Api-Key", apiNinjasKey);
                    httpHeaders.set("Accept", "application/json");
                    httpHeaders.set("Content-Type", "application/json");
                }).build();

        return restClient.get().uri(uriBuilder -> {

            log.debug("Building URI for weather request: {}" , weatherRequest);

//            uriBuilder.queryParam("city", weatherRequest.location());
//
//            if (weatherRequest.state() != null && !weatherRequest.state().isBlank()) {
//                uriBuilder.queryParam("state", weatherRequest.state());
//            }
//            if (weatherRequest.country() != null && !weatherRequest.country().isBlank()) {
//                uriBuilder.queryParam("country", weatherRequest.country());
//            }
            return uriBuilder.build();
        }).retrieve().body(WeatherResponse.class);
    }
}
