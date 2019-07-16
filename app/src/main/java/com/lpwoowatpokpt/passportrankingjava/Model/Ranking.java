package com.lpwoowatpokpt.passportrankingjava.Model;

public class Ranking {
    private String Name, Cover;
    private Integer TotalScore, VisaFree, VisaOnArrival, eTa, VisaRequiered;

    public Ranking() {
    }

    public Ranking(String name, String cover, Integer totalScore, Integer visaFree, Integer visaOnArrival, Integer eTa, Integer visaRequiered) {
        Name = name;
        Cover = cover;
        TotalScore = totalScore;
        VisaFree = visaFree;
        VisaOnArrival = visaOnArrival;
        this.eTa = eTa;
        VisaRequiered = visaRequiered;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCover() {
        return Cover;
    }

    public void setCover(String cover) {
        Cover = cover;
    }

    public Integer getTotalScore() {
        return TotalScore;
    }

    public void setTotalScore(Integer totalScore) {
        TotalScore = totalScore;
    }

    public Integer getVisaFree() {
        return VisaFree;
    }

    public void setVisaFree(Integer visaFree) {
        VisaFree = visaFree;
    }

    public Integer getVisaOnArrival() {
        return VisaOnArrival;
    }

    public void setVisaOnArrival(Integer visaOnArrival) {
        VisaOnArrival = visaOnArrival;
    }

    public Integer geteTa() {
        return eTa;
    }

    public void seteTa(Integer eTa) {
        this.eTa = eTa;
    }

    public Integer getVisaRequiered() {
        return VisaRequiered;
    }

    public void setVisaRequiered(Integer visaRequiered) {
        VisaRequiered = visaRequiered;
    }
}
