package com.example.weathertalk;

public class CityWeather {
    private String cityName;
    private String weatherInfo;

    public CityWeather(String cityName, String weatherInfo) {
        this.cityName = cityName;
        this.weatherInfo = weatherInfo;
    }

    public String getCityName() {
        return cityName;
    }

    public String getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(String weatherInfo) {
        this.weatherInfo = weatherInfo;
    }
}
