package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RunnableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 这个fragment被两个界面使用，，，这就是fragment的好处，，可以多次使用
 * Created by 29208 on 2018/11/19.
 */
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    //进度对话框
    private ProgressDialog progressDialog;

    //三个控件，作为fragment的主要内容
    private TextView titleText;
    private Button backButton;
    private ListView listView;

//    适配器
    private ArrayAdapter<String>adapter;

    //当前所处容器，也是作为listView的显示内容，，，所有要显示的东西都放到这里面
    private List<String> dataList =new ArrayList<>();

    //省列表容器           市列表             //县列表
    private List<Province>provinceList;
    private List<City>cityList;
    private  List<County>countyList;

    //选中的省份 城市 县
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    //当前级别，是省或市，或县
    private int currentLevel;
    private ImageView menubk;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //获取插入了布局（layout）的view,,,,关联fragment布局
        View view = inflater.inflate(R.layout.choose_area, container, false);
        //获取其中的控件实例
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        menubk=view.findViewById(R.id.menu_bk);
        Glide.with(this).load(R.drawable.menubk).into(menubk);
//        给其中的listView设置适配器，，，直接使用系统自带的适配器，ArrayAdapter传入context，子布局，，，string list
        adapter = new ArrayAdapter<>(getContext(), R.layout.listview_item, dataList);
        listView.setAdapter(adapter);
        Log.d("ChooseAreaFragment","onCreateView");
        return view;
    }


    @Override//onActivityCreated就是设置点击事件，，首先设置listView的setOnItemCreated，，，然后设置back的返回作用
                //主要通过queryCounties,queryProvince,queryCities,三个方法进行完成点击时间，，（判断当前level）
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("ChooseAreaFragment","onActivityCreated");
        //点击前进事件,使用setOnItemClickListener,.,,专用于列表的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override//回调方法，onItemClick回调AdapterView    view,position id
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {//省级
                    selectedProvince = provinceList.get(position);//设置当前选中的
                    queryCities();//省级降级 查询
                }
                else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);//设置当前选中的东西
                    queryCounties();//市级降级查询
                }

                //如果已经是最低级，
                else if(currentLevel==LEVEL_COUNTY){
                    //获取weatherId，，，，根据position获取County对象   从而获取weatherid
                    String weatherId=countyList.get(position).getWeatherId();

                    //instanceof 判断当前活动是哪个活动的对象，，，
                    if(getActivity() instanceof MainActivity){
                        //若为MainActivity对象，就跳转，，并且向weartherActivity传入weatherId,intent跳转传数据法
                        Intent intent=new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();//结束当前活动
                    }
                    //如果已经在weatherActicity中，
                    else if(getActivity() instanceof WeatherActivity){
//                        获取当前活动实例
                        WeatherActivity activity= (WeatherActivity) getActivity();
                        //调用weatherActivity抽屉布局的关闭抽屉方法
                        activity.drawerLayout.closeDrawer(GravityCompat.START);
                        //刷新布局
                        activity.swipeRefresh.setRefreshing(true);
//                        根据天气id请求城市天气信息
                        activity.requestWeather(weatherId);
//                        activity.mWeatherId=weatherId;
                    }
                }

            }
        });

        //点击后退事件（纯粹）
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                }
            }
        });
//        String address="http://guolin.tech/api/china";
//        queryFromServer(address, "province");
        queryProvinces();//从这里开始加载省级数据*****************************
    }

    //向服务器发出数据请求信息
    private void queryFromServer(String address,final String type){
        Log.d("ChooseAreaFragment","queryFromServer");
        showProgressDialog(); //显示对话进度框


        //发送网络请求........sendOKHttprequest请求，传入网址，和一个Callback对象

        HttpUtil.sendOkHttpRequest(address, new Callback() {//新建内部回调接口对象

            @Override//失败，弹出加载失败，，关闭正在加载
            public void onFailure(Call call, IOException e) {
                Log.d("ChooseAreaFragment","queryFromServer.onFailure");
                getActivity().runOnUiThread(new Runnable() {
                //由于要进行ui操作，，，要从子线程切换到主线程
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override//回调方法，，把网上数据存储到本地，，，再调用三个方法
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("ChooseAreaFragment","queryFromServer.onresponse");
                String responseText = response.body().string();

                boolean result = false;

                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                    if(result)
                    Log.d("exe","over");
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                //解析和处理完 数据之后，，重新调用queryProvince重新加载省级数据
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });

                }
            }
        });
    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());//传context构造ProgressDialog对象
            progressDialog.setMessage("正在加载...");//设置文字
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
        Log.d("ChooseAreaFragment","showProgressDialog");
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Log.d("ChooseAreaFragment","closeProgressDialog");
    }




    //以下三个方法主要就是三个控件的初始化，，，，刷新listView。以及改变当前level
    private void queryProvinces(){
//        Log.d("ChooseAreaFragment","queryProvince");
        titleText.setText("中国");
        //把标题设为中国，，，当前属于省级
        backButton.setVisibility(View.GONE);//back可见性设为不可见


        //省列表初始化，，DataSupport.findAll（class）所有的该class的对象findAll
        //litepal查询接口
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();//清空当前列表
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());//存储所有省份的名字
            }
            adapter.notifyDataSetChanged();//设置adapter刷新，，因为datalist已经发生改变
            listView.setSelection(0);//选择从0开始显示
            currentLevel=LEVEL_PROVINCE;//改变当前level

        }else{//首次申请，需要向网站获取数据
            String address="http://guolin.tech/api/china";

            queryFromServer(address, "province");
        }

    }

    private void queryCities(){
        Log.d("ChooseAreaFragment","queryCities");
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
        else{
            String address="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromServer(address,"city");
        }

    }
    private void queryCounties(){
        Log.d("ChooseAreaFragment","queryCounties");
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        //countryList初始化，，，根据DataSupport类获取，，需要根据城市id和雷鸣一起查询，，因为三个级别的地区类都继承了DataSupport而且每次构造对象都会采取save方法保存，，，因此亦能够通过DataSupport找到这些对象
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }
        else{
            String address ="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryFromServer(address,"county");
        }
    }







}
