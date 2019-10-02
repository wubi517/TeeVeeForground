package com.it_tech613.zhe.teevee.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;
import com.it_tech613.zhe.teevee.dialog.ConnectionDlg;
import com.it_tech613.zhe.teevee.dialog.VPNPinDlg;
import com.it_tech613.zhe.teevee.models.CategoryModels;
import com.it_tech613.zhe.teevee.models.ChannelModel;
import com.it_tech613.zhe.teevee.models.FullModel;
import com.it_tech613.zhe.teevee.models.LoginModel;
import com.it_tech613.zhe.teevee.models.MovieModel;
import com.it_tech613.zhe.teevee.models.SeriesModel;
import com.it_tech613.zhe.teevee.utils.JsonHelper;
import com.it_tech613.zhe.teevee.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginAcitivity extends AppCompatActivity implements View.OnClickListener{
    EditText name_txt, pass_txt;
    ProgressBar progressBar;
    String user, password,exp_date,xxxcategory_id;
    List<CategoryModels> categories;
    TextView phone;
    CheckBox checkBox;
    ImageView logo;
    boolean is_remember = false;
    static {
        System.loadLibrary("notifications");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        setContentView(R.layout.activity_login_acitivity);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (MyApp.instance.getPreference().get(Constants.MAC_ADDRESS) == null) {
            MyApp.mac_address = Utils.getPhoneMac(LoginAcitivity.this);
            MyApp.instance.getPreference().put(Constants.MAC_ADDRESS, MyApp.mac_address.toUpperCase());
        } else
            MyApp.mac_address = (String) MyApp.instance.getPreference().get(Constants.MAC_ADDRESS);

        RelativeLayout main_lay = findViewById(R.id.main_lay);
        Bitmap myImage = getBitmapFromURL(Constants.GetLoginImage(LoginAcitivity.this));
        Drawable dr = new BitmapDrawable(myImage);
        main_lay.setBackgroundDrawable(dr);
        progressBar = findViewById(R.id.login_progress);
        name_txt = findViewById(R.id.login_name);
        pass_txt =  findViewById(R.id.login_pass);
        phone = findViewById(R.id.phone);
        checkBox = findViewById(R.id.checkbox);
        checkBox.setOnClickListener(this);
        if(phone.getVisibility()==View.GONE){
            MyApp.instance.getPreference().put(Constants.IS_PHONE,"is_phone");
        }

        if(MyApp.instance.getPreference().get(Constants.LOGIN_INFO)!=null){
            LoginModel loginModel = (LoginModel) MyApp.instance.getPreference().get(Constants.LOGIN_INFO);
            user = loginModel.getUser_name();
            password = loginModel.getPassword();
            name_txt.setText(user);
            pass_txt.setText(password);
            checkBox.setChecked(true);

            progressBar.setVisibility(View.VISIBLE);
            new Thread(this::callLogin).start();
        }
        name_txt.requestFocus();
        name_txt.setText("antonello343");
        pass_txt.setText("34316878762");
        TextView mac_txt = findViewById(R.id.login_mac_address);
        mac_txt.setText(MyApp.mac_address);
        TextView version_txt = (TextView) findViewById(R.id.app_version_code);
        MyApp.version_str = "v " + MyApp.version_name;
        version_txt.setText(MyApp.version_str);
        findViewById(R.id.login_btn).setOnClickListener(this);
        logo = (ImageView)findViewById(R.id.logo);
        Picasso.with(LoginAcitivity.this).load(Constants.GetIcon(LoginAcitivity.this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(logo);
        logo.setOnClickListener(this);
        FullScreencall();
    }

    private void callLogin() {
        try {
            runOnUiThread(()->progressBar.setVisibility(View.VISIBLE));
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
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                } else {
                    MyApp.created_at = u_m.getString("created_at");
                    MyApp.status = u_m.getString("status");
                    if(!MyApp.status.equalsIgnoreCase("Active")){
                        Intent intent =new Intent(LoginAcitivity.this,EmptyActivity.class);
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
                    Log.e("remember",String.valueOf(is_remember));
                    if(checkBox.isChecked()){
                        MyApp.instance.getPreference().put(Constants.LOGIN_INFO, loginModel);
                    }
                    JSONObject serverInfo= map.getJSONObject("server_info");
                    String  my_timestamp= serverInfo.getString("timestamp_now");
                    String server_timestamp= serverInfo.getString("time_now");
                    Constants.setServerTimeOffset(my_timestamp,server_timestamp);
                    callVodCategory();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() ->{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                } );
            }
        } catch (Exception e0) {
            e0.printStackTrace();
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginAcitivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLogin()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginAcitivity.this, ConnectionErrorActivity.class));
                    }
                });
                connectionDlg.show();
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
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginAcitivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callVodCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginAcitivity.this, ConnectionErrorActivity.class));
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
            MyApp.live_categories = categories;
            for(CategoryModels cagegory : categories){
                if(cagegory.getName().toLowerCase().contains("xxx") || cagegory.getName().toLowerCase().contains("adult")){
                    xxxcategory_id = cagegory.getId();
                }
            }
            callSeriesCategory();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginAcitivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginAcitivity.this, ConnectionErrorActivity.class));
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
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginAcitivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeriesCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginAcitivity.this, ConnectionErrorActivity.class));
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
                            Log.e("error","parse_error"+String .valueOf(i));
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
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginAcitivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveStreams()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginAcitivity.this, ConnectionErrorActivity.class));
                    }
                });
                connectionDlg.show();
            });
        }
    }



    public Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (name_txt.getText().toString().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "User name cannot be blank.", Toast.LENGTH_LONG).show();
                    return;
                } else if (pass_txt.getText().toString().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Password cannot be blank.", Toast.LENGTH_LONG).show();
                    return;
                }
                user = name_txt.getText().toString();
                password = pass_txt.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                new Thread(this::callLogin).start();
                break;
            case R.id.checkbox:
                is_remember = checkBox.isChecked();
                break;
            case R.id.logo:
                showVpnPinDlg();
                break;
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
        StringRequest request = new StringRequest(Constants.GetAutho1(this), new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    JSONObject object = new JSONObject(string);
                    if (((String) object.get("status")).equalsIgnoreCase("success")) {
                        startActivity(new Intent(LoginAcitivity.this,WelcomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginAcitivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, volleyError -> Toast.makeText(getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show());

        RequestQueue rQueue = Volley.newRequestQueue(LoginAcitivity.this);
        rQueue.add(request);
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else  {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            Toast.makeText(this,""+event.getKeyCode(),Toast.LENGTH_SHORT).show();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    showVpnPinDlg();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    hideKeyboard(view);
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    public void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception ignored) {
        }
    }
    private void showVpnPinDlg() {
        VPNPinDlg pinDlg = new VPNPinDlg(LoginAcitivity.this, new VPNPinDlg.DlgPinListener() {
            @Override
            public void OnYesClick(Dialog dialog, String pin_code) {
                String pin = (String )MyApp.instance.getPreference().get(Constants.DIRECT_VPN_PIN_CODE);
                if(pin_code.equalsIgnoreCase(pin)){
                    dialog.dismiss();
                    startActivity(new Intent(LoginAcitivity.this,VpnActivity.class));
                }else {
                    Toast.makeText(LoginAcitivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void OnCancelClick(Dialog dialog, String pin_code) {
                dialog.dismiss();
            }
        });
        pinDlg.show();
    }
}
