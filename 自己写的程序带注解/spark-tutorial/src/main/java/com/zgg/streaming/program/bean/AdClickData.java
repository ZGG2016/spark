package com.zgg.streaming.program.bean;

public class AdClickData {
    private String ts;
    private String area;
    private String city;
    private String user;
    private String ad;

    public AdClickData(String s, String s1, String s2, String s3, String s4, String s5) {
    }

    public AdClickData(String ts, String area, String city, String user, String ad) {
        this.ts = ts;
        this.area = area;
        this.city = city;
        this.user = user;
        this.ad = ad;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    @Override
    public String toString() {
        return "AdClickData{" +
                "ts='" + ts + '\'' +
                ", area='" + area + '\'' +
                ", city='" + city + '\'' +
                ", user='" + user + '\'' +
                ", ad='" + ad + '\'' +
                '}';
    }
}
