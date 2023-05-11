package com.example.weather_app;

public class CityModel {
    private String cityName, iconName;
    private double temp;
    public CityModel(double temp, String cityName, String iconName){
        this.temp = temp;
        this.cityName = cityName;
        this.iconName = iconName;
    }

    public double getTemp() {
        return temp;
    }

    public String getCityName() {
        return cityName;
    }

    public String getIconName() {
        return iconName;
    }
}
