package com.it_tech613.zhe.teevee.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;
import com.it_tech613.zhe.teevee.apps.PlayGifView;
import com.it_tech613.zhe.teevee.dialog.ConnectionDlg;
import com.it_tech613.zhe.teevee.models.CategoryModels;
import com.it_tech613.zhe.teevee.models.ChannelModel;
import com.it_tech613.zhe.teevee.models.FullModel;
import com.it_tech613.zhe.teevee.models.LoginModel;
import com.it_tech613.zhe.teevee.models.MovieModel;
import com.it_tech613.zhe.teevee.models.SeriesModel;
import com.it_tech613.zhe.teevee.utils.JsonHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    String user,password,exp_date,xxxcategory_id;
    List<CategoryModels> categories;
    LoginModel loginModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        PlayGifView playGifView = findViewById(R.id.splash_gif);
        playGifView.setImageResource(R.raw.start);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playGifView.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        playGifView.setLayoutParams(params);
        loginModel = (LoginModel) MyApp.instance.getPreference().get(Constants.LOGIN_INFO);
        user = loginModel.getUser_name().replaceAll("\\s","");
        password = loginModel.getPassword().replaceAll("\\s","");

        new Thread(this::callLogin).start();
//        FullScreencall();

    }

    private void callLogin() {
        try {
            long startTime = System.nanoTime();
            String responseBody = MyApp.instance.getIptvclient().authenticate(user,password);
            long endTime = System.nanoTime();
            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),responseBody);
            Log.e("BugCheck","authenticate success "+MethodeDuration);
            try {
                JSONObject map = new JSONObject(responseBody);
                MyApp.user = user;
                MyApp.pass = password;
                JSONObject u_m;
                u_m = map.getJSONObject("user_info");
                if (u_m.getString("username") == null) {
                    Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                } else {
                    MyApp.created_at = u_m.getString("created_at");
                    MyApp.status = u_m.getString("status");
                    if(!MyApp.status.equalsIgnoreCase("Active")){
                        Intent intent =new Intent(this,EmptyActivity.class);
                        intent.putExtra("msg","Your account is Expired");
                        startActivity(intent);
                        return;
                    }
                    MyApp.is_trail = u_m.getString("is_trial");
                    MyApp.active_cons = u_m.getString("active_cons");
                    MyApp.max_cons = u_m.getString("max_connections");
                    String exp_date;
                    try{
                        exp_date = u_m.getString("exp_date");
                    }catch (Exception e){
                        exp_date = "unlimited";
                    }
                    LoginModel loginModel = new LoginModel();
                    loginModel.setUser_name(MyApp.user);
                    loginModel.setPassword(MyApp.pass);
                    try{
                        loginModel.setExp_date(exp_date);
                    }catch (Exception e){
                        loginModel.setExp_date("unlimited");
                    }
                    MyApp.loginModel = loginModel;
                    MyApp.instance.getPreference().put(Constants.LOGIN_INFO, loginModel);

                    JSONObject serverInfo= map.getJSONObject("server_info");
                    String my_timestamp= serverInfo.getString("timestamp_now");
                    String server_timestamp= serverInfo.getString("time_now");
                    Constants.setServerTimeOffset(my_timestamp,server_timestamp);
                    callVodCategory();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() ->{
                    Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this,LoginAcitivity.class));
                    finish();
                } );
            }
        } catch (Exception e0) {
            e0.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this,LoginAcitivity.class));
                finish();
            });
        }
    }

    private void callVodCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getMovieCategories(user,password);
            long endTime = System.nanoTime();
            long MethodeDuration = (endTime - startTime);

            Gson gson=new Gson();
            map = map.replaceAll("[^\\x00-\\x7F]", "");
            categories = new ArrayList<>();
            categories.add(getRecentMovies());
            categories.add(new CategoryModels(Constants.all_id,Constants.All,""));
            categories.add(new CategoryModels("favorite","Favorite","aa"));
            try {
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModels>>(){}.getType()));
            }catch (Exception e){

            }
            MyApp.vod_categories = categories;
            callLiveCategory();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callVodCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                });
                connectionDlg.show();
            });
        }
    }

    private void callLiveCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveCategories success "+MethodeDuration);
            Gson gson=new Gson();
            map = map.replaceAll("[^\\x00-\\x7F]", "");
            List<CategoryModels> categories;
            categories = new ArrayList<>();
            categories.add(new CategoryModels(Constants.recent_id,Constants.Recently_Viewed,""));
            categories.add(new CategoryModels(Constants.all_id,Constants.All,""));
            categories.add(new CategoryModels(Constants.fav_id,Constants.Favorites,""));
            try {
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModels>>(){}.getType()));
            }catch (Exception e){
            }
            MyApp.live_categories = categories;
            callSeriesCategory();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                });
                connectionDlg.show();
            });
        }
    }

    private void callSeriesCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getSeriesCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getSeriesCategories success "+MethodeDuration);
            Gson gson=new Gson();

            map = map.replaceAll("[^\\x00-\\x7F]", "");
            List<CategoryModels> categories;
            categories = new ArrayList<>();
            categories.add(getRecentSeries());
            categories.add(new CategoryModels(Constants.all_id,Constants.All,""));
            try {
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModels>>(){}.getType()));
            }catch (Exception e){

            }
            MyApp.series_categories = categories;
            callLiveStreams();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeriesCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                });
                connectionDlg.show();
            });
        }
    }

    private void callLiveStreams(){
        try{
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveStreams(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            // Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveStreams success "+MethodeDuration);
            try {
//                map = map.replaceAll("[^\\x00-\\x7F]", "");
                JSONArray array = new JSONArray(map);
                List maps = JsonHelper.toList(array);
                List<ChannelModel> channelModels = new ArrayList<>();
                if(maps!=null && maps.size()>0){
                    for (int i = 0; i < maps.size(); i++) {
                        Map ch_m = (Map) maps.get(i);
                        ChannelModel chModel = new ChannelModel();
                        String epg_id = "";
                        if(ch_m.get("epg_channel_id") == null || ch_m.get("epg_channel_id").toString().isEmpty()){
                            epg_id = String.valueOf(i);
                        }else {
                            epg_id = ch_m.get("epg_channel_id").toString();
                        }
                        String img_url ;
                        if(ch_m.get("stream_icon")==null ||  ch_m.get("stream_icon").toString().isEmpty()){
                            img_url = "a";
                        }else {
                            img_url =  ch_m.get("stream_icon").toString();
                        }
                        try{
                            chModel.setNum(String.valueOf(ch_m.get("num")));
                            chModel.setName(ch_m.get("name").toString());
                            chModel.setStream_id(String.valueOf(ch_m.get("stream_id")));
                            chModel.setCategory_id(ch_m.get("category_id").toString());
                            chModel.setEpg_channel_id(epg_id);
                            chModel.setStream_icon(img_url);
                            chModel.setTv_archive(String .valueOf(ch_m.get("tv_archive")));
                            if(ch_m.get("category_id").toString().equalsIgnoreCase(xxxcategory_id)){
                                chModel.setIs_locked(true);
                            }else {
                                chModel.setIs_locked(false);
                            }
                            channelModels.add(chModel);
                        }catch (Exception e){
                            Log.e("error","parse_error"+ i);
                        }
                    }
                }

                MyApp.channel_size = channelModels.size();
                Map back_map = new HashMap();
                back_map.put("channels", channelModels);
                MyApp.backup_map = back_map;
                List<FullModel> fullModels = new ArrayList<>();
                fullModels.add(new FullModel(Constants.recent_id, getRecentChannels(channelModels), Constants.Recently_Viewed));
                fullModels.add(new FullModel("All Channel", channelModels,"All Channel"));
                if(MyApp.instance.getPreference().get(Constants.FAV_INFO)==null){
                    fullModels.add(new FullModel("My Favorites", new ArrayList<ChannelModel>(),"My Favorites"));
                }else {
                    fullModels.add(new FullModel("My Favorites", (List<ChannelModel>) MyApp.instance.getPreference().get(Constants.FAV_INFO),"My Favorites"));
                    for(int i = 0;i<fullModels.get(0).getChannels().size();i++){
                        List<ChannelModel> fav = (List<ChannelModel>) MyApp.instance.getPreference().get(Constants.FAV_INFO);
                        for(int j=0;j< fav.size();j++){
                            if(fullModels.get(0).getChannels().get(i).getName().equals(fav.get(j).getName())){
                                fullModels.get(0).getChannels().get(i).setIs_favorite(true);
                            }else {
                                fullModels.get(0).getChannels().get(i).setIs_favorite(false);
                            }
                        }
                    }
                }

                List<String> datas = new ArrayList<>();
                datas.add(Constants.Recently_Viewed);
                datas.add("All Channel");
                datas.add("My Favorites");
                for(int i = 3; i< MyApp.live_categories.size(); i++){
                    String category_id = MyApp.live_categories.get(i).getId();
                    String category_name = MyApp.live_categories.get(i).getName();
                    int count =0;
                    List<ChannelModel> chModels = new ArrayList<>();
                    for(int j = 0;j<channelModels.size();j++){
                        if(category_id.equals(channelModels.get(j).getCategory_id())){
                            ChannelModel chModel = channelModels.get(j);
                            chModels.add(chModel);
                        }
                    }
//                    if(chModels.size()<1){
//                        continue;
//                    }
                    datas.add(MyApp.live_categories.get(i).getName());
                    fullModels.add(new FullModel(MyApp.live_categories.get(i).getName(),chModels,category_name));
                }
                MyApp.fullModels = fullModels;
                MyApp.maindatas = datas;
            }catch (Exception e){
                e.printStackTrace();
            }
            getAuthorization();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveStreams()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                });
                connectionDlg.show();
            });
        }
    }
    private List<ChannelModel> getRecentChannels(List<ChannelModel> epgChannels){
        List<ChannelModel> recentChannels=new ArrayList<>();
        if(MyApp.instance.getPreference().get(Constants.getRecentChannels())!=null){
            List<String> recent_channel_names=(List<String>) MyApp.instance.getPreference().get(Constants.getRecentChannels());
            for(int j=0;j< recent_channel_names.size();j++){
                for(int i = 0;i<epgChannels.size();i++){
                    if(epgChannels.get(i).getName().equals(recent_channel_names.get(j))){
                        recentChannels.add(epgChannels.get(i));
                    }
                }
            }
        }
        return recentChannels;
    }
    private void getAuthorization(){
        StringRequest request = new StringRequest(Constants.GetAutho1(this), string -> {
            try {
                JSONObject object = new JSONObject(string);
                if (((String) object.get("status")).equalsIgnoreCase("success")) {
                    startActivity(new Intent(SplashActivity.this,WelcomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(SplashActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        }, volleyError -> Toast.makeText(getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show());

        RequestQueue rQueue = Volley.newRequestQueue(SplashActivity.this);
        rQueue.add(request);
    }

    private CategoryModels getRecentMovies() {
        CategoryModels recentCategory = new CategoryModels(Constants.recent_id,Constants.Recently_Viewed,"");
        List<MovieModel> recentMovies=(List<MovieModel>) MyApp.instance.getPreference().get(Constants.getRecentMovies());
        if (recentMovies!=null){
            MyApp.recentMovieModels=recentMovies;
            recentCategory.setMovieModels(recentMovies);
        }
        else {
            MyApp.recentMovieModels=new ArrayList<>();
            recentCategory.setMovieModels(new ArrayList<>());
        }
        return recentCategory;
    }
    private CategoryModels getRecentSeries() {
        CategoryModels recentCategory = new CategoryModels(Constants.recent_id,Constants.Recently_Viewed,"");
        List<SeriesModel> recentMovies=(List<SeriesModel>) MyApp.instance.getPreference().get(Constants.getRecentSeries());
        if (recentMovies!=null){
            MyApp.recentSeriesModels=recentMovies;
            recentCategory.setSeriesModels(recentMovies);
        }
        else {
            MyApp.recentSeriesModels=new ArrayList<>();
            recentCategory.setSeriesModels(new ArrayList<>());
        }
        return recentCategory;
    }
    public void FullScreencall() {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
