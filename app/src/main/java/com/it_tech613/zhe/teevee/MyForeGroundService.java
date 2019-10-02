package com.it_tech613.zhe.teevee;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.it_tech613.zhe.teevee.activity.EmptyActivity;
import com.it_tech613.zhe.teevee.activity.WelcomeActivity;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;
import com.it_tech613.zhe.teevee.models.CategoryModels;
import com.it_tech613.zhe.teevee.models.ChannelModel;
import com.it_tech613.zhe.teevee.models.FullModel;
import com.it_tech613.zhe.teevee.models.LoginModel;
import com.it_tech613.zhe.teevee.models.MovieModel;
import com.it_tech613.zhe.teevee.models.SeriesModel;
import com.it_tech613.zhe.teevee.utils.JsonHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.it_tech613.zhe.teevee.apps.Constants.id1;



/*
 * this is an example of a service that prompts itself to a foreground service with a persistent
 * notification.  Which is now required by Oreo otherwise, a background service without an app will be killed.
 *
 */

public class MyForeGroundService extends Service {

    private final static String TAG = "MyForegroundService";

    String user,password,xxxcategory_id;
    List<CategoryModels> categories;

    public MyForeGroundService() {
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
                        Intent intent =new Intent(this, EmptyActivity.class);
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
                    String  my_timestamp= serverInfo.getString("timestamp_now");
                    String server_timestamp= serverInfo.getString("time_now");
                    Constants.setServerTimeOffset(my_timestamp,server_timestamp);
                    callVodCategory();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e0) {
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
        }
    }

    private void callLiveCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
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
            for(CategoryModels cagegory : categories){
                if(cagegory.getName().toLowerCase().contains("xxx") || cagegory.getName().toLowerCase().contains("adult")){
                    xxxcategory_id = cagegory.getId();
                }
            }
            MyApp.live_categories = categories;
            callSeriesCategory();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void callSeriesCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getSeriesCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
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
                map = map.replaceAll("[^\\x00-\\x7F]", "");
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
                    if(chModels.size()<1){
                        continue;
                    }
                    datas.add(MyApp.live_categories.get(i).getName());
                    fullModels.add(new FullModel(MyApp.live_categories.get(i).getName(),chModels,category_name));
                }
                MyApp.fullModels = fullModels;
                MyApp.maindatas = datas;
            }catch (Exception e){

            }
            getAuthorization();
        }catch (Exception e){
            e.printStackTrace();
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
    private void getAuthorization(){
        StringRequest request = new StringRequest(Constants.GetAutho1(this), string -> {
            try {
                JSONObject object = new JSONObject(string);
                if (((String) object.get("status")).equalsIgnoreCase("success")) {
                    startActivity(new Intent(MyForeGroundService.this, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                }
            }catch (JSONException e){
                toast("Check your Internet Connection!");
                e.printStackTrace();
            }

        }, volleyError -> Toast.makeText(getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show());

        RequestQueue rQueue = Volley.newRequestQueue(this);
        rQueue.add(request);
    }

    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(() -> Toast.makeText(MyForeGroundService.this, text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1,getNotification("Updating Video Resource"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.e(TAG,"service starting");
        LoginModel loginModel = (LoginModel) MyApp.instance.getPreference().get(Constants.LOGIN_INFO);
        if (loginModel==null) {
            stopSelfResult(startId);// Stops the service for the most recent start id.
            return START_NOT_STICKY;
        }else {
            user = loginModel.getUser_name();
            password = loginModel.getPassword();
            if (user.equals("") || ! password.equals("")) stopSelf();
            new Thread(this::callLogin).start();
            // If we get killed, after returning from here, restart
            return START_STICKY;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

//    @Override
//    public void onDestroy() {
//        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
//    }

    // build a persistent notification and return it.
    public Notification getNotification(String message) {

        return new NotificationCompat.Builder(getApplicationContext(), id1)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)  //persistent notification!
                .setChannelId(id1)
                .setContentTitle("Service")   //Title message top row.
                .setContentText(message)  //message when looking at the notification, second row
                .build();  //finally build and return a Notification.
    }

}
