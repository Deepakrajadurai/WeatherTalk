package com.example.weathertalk;

public class City {
    public String name;
    public double lat, lon;
    public float[] temps;

    public City(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}
