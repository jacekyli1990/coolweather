package com.jiali.coolweather.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiali.coolweather.Activity.WeatherActivity;
import com.jiali.coolweather.R;
import com.jiali.coolweather.db.City;
import com.jiali.coolweather.db.County;
import com.jiali.coolweather.db.Province;
import com.jiali.coolweather.util.HttpUtil;
import com.jiali.coolweather.util.LogUtil;
import com.jiali.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.crud.DataSupport.count;
import static org.litepal.crud.DataSupport.findAll;

/**
 * Created by asus on 2017/6/6.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> datalist=new ArrayList<>();
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText= (TextView) view.findViewById(R.id.title_text);
        backButton= (Button) view.findViewById(R.id.back_button);
        listView= (ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.e("ChooseAreaFragment","onItemClick");
                switch(currentLevel){
                    case LEVEL_PROVINCE:
                        selectedProvince=provinceList.get(position);
                        LogUtil.e("ChooseAreaFragment","onItemClick LEVEL_PROVINCE");
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        selectedCity=cityList.get(position);
                        LogUtil.e("ChooseAreaFragment","onItemClick LEVEL_CITY");
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        String weatherId=countyList.get(position).getWeatherId();
                        Intent intent=new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        LogUtil.e("ChooseAreaFragment","onItemClick LEVEL_COUNTY weather_id="+weatherId);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    default:
                        break;
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    //查询全国所有的省，先从数据库查，如果没有则查询服务器
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= findAll(Province.class);
        if (provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    //查询选中省的所有市，先从数据库查，如果没有则查询服务器
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            datalist.clear();
            for (City city:cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
           int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    //查询选中市的所有县，先从数据库查，如果没有则查询服务器
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList =DataSupport.where("cityid=?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            datalist.clear();
            for (County county : countyList){
                datalist.add(county.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel= LEVEL_COUNTY;
        }else {
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china"+"/"+selectedProvince.getProvinceCode()
                    +"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                switch(type){
                    case "province":
                        result= Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                        break;
                    case "county":
                        result=Utility.handleCountryResponse(responseText,selectedCity.getId());
                        break;
                    default:
                        break;
                }
                //如果处理返回的数据成功
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch(type){
                                case "province":
                                    queryProvinces();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
