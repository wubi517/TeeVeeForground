package com.it_tech613.zhe.teevee.apps;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.it_tech613.zhe.teevee.models.CategoryModels;
import com.it_tech613.zhe.teevee.models.FullModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Constants {
    public static final String APP_INFO = "app_info";
    public static final String LOGIN_INFO = "login_info";
    public static final String FAV_INFO = "user_info";
    public static final String MAC_ADDRESS = "mac_addr";
    public static final String MOVIE_FAV = "movie_app";

    public static final String CHANNEL_POS = "channel_pos";
    public static final String SUB_POS ="sub_pos";
    public static final String VOD_POS = "vod_pos";
    public static final String SERIES_POS = "series_pos";
    public static final String PIN_CODE = "pin_code";
    public static final String OSD_TIME = "osd_time";
    public static final String IS_PHONE = "is_phone";
    public static final String EPG_OFFSET = "epg_offset";
    public static final String SORT = "sort";
    public static final String CURRENT_PLAYER = "current_player";
    private static final String RECENT_CHANNELS="RECENT_CHANNELS";
    private static final String RECENT_MOVIES="RECENT_MOVIES";
    private static final String RECENT_SERIES="RECENT_SERIES";
    public static String  recent_id = "1000";
    public static String all_id = "100";
    public static String fav_id = "200";
    public static String All="All";
    public static String Favorites="Favorites";
    public static final String TIME_FORMAT = "time_format";
    public static final String INVISIBLE_VOD_CATEGORIES = "invisible_vod_categories";
    public static final String INVISIBLE_LIVE_CATEGORIES = "invisible_live_categories";
    public static final String INVISIBLE_SERIES_CATEGORIES = "invisible_series_categories";
    public static final String DIRECT_VPN_PIN_CODE = "direct_vpn_pin_code";
    public static String Recently_Viewed = "Recently Viewed";
    public static SimpleDateFormat stampFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat clockFormat=new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat clock12Format=new SimpleDateFormat("hh:mm");
    public static String id1 = "test_channel_01";
    public static String getRecentChannels(){
        return RECENT_CHANNELS;
    }
    public static String getRecentMovies(){
        return RECENT_MOVIES;
    }

    public static String getRecentSeries(){
        return RECENT_SERIES;
    }
    public static FullModel getRecentFullModel(List<FullModel> fullModels){
        for (FullModel fullModel:fullModels){
            if (fullModel.getCategory().equals(Constants.recent_id))
                return fullModel;
        }
        return null;
    }
    public static CategoryModels getRecentCatetory(List<CategoryModels> categoryModels){
        for (CategoryModels categoryModel:categoryModels){
            if (categoryModel.getId().equals(Constants.recent_id))
                return categoryModel;
        }
        return null;
    }
    public static long SEVER_OFFSET;
    public static void setServerTimeOffset(String my_timestamp,String server_timestamp) {
        Log.e("server_timestamp",server_timestamp);
        try {
            long my_time= Long.parseLong(my_timestamp);
            Date date_server= stampFormat.parse(server_timestamp);
            my_time=my_time*1000;
            SEVER_OFFSET=my_time-date_server.getTime();
            Log.e("offset",String.valueOf(SEVER_OFFSET));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static String Offset(boolean is_12,String string){
        try {
            Date that_date=stampFormat.parse(string);
            that_date.setTime(that_date.getTime()+ Constants.SEVER_OFFSET);
            if (is_12)return clock12Format.format(that_date);
            else return clockFormat.format(that_date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String GetAppDomain(Context mcontext)
    {
        String base_url="";
        SharedPreferences serveripdetails = mcontext.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        base_url=serveripdetails.getString("ip","");
        return  base_url;
    }

    public static String GetIcon(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("icon_image","");
        return  app_icon;
    }

    public static String GetLoginImage(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("login_image","");
        return  app_icon;
    }

    public static String GetMainImage(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("main_bg","");
        return  app_icon;
    }

    public static String GetAd1(Context mcontext)
    {
        String base_url="";
        SharedPreferences serveripdetails = mcontext.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        base_url=serveripdetails.getString("ad1","");
        return  base_url;
    }

    public static String GetAd2(Context mcontext)
    {
        String base_url="";
        SharedPreferences serveripdetails = mcontext.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        base_url=serveripdetails.getString("ad2","");
        return  base_url;
    }
    public static String GetAutho1(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("autho1","");
        return  app_icon;
    }
    public static String GetAutho2(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("autho2","");
        return  app_icon;
    }
    public static String GetList(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("list","");
        return  app_icon;
    }
    public static String GetGrid(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("grid","");
        return  app_icon;
    }
    public static String GetUrl(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("url","");
        return  app_icon;
    }

    public static String GetKey(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("key","");
        return  app_icon;
    }

    public static String GetCurrentTimeByTimeZone(String pattern,String time_zone) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone(time_zone)); //
        return df.format(c.getTime());
    }

    public static String GetCurrentDateByTimeZone(String pattern,String time_zone) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone(time_zone)); //
        return df.format(c.getTime());
    }


    public static String GetCurrentTime(String pattern) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(c.getTime());
    }

    public static String GetCurrentDate(String pattern) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(c.getTime());
    }

    public static long getTimeDiffMinutes(String start_time, String end_time, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date d1 = null;
        Date d2 = null;
        long diffMinutes = 0;
        try {
            d1 = format.parse(start_time);
            d2 = format.parse(end_time);

            long diff = d2.getTime() - d1.getTime();
            diffMinutes = diff / (60 * 1000);
            return diffMinutes;

        } catch (Exception e) {
            e.printStackTrace();
            return diffMinutes;
        }
    }

    public static boolean checktimings(String current_time, String endtime, String pattern) {

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(current_time);
            Date date2 = sdf.parse(endtime);

            if (date1.before(date2)) {
                return true;
            } else {

                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String GetDecodedString(String text) {
        return text;
    }
}
