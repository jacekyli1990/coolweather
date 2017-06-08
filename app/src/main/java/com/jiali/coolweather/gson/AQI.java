package com.jiali.coolweather.gson;

/**
 * Created by asus on 2017/6/8.
 */

public class Aqi {
    public AqiCity city;
    public class AqiCity{
        public String aqi;
        public String pm25;
    }
}
