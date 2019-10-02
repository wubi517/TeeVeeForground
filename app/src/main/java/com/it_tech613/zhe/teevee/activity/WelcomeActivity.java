package com.it_tech613.zhe.teevee.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.adapter.CategoryListAdapter;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;
import com.it_tech613.zhe.teevee.dialog.AccountDlg;
import com.it_tech613.zhe.teevee.dialog.HideCategoryDlg;
import com.it_tech613.zhe.teevee.dialog.MultiScreenDlg;
import com.it_tech613.zhe.teevee.dialog.ParentContrlDlg;
import com.it_tech613.zhe.teevee.dialog.PinDlg;
import com.it_tech613.zhe.teevee.dialog.PinMultiScreenDlg;
import com.it_tech613.zhe.teevee.dialog.ReloadDlg;
import com.it_tech613.zhe.teevee.dialog.SettingDlg;
import com.it_tech613.zhe.teevee.dialog.TimeFormatDlg;
import com.it_tech613.zhe.teevee.listner.SimpleGestureFilter;
import com.it_tech613.zhe.teevee.listner.SimpleGestureFilter.SimpleGestureListener;
import com.it_tech613.zhe.teevee.models.CategoryModels;
import com.it_tech613.zhe.teevee.models.CategoryType;
import com.it_tech613.zhe.teevee.vpn.fastconnect.core.OpenConnectManagementThread;
import com.it_tech613.zhe.teevee.vpn.fastconnect.core.OpenVpnService;
import com.it_tech613.zhe.teevee.vpn.fastconnect.core.VPNConnector;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,View.OnClickListener, SimpleGestureListener {
    private SimpleGestureFilter detector;
    Context context = null;
    ImageView image_left, image_center, image_right,image_ad1,image_ad2,icon;
    ListView categroy_list;
    TextView txt_left, txt_center, txt_right, txt_date, txt_time;
    Button btn_left,btn_right;
    CategoryListAdapter categoryListAdapter;
    List<CategoryModels> categories;
    List<String> settingDatas;
    LinearLayout ly_center;
    int category_pos;
    SimpleDateFormat date = new SimpleDateFormat("EEE,  dd  MMM, yyyy");
    SimpleDateFormat time ;
    String time_format;
    boolean is_center = false,doubleBackToExitPressedOnce = false;
    boolean admAvailable;
    String[] category_names;
    String[] category_ids;
    boolean[] checkedItems;
    List<String> selectedIds= new ArrayList<>();
    private VPNConnector mConn;
    private int current_position = 0;
    private int current_player = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        admAvailable = false;
        LinearLayout main_lay = findViewById(R.id.main_lay);
        Bitmap myImage = getBitmapFromURL(Constants.GetMainImage(WelcomeActivity.this));
        Drawable dr = new BitmapDrawable(myImage);
        main_lay.setBackgroundDrawable(dr);
        detector = new SimpleGestureFilter(WelcomeActivity.this, WelcomeActivity.this);
        context = this;
        time_format = (String) MyApp.instance.getPreference().get(Constants.TIME_FORMAT);
        if(time_format.equalsIgnoreCase("12hour")){
            time=new SimpleDateFormat("hh:mm a");
        }else {
            time = new SimpleDateFormat("HH:mm");
        }
        MyApp.is_welcome = true;
        settingDatas = new ArrayList<>();
        settingDatas.addAll(Arrays.asList(getResources().getStringArray(R.array.setting_list)));

        image_ad1 = findViewById(R.id.image_ad1);
        image_ad2 = findViewById(R.id.image_ad2);
        icon = findViewById(R.id.icon);

        Picasso.with(this).load(Constants.GetIcon(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(icon);

        Picasso.with(this).load(Constants.GetAd1(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.ad1)
                .into(image_ad1);
        Picasso.with(this).load(Constants.GetAd2(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.ad2)
                .into(image_ad2);

        ly_center = findViewById(R.id.ly_center);
        ly_center.setOnClickListener(this);
        txt_date = findViewById(R.id.txt_date);
        txt_time = findViewById(R.id.txt_time);
        image_left = findViewById(R.id.image_left);
        image_center = findViewById(R.id.image_center);
        image_right = findViewById(R.id.image_right);
        txt_left = findViewById(R.id.txt_left);
        txt_right = findViewById(R.id.txt_right);
        txt_center = findViewById(R.id.txt_center);
        categroy_list = findViewById(R.id.category_list);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        btn_left.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        categroy_list.setOnItemClickListener(this);
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();

        switch (txt_center.getText().toString().toLowerCase()) {
            case "tv":
                if (MyApp.instance.getPreference().get(Constants.CHANNEL_POS) == null) {
                    category_pos = 0;
                } else {
                    category_pos = (int) MyApp.instance.getPreference().get(Constants.CHANNEL_POS);
                }
                categories = new ArrayList<>();
                getLiveFilter();
                categories = MyApp.live_categories_filter;
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                categoryListAdapter.selectItem(category_pos);
                categroy_list.setSelection(category_pos);
                break;
            case "tv series":
                if(MyApp.instance.getPreference().get(Constants.SERIES_POS)==null){
                    category_pos = 0;
                }else {
                    category_pos = (int) MyApp.instance.getPreference().get(Constants.SERIES_POS);
                }
                categories = new ArrayList<>();
                getSeriesFilter();
                categories = MyApp.series_categories_filter;
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                categoryListAdapter.selectItem(category_pos);
                categroy_list.setSelection(category_pos);
                break;
            case "video club":
                if(MyApp.instance.getPreference().get(Constants.VOD_POS)==null){
                    category_pos = 0;
                }else {
                    category_pos = (int) MyApp.instance.getPreference().get(Constants.VOD_POS);
                }
                categories = new ArrayList<>();
                getVodFilter();
                categories = MyApp.vod_categories_filter;
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                categoryListAdapter.selectItem(category_pos);
                categroy_list.setSelection(category_pos);
                break;
        }

        changeButtons(txt_center.getText().toString().toLowerCase());
        categroy_list.requestFocus();
        FullScreencall();

        if(MyApp.is_vpn){
            findViewById(R.id.ly_vpn).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.ly_vpn).setVisibility(View.GONE);
        }
    }

    private void stopVPN() {
        if (mConn.service.getConnectionState() ==
                OpenConnectManagementThread.STATE_DISCONNECTED) {
            mConn.service.startReconnectActivity(this);
        } else {
            mConn.service.stopVPN();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_left:
                RowLeft();
                break;
            case R.id.btn_right:
                RowRight();
                break;
            case R.id.ly_center:
                if(txt_center.getText().toString().equalsIgnoreCase("setting")){
                    showSettingDlg();
                }else if(txt_center.getText().toString().equalsIgnoreCase("guide")){
                    startActivity(new Intent(WelcomeActivity.this, EPGActivityUpdated.class));
                }else if(txt_center.getText().toString().equalsIgnoreCase("multi")){
//                    showScreenModeList();
                    showMultiScrenDlg();
                }
                break;
        }
    }

    private void showSettingDlg() {
        SettingDlg settingDlg = new SettingDlg(WelcomeActivity.this, settingDatas, (dialog, position) -> {
            switch (position){
                case 0:
                    ParentContrlDlg dlg = new ParentContrlDlg(WelcomeActivity.this, new ParentContrlDlg.DialogUpdateListener() {
                        @Override
                        public void OnUpdateNowClick(Dialog dialog, int code) {
                            if(code==1){
                                dialog.dismiss();
                            }
                        }
                        @Override
                        public void OnUpdateSkipClick(Dialog dialog, int code) {
                            dialog.dismiss();
                        }
                    });
                    dlg.show();
                    break;
                case 1:
                    ReloadDlg();
                    break;
                case 2:
                    showSortingDlg();
                    break;
                case 3:
                    showInternalPlayers();
                    break;
                case 4:
                    TimeFormatDlg timeFormatDlg = new TimeFormatDlg(WelcomeActivity.this, new TimeFormatDlg.DialogUpdateListener() {
                        @Override
                        public void OnUpdateNowClick(Dialog dialog) {
                            dialog.dismiss();
                        }

                        @Override
                        public void OnUpdateSkipClick(Dialog dialog) {
                            dialog.dismiss();
                        }
                    });
                    timeFormatDlg.show();
                    break;
//                case 4:
//                    //TODO
//                    showMultiSelection(CategoryType.live);
//                    break;
//                case 5:
//                    //TODO
//                    showMultiSelection(CategoryType.vod);
//                    break;
//                case 6:
//                    //TODO
//                    showMultiSelection(CategoryType.series);
//                    break;
                case 5:
                    AccountDlg accountDlg = new AccountDlg(WelcomeActivity.this, dialog1 -> {
                    });
                    accountDlg.show();
                    break;
//                case 8:
//                    startActivity(new Intent(WelcomeActivity.this, VpnActivity.class));
//                    break;
                case 6:
                    MyApp.instance.getPreference().remove(Constants.LOGIN_INFO);
                    MyApp.instance.getPreference().remove(Constants.CHANNEL_POS);
                    MyApp.instance.getPreference().remove(Constants.SERIES_POS);
                    MyApp.instance.getPreference().remove(Constants.VOD_POS);
                    startActivity(new Intent(WelcomeActivity.this, LoginAcitivity.class));
                    finish();
                    break;
            }
        });
        settingDlg.show();
    }

    private void showInternalPlayers(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Player Option");
        String[] screen_mode_list = {"VLC Player", "IJK Player", "Exo Player"};
        if (MyApp.instance.getPreference().get(Constants.CURRENT_PLAYER)!=null)
            current_position = (int) MyApp.instance.getPreference().get(Constants.CURRENT_PLAYER);
        else current_position = 0;
        builder.setSingleChoiceItems(screen_mode_list, current_position,
                (dialog, which) -> current_position =which);
        builder.setPositiveButton("OK", (dialog, which) -> {
            MyApp.instance.getPreference().put(Constants.CURRENT_PLAYER, current_position);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showSortingDlg(){
        if (MyApp.instance.getPreference().get(Constants.SORT)!=null)
            current_position = (int) MyApp.instance.getPreference().get(Constants.SORT);
        else current_position = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Sorting Method");
        String[] screen_mode_list = {"Sort by Number", "Sort by Added", "Sort by Name"};
        builder.setSingleChoiceItems(screen_mode_list, current_position, (dialog, which) -> current_position =which);
        builder.setPositiveButton("OK", (dialog, which) ->
                MyApp.instance.getPreference().put(Constants.SORT, current_position));
        builder.setNegativeButton("Cancel", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showMultiSelection(final CategoryType categoryType) {
        switch (categoryType){
            case vod:
                if (MyApp.instance.getPreference().get(Constants.INVISIBLE_VOD_CATEGORIES)!=null) selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.INVISIBLE_VOD_CATEGORIES);
                category_names=new String[MyApp.vod_categories.size()];
                category_ids=new String[MyApp.vod_categories.size()];
                checkedItems=new boolean[category_names.length];
                for (int i = 0; i< MyApp.vod_categories.size(); i++){
                    CategoryModels categoryModels= MyApp.vod_categories.get(i);
                    category_names[i]=categoryModels.getName();
                    category_ids[i]=categoryModels.getId();
                    checkedItems[i] = !selectedIds.contains(categoryModels.getId());
                }
                break;
            case live:
                if (MyApp.instance.getPreference().get(Constants.INVISIBLE_LIVE_CATEGORIES)!=null) selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.INVISIBLE_LIVE_CATEGORIES);
                category_names=new String[MyApp.live_categories.size()-2];
                category_ids=new String[MyApp.live_categories.size()-2];
                checkedItems=new boolean[category_names.length];
                for (int i = 0; i< MyApp.live_categories.size()-2; i++){
                    CategoryModels categoryModels= MyApp.live_categories.get(i+2);
                    category_names[i]=categoryModels.getName();
                    category_ids[i]=categoryModels.getId();
                    checkedItems[i] = !selectedIds.contains(categoryModels.getId());
                }
                break;
            case series:
                if (MyApp.instance.getPreference().get(Constants.INVISIBLE_SERIES_CATEGORIES)!=null) selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.INVISIBLE_SERIES_CATEGORIES);
                category_names=new String[MyApp.series_categories.size()-1];
                category_ids=new String[MyApp.series_categories.size()-1];
                checkedItems=new boolean[category_names.length];
                for (int i = 0; i< MyApp.series_categories.size()-1; i++){
                    CategoryModels categoryModels= MyApp.series_categories.get(i+1);
                    category_names[i]=categoryModels.getName();
                    category_ids[i]=categoryModels.getId();
                    checkedItems[i] = !selectedIds.contains(categoryModels.getId());
                }
                break;
        }
        HideCategoryDlg dlg=new HideCategoryDlg(this, category_names, checkedItems, new HideCategoryDlg.DialogSearchListener() {
            @Override
            public void OnItemClick(Dialog dialog, int position, boolean checked) {
                if (!checked){
                    if (!selectedIds.contains(category_ids[position])){
                        selectedIds.add(category_ids[position]);
                    }
                }else {
                    if (selectedIds.contains(category_ids[position])){
                        selectedIds.removeAll(Arrays.asList(category_ids[position]));
                    }
                }
            }

            @Override
            public void OnOkClick(Dialog dialog) {
                selectedIds=new ArrayList<>();
                for (int m=0;m<checkedItems.length;m++){
                    if (!checkedItems[m]) selectedIds.add(category_ids[m]);
                }
                switch (categoryType){
                    case series:
                        MyApp.instance.getPreference().put(Constants.INVISIBLE_SERIES_CATEGORIES,selectedIds);
                        break;
                    case live:
                        MyApp.instance.getPreference().put(Constants.INVISIBLE_LIVE_CATEGORIES,selectedIds);
                        break;
                    case vod:
                        MyApp.instance.getPreference().put(Constants.INVISIBLE_VOD_CATEGORIES,selectedIds);
                        break;
                }
            }

            @Override
            public void OnCancelClick(Dialog dialog) {

            }

            @Override
            public void OnSelectAllClick(Dialog dialog) {
                for(int i=0;i<checkedItems.length;i++){
                    checkedItems[i]=true;
                    selectedIds.clear();
                }
            }
        });
        dlg.show();
    }

    private void  RowLeft(){
        String center_str="";
        switch (txt_center.getText().toString().toLowerCase()) {
            case "tv":
                center_str="video club";
                break;
            case "multi":
                center_str="tv";
                break;
            case "tv series":
                center_str="tv";
//                center_str="multi";
                break;
            case "guide":
                center_str="tv series";
                break;
            case "setting":
                center_str="guide";
                break;
            case "video club":
                center_str="setting";
                break;
        }
        changeButtons(center_str);
    }

    private void  RowRight(){
        String center_str="";
        switch (txt_center.getText().toString().toLowerCase()) {
            case "tv":
//                center_str="multi";
                center_str="tv series";
                break;
            case "multi":
                center_str="tv series";
                break;
            case "tv series":
                center_str="guide";
                break;
            case "guide":
                center_str="setting";
                break;
            case "setting":
                center_str="video club";
                break;
            case "video club":
                center_str="tv";
                break;
        }
        changeButtons(center_str);
    }

    private void changeButtons(String center_str){
        switch (center_str) {
            case "video club":
                if(MyApp.instance.getPreference().get(Constants.VOD_POS)==null){
                    category_pos = 0;
                }else {
                    category_pos = (int) MyApp.instance.getPreference().get(Constants.VOD_POS);
                }
                getVodFilter();
                categories = MyApp.vod_categories_filter;
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                categoryListAdapter.selectItem(category_pos);
                categroy_list.setSelection(category_pos);
                image_center.setImageResource(R.drawable.video_club);
                txt_center.setText("VIDEO CLUB");
                image_left.setImageResource(R.drawable.setting);
                txt_left.setText("SETTING");
                image_right.setImageResource(R.drawable.tv_icon);
                txt_right.setText("TV");
                break;
            case "tv":
                if (MyApp.instance.getPreference().get(Constants.CHANNEL_POS) == null) {
                    category_pos = 0;
                } else {
                    category_pos = (int) MyApp.instance.getPreference().get(Constants.CHANNEL_POS);
                }
                getLiveFilter();
                categories = MyApp.live_categories_filter;
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                categoryListAdapter.selectItem(category_pos);
                categroy_list.setSelection(category_pos);
                image_center.setImageResource(R.drawable.tv_icon);
                txt_center.setText("TV");
                image_right.setImageResource(R.drawable.tv_serires);
                txt_right.setText("TV SERIES");
//                image_right.setImageResource(R.drawable.multi);
//                txt_right.setText("MULTI");
                image_left.setImageResource(R.drawable.video_club);
                txt_left.setText("VIDEO CLUB");
                break;
            case "multi":
                categories = new ArrayList<>();
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                image_center.setImageResource(R.drawable.multi);
                txt_center.setText("MULTI");
                image_right.setImageResource(R.drawable.tv_serires);
                txt_right.setText("TV SERIES");
                image_left.setImageResource(R.drawable.tv_icon);
                txt_left.setText("TV");
                break;
            case "tv series":
                if (MyApp.instance.getPreference().get(Constants.SERIES_POS) == null) {
                    category_pos = 0;
                } else {
                    category_pos = (int) MyApp.instance.getPreference().get(Constants.SERIES_POS);
                }
                getSeriesFilter();
                categories = MyApp.series_categories_filter;
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                categoryListAdapter.selectItem(category_pos);
                categroy_list.setSelection(category_pos);
//                            image_center.setPadding(Utils.dp2px(this,85),Utils.dp2px(this,8),Utils.dp2px(this,85),Utils.dp2px(this,0));
                image_center.setImageResource(R.drawable.tv_serires);
                txt_center.setText("TV SERIES");
                image_right.setImageResource(R.drawable.guide);
                txt_right.setText("GUIDE");
                image_left.setImageResource(R.drawable.tv_icon);
                txt_left.setText("TV");
//                image_left.setImageResource(R.drawable.multi);
//                txt_left.setText("MULTI");
                break;
            case "guide":
                categories = new ArrayList<>();
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                image_center.setImageResource(R.drawable.guide);
                txt_center.setText("GUIDE");
                image_right.setImageResource(R.drawable.setting);
                txt_right.setText("SETTING");
                image_left.setImageResource(R.drawable.tv_serires);
                txt_left.setText("TV SERIES");
                break;
            case "setting":
                categories = new ArrayList<>();
                categoryListAdapter = new CategoryListAdapter(WelcomeActivity.this, categories);
                categroy_list.setAdapter(categoryListAdapter);
                image_center.setImageResource(R.drawable.setting);
                image_right.setImageResource(R.drawable.video_club);
                image_left.setImageResource(R.drawable.guide);
                txt_center.setText("SETTING");
                txt_right.setText("VIDEO CLUB");
                txt_left.setText("GUIDE");
                break;
        }
    }

    private void getVodFilter() {
        List<String> selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.INVISIBLE_VOD_CATEGORIES);
        MyApp.vod_categories_filter=new ArrayList<>();
        MyApp.vod_categories_filter.addAll(MyApp.vod_categories);
        if (selectedIds!=null && selectedIds.size()!=0) {
            for (CategoryModels categoryModel: MyApp.vod_categories){
                for (String string:selectedIds){
                    if (string.equalsIgnoreCase(categoryModel.getId()))
                        MyApp.vod_categories_filter.remove(categoryModel);
                }
            }

        }
    }

    private void getLiveFilter() {
        List<String> selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.INVISIBLE_LIVE_CATEGORIES);
        MyApp.live_categories_filter=new ArrayList<>();
        MyApp.live_categories_filter.addAll(MyApp.live_categories);
        MyApp.fullModels_filter=new ArrayList<>();
        MyApp.fullModels_filter.addAll(MyApp.fullModels);
        if (selectedIds!=null && selectedIds.size()!=0) {
            for (int i = 0; i< MyApp.live_categories.size(); i++){
                CategoryModels categoryModel= MyApp.live_categories.get(i);
                for (String string:selectedIds){
                    if (string.equalsIgnoreCase(categoryModel.getId())) {
                        MyApp.live_categories_filter.remove(categoryModel);
                        MyApp.fullModels_filter.remove(MyApp.fullModels.get(i));
                    }
                }
            }

        }
    }

    private void getSeriesFilter() {
        List<String> selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.INVISIBLE_SERIES_CATEGORIES);
        MyApp.series_categories_filter=new ArrayList<>();
        MyApp.series_categories_filter.addAll(MyApp.series_categories);
        if (selectedIds!=null && selectedIds.size()!=0) {
            for (CategoryModels categoryModel: MyApp.series_categories){
                for (String string:selectedIds){
                    if (string.equalsIgnoreCase(categoryModel.getId()))
                        MyApp.series_categories_filter.remove(categoryModel);
                }
            }

        }
    }

    private void ReloadDlg(){
        ReloadDlg reloadDlg = new ReloadDlg(WelcomeActivity.this, new ReloadDlg.DialogUpdateListener() {
            @Override
            public void OnUpdateNowClick(Dialog dialog) {
                dialog.dismiss();
//                startActivity(new Intent(WelcomeActivity.this,SplashActivity.class));
//                finish();
            }

            @Override
            public void OnUpdateSkipClick(Dialog dialog) {
                dialog.dismiss();
            }
        });
        reloadDlg.show();
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        current_player = (int) MyApp.instance.getPreference().get(Constants.CURRENT_PLAYER);
        switch (txt_center.getText().toString().toLowerCase()) {
            case "tv":
                category_pos = position;
                MyApp.instance.getPreference().put(Constants.CHANNEL_POS,position);
                if(categories.get(category_pos).getName().toLowerCase().contains("adult") ||categories.get(category_pos).getName().toLowerCase().contains("adult")){
                    PinDlg pinDlg = new PinDlg(WelcomeActivity.this, new PinDlg.DlgPinListener() {
                        @Override
                        public void OnYesClick(Dialog dialog, String pin_code) {
                            String pin = (String ) MyApp.instance.getPreference().get(Constants.PIN_CODE);
                            if(pin_code.equalsIgnoreCase(pin)){
                                dialog.dismiss();
                                switch (current_player){
                                    case 0:
                                        startActivity(new Intent(WelcomeActivity.this, PreviewChannelActivity.class));
                                        break;
                                    case 1:
                                        startActivity(new Intent(WelcomeActivity.this, PreviewChannelIJKActivity.class));
                                        break;
                                    case 2:
                                        startActivity(new Intent(WelcomeActivity.this, PreviewChannelExoActivity.class));
                                        break;
                                }

                            }else {
                                Toast.makeText(WelcomeActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void OnCancelClick(Dialog dialog, String pin_code) {
                            dialog.dismiss();
                        }
                    });
                    pinDlg.show();
                }else {
                    switch (current_player){
                        case 0:
                            startActivity(new Intent(WelcomeActivity.this, PreviewChannelActivity.class));
                            break;
                        case 1:
                            startActivity(new Intent(WelcomeActivity.this, PreviewChannelIJKActivity.class));
                            break;
                        case 2:
                            startActivity(new Intent(WelcomeActivity.this, PreviewChannelExoActivity.class));
                            break;
                    }

                }
                categoryListAdapter.selectItem(position);
                break;
            case "tv series":
                is_center = true;
                category_pos = position;
                MyApp.instance.getPreference().put(Constants.SERIES_POS,category_pos);
                categoryListAdapter.selectItem(position);
                if(categories.get(category_pos).getName().toLowerCase().contains("adult") ||categories.get(category_pos).getName().toLowerCase().contains("adult")){
                    PinDlg pinDlg = new PinDlg(WelcomeActivity.this, new PinDlg.DlgPinListener() {
                        @Override
                        public void OnYesClick(Dialog dialog, String pin_code) {
                            String pin = (String ) MyApp.instance.getPreference().get(Constants.PIN_CODE);
                            if(pin_code.equalsIgnoreCase(pin)){
                                dialog.dismiss();
                                startActivity(new Intent(WelcomeActivity.this, PreviewSeriesActivity.class));
                            }else {
                                Toast.makeText(WelcomeActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void OnCancelClick(Dialog dialog, String pin_code) {
                            dialog.dismiss();
                        }
                    });
                    pinDlg.show();
                }else {
                    startActivity(new Intent(WelcomeActivity.this, PreviewSeriesActivity.class));
                }
                break;
            case "video club":
                is_center = true;
                category_pos = position;
                MyApp.instance.getPreference().put(Constants.VOD_POS,category_pos);
                categoryListAdapter.selectItem(position);
                if(categories.get(category_pos).getName().toLowerCase().contains("adult") ||categories.get(category_pos).getName().toLowerCase().contains("adult")){
                    PinDlg pinDlg = new PinDlg(WelcomeActivity.this, new PinDlg.DlgPinListener() {
                        @Override
                        public void OnYesClick(Dialog dialog, String pin_code) {
                            String pin = (String ) MyApp.instance.getPreference().get(Constants.PIN_CODE);
                            if(pin_code.equalsIgnoreCase(pin)){
                                dialog.dismiss();
                                startActivity(new Intent(WelcomeActivity.this, PreviewVodActivity.class));
                            }else {
                                Toast.makeText(WelcomeActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void OnCancelClick(Dialog dialog, String pin_code) {
                            dialog.dismiss();
                        }
                    });
                    pinDlg.show();
                }else {
                    startActivity(new Intent(WelcomeActivity.this, PreviewVodActivity.class));
                }
                break;
        }
    }

    @Override
    public void onSwipe(int direction) {
        switch (direction) {
            case SimpleGestureFilter.SWIPE_RIGHT:
                RowLeft();
                break;
            case SimpleGestureFilter.SWIPE_LEFT:
                RowRight();
                break;
        }
    }

    @Override
    public void onDoubleTap() {

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

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    txt_time.setText(time.format(new Date()));
                    txt_date.setText(date.format(new Date()));
                } catch (Exception e) {
                }
            }
        });
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
    protected void onResume(){
        super.onResume();
        MyApp.is_welcome = true;
        if(MyApp.is_vpn){
            findViewById(R.id.ly_vpn).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.ly_vpn).setVisibility(View.GONE);
        }
        mConn = new VPNConnector(this, true) {
            @Override
            public void onUpdate(OpenVpnService service) {
            }
        };
    }

    private void showMultiScrenDlg(){
        MultiScreenDlg multiScreenDlg = new MultiScreenDlg(this, new MultiScreenDlg.DialogSearchListener() {
            @Override
            public void OnSearchClick(Dialog dialog, Integer position,Boolean is_clicked) {
                dialog.dismiss();
                if(is_clicked){
                    final Intent intent=new Intent(WelcomeActivity.this, MultiScreenActivity.class);
                    if (position==0) {
                        boolean remember_four=false;
                        if (MyApp.instance.getPreference().get("remember_four_screen")!=null) remember_four=(boolean) MyApp.instance.getPreference().get("remember_four_screen");
                        if (!remember_four){
                            PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(WelcomeActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                                @Override
                                public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                                    if(!pin_code.equals("9514")) {
                                        Toast.makeText(WelcomeActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    MyApp.instance.getPreference().put("remember_four_screen",is_remember);
                                    intent.putExtra("num_screen",4);
                                    startActivity(intent);
                                }

                                @Override
                                public void OnCancelClick(Dialog dialog, String pin_code) {

                                }
                            },remember_four);
                            pinMultiScreenDlg.show();
                        }else {
                            intent.putExtra("num_screen",4);
                            startActivity(intent);
                        }
                    }
                    else if (position==1) {
                        boolean remember_three=false;
                        if (MyApp.instance.getPreference().get("remember_three_screen")!=null) remember_three=(boolean) MyApp.instance.getPreference().get("remember_three_screen");
                        if (!remember_three){
                            PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(WelcomeActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                                @Override
                                public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                                    if(!pin_code.equals("9513")) {
                                        Toast.makeText(WelcomeActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    MyApp.instance.getPreference().put("remember_three_screen",is_remember);
                                    intent.putExtra("num_screen",3);
                                    startActivity(intent);
                                }

                                @Override
                                public void OnCancelClick(Dialog dialog, String pin_code) {

                                }
                            },remember_three);
                            pinMultiScreenDlg.show();
                        }else {
                            intent.putExtra("num_screen",3);
                            startActivity(intent);
                        }
                    }
                    else {
                        boolean remember_two=false;
                        if (MyApp.instance.getPreference().get("remember_two_screen")!=null) remember_two=(boolean) MyApp.instance.getPreference().get("remember_two_screen");
                        if (!remember_two){
                            PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(WelcomeActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                                @Override
                                public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                                    if(!pin_code.equals("9512")) {
                                        Toast.makeText(WelcomeActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    MyApp.instance.getPreference().put("remember_two_screen",is_remember);
                                    intent.putExtra("num_screen",2);
                                    startActivity(intent);
                                }

                                @Override
                                public void OnCancelClick(Dialog dialog, String pin_code) {

                                }
                            },remember_two);
                            pinMultiScreenDlg.show();
                        }else {
                            intent.putExtra("num_screen",2);
                            startActivity(intent);
                        }
                    }
                }else {
                    dialog.dismiss();
                }
            }
        });
        multiScreenDlg.show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            Toast.makeText(this,""+event.getKeyCode(),Toast.LENGTH_SHORT).show();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (!doubleBackToExitPressedOnce) {
                        this.doubleBackToExitPressedOnce = true;
                        Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doubleBackToExitPressedOnce = false;
                            }
                        }, 2000);
                        return true;
                    }
                    stopVPN();
                    finish();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    btn_left.setFocusable(false);
                    btn_right.setFocusable(false);
                    RowRight();
                    categroy_list.requestFocus();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    btn_left.setFocusable(false);
                    btn_right.setFocusable(false);
                    RowLeft();
                    categroy_list.requestFocus();
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                        if(txt_center.getText().toString().equalsIgnoreCase("setting")){
                            showSettingDlg();
                        }else if(txt_center.getText().toString().equalsIgnoreCase("guide")){
                            startActivity(new Intent(this, EPGActivityUpdated.class));
                        }else if (txt_center.getText().toString().equalsIgnoreCase("multi")){
//                            showScreenModeList();
                            showMultiScrenDlg();
                        }
//                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if(category_pos<categories.size()-1){
                        categroy_list.setSelection(category_pos);
                        category_pos++;
                        categoryListAdapter.selectItem(category_pos);
                    }else {
                        category_pos = 0;
                        categroy_list.setSelection(category_pos);
                        categoryListAdapter.selectItem(category_pos);
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if(category_pos>0){
                        categroy_list.setSelection(category_pos);
                        category_pos--;
                        categoryListAdapter.selectItem(category_pos);
                    }else {
                        category_pos = categories.size()-1;
                        categroy_list.setSelection(category_pos);
                        categoryListAdapter.selectItem(category_pos);
                        return true;
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
//        Log.e("last_pos",String .valueOf(channel_list.getLastVisiblePosition()));
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

}
