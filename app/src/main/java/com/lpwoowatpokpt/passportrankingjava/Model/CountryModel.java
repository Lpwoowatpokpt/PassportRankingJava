package com.lpwoowatpokpt.passportrankingjava.Model;

public class CountryModel {
    private String Name, Image, Cover;
    private Long Latitude, Longitude;

    public CountryModel() {
    }

    public CountryModel(String name, String image, String cover, Long latitude, Long longitude) {
        Name = name;
        Image = image;
        Cover = cover;
        Latitude = latitude;
        Longitude = longitude;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getCover() {
        return Cover;
    }

    public void setCover(String cover) {
        Cover = cover;
    }

    public Long getLatitude() {
        return Latitude;
    }

    public void setLatitude(Long latitude) {
        Latitude = latitude;
    }

    public Long getLongitude() {
        return Longitude;
    }

    public void setLongitude(Long longitude) {
        Longitude = longitude;
    }
}
