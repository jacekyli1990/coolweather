package com.jiali.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jiali.coolweather.R;
import com.jiali.coolweather.util.LogUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //判断是否有缓存，有的话，直接跳到天气界面
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String cache=preferences.getString("weather",null);
        if (cache!=null){
            LogUtil.e(this,"onCreate weather不为空="+cache);
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
