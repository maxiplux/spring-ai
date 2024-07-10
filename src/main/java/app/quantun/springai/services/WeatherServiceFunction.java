package app.quantun.springai.services;

import app.quantun.springai.dto.WeatherRequest;
import app.quantun.springai.dto.WeatherResponse;

public interface WeatherServiceFunction {

    public  WeatherResponse apply(WeatherRequest weatherRequest);

}
