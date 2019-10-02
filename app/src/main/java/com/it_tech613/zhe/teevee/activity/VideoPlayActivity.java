package com.it_tech613.zhe.teevee.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;
import com.it_tech613.zhe.teevee.dialog.PackageDlg;
import com.it_tech613.zhe.teevee.models.MovieModel;
import com.it_tech613.zhe.teevee.utils.Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener, IVLCVout.Callback,View.OnClickListener{
    private MediaPlayer mMediaPlayer = null;
    public int mHeight;
    public int mWidth;
    LibVLC libvlc;
    private SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;
    SeekBar seekBar;
    LinearLayout bottom_lay, def_lay,ly_play,ly_resolution,ly_audio,ly_subtitle,ly_fav;
    ImageView img_play;


    MediaPlayer.TrackDescription[] traks;
    MediaPlayer.TrackDescription[] subtraks;
    String ratio;
    String[] resolutions;
    int current_resolution = 0;
    boolean first = true;

    TextView title_txt, start_txt, end_txt;
    ImageView imageView,image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker;
    String cont_url,title,rss="";
    int dration_time = 0,selected_item = 0,position,fav_pos = 0,msg_time = 0;
    List<MovieModel>vod_favlist;
    boolean is_create = true,is_long=false;
    boolean is_exit = false,is_rss = false,is_msg = false;
    List<String>  pkg_datas;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_player);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        pkg_datas = new ArrayList<>();
        for (int i = 0; i < getResources().getStringArray(R.array.package_list1).length; i++) {
            pkg_datas.add(getResources().getStringArray(R.array.package_list1)[i]);
        }
        if(MyApp.instance.getPreference().get(Constants.MOVIE_FAV)==null){
            vod_favlist = new ArrayList<>();
        }else {
            vod_favlist = (List<MovieModel>) MyApp.instance.getPreference().get(Constants.MOVIE_FAV);
        }
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottom_lay.getVisibility()== View.VISIBLE){
                    bottom_lay.setVisibility(View.GONE);
                }else {
                    bottom_lay.setVisibility(View.VISIBLE);
                    updateTimer();
                }
            }
        });
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.RGBX_8888);
        def_lay = (LinearLayout) findViewById(R.id.def_lay);
        bottom_lay = (LinearLayout) findViewById(R.id.vod_bottom_lay);
        ly_fav = findViewById(R.id.ly_fav);
        ly_fav.setOnClickListener(this);
        title_txt = (TextView) findViewById(R.id.vod_channel_title);
        imageView = (ImageView) findViewById(R.id.vod_channel_img);
        start_txt = (TextView) findViewById(R.id.vod_start_time);
        end_txt = (TextView) findViewById(R.id.vod_end_time);
        seekBar = (SeekBar) findViewById(R.id.vod_seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        remote_subtitles_surface = findViewById(R.id.remote_subtitles_surface);
        remote_subtitles_surface.setZOrderMediaOverlay(true);
        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        ly_audio = findViewById(R.id.ly_audio);

        ly_play = findViewById(R.id.ly_play);
        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);

        ly_subtitle.setOnClickListener(this);
        ly_play.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_subtitle.setOnClickListener(this);
        ly_audio.setOnClickListener(this);

        img_play = findViewById(R.id.img_play);

        txt_rss = findViewById(R.id.txt_rss);
        txt_rss.setSingleLine(true);

        image_icon = findViewById(R.id.image_icon);
        Picasso.with(this).load(Constants.GetIcon(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(image_icon);

        title_txt.setText(getIntent().getStringExtra("title"));
        cont_url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        try {
            Picasso.with(this).load(getIntent().getStringExtra("img"))
                    .placeholder(R.drawable.icon_default)
                    .error(R.drawable.icon_default)
                    .into(imageView);

        }catch (Exception e){
            Picasso.with(this).load(R.drawable.icon_default).into(imageView);
        }

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_HEIGHT = displayMetrics.heightPixels;
        int SCREEN_WIDTH = displayMetrics.widthPixels;
        holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        mHeight = displayMetrics.heightPixels;
        mWidth = displayMetrics.widthPixels;

        ratio = MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT;
        resolutions =  new String[]{"16:9", "4:3", ratio};

        playVideo(cont_url);
        FullScreencall();
        getRespond();
    }

    private void getRespond(){
        StringRequest request = new StringRequest(Constants.GetUrl(this)+"="+Constants.GetKey(this), new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    JSONObject object = new JSONObject(string);
                    if (((String) object.get("status")).equalsIgnoreCase("success")) {
                        String msg_on_off = (String) object.get("message_on_off");
                        is_msg = !msg_on_off.equalsIgnoreCase("0");
                        try {
                            msg_time = Integer.parseInt((String)object.get("message_time"));
                        }catch (Exception e){
                            msg_time = 20;
                        }
                        String rss_feed = "                 "+object.get("msg")+"                 ";
                        if(rss.equalsIgnoreCase(rss_feed)){
                            txt_rss.setVisibility(View.GONE);
                            is_rss = false;
                        }else {
                            rss =rss_feed;
                            is_rss = true;
                            txt_rss.setVisibility(View.VISIBLE);
                        }
                        Paint paint = new Paint();
                        paint.setTextSize(25);
                        paint.setColor(Color.BLACK);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTypeface(Typeface.DEFAULT);
                        Rect result = new Rect();
                        paint.getTextBounds(rss, 0, rss.length(), result);
                        txt_rss.setBackgroundResource(R.color.black);
                        int divide = (MyApp.SCREEN_WIDTH)/Utils.dp2px(VideoPlayActivity.this,result.width());
                        if(divide<1){
                            Log.e("rss1",rss);
                            if(is_msg){
                                image_icon.setVisibility(View.VISIBLE);
                                txt_rss.setSelected(true);
                                txt_rss.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                txt_rss.setText(rss);
                            }else {
                                txt_rss.setVisibility(View.GONE);
                                image_icon.setVisibility(View.GONE);
                            }
                        }else {
                            for(int i =0;i<divide+1;i++){
                                rss +=rss;
                            }
                            Log.e("rss2",rss);
                            if(is_msg){
                                image_icon.setVisibility(View.VISIBLE);
                                txt_rss.setSelected(true);
                                txt_rss.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                txt_rss.setText(rss);
                            }else {
                                txt_rss.setVisibility(View.GONE);
                                image_icon.setVisibility(View.GONE);
                            }

//                            txt_rss.setText(rss);
//                            txt_rss.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.marquee1));
                        }
                        rssTimer();
                    } else {
                        Toast.makeText(VideoPlayActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(VideoPlayActivity.this);
        rQueue.add(request);
    }
    int rss_time;
    private void rssTimer() {
        rss_time = msg_time;
        rssTicker = new Runnable() {
            public void run() {
                if (rss_time < 1) {
                    txt_rss.setText("");
                    txt_rss.setBackgroundResource(R.color.trans_parent);
                    txt_rss.setVisibility(View.GONE);
                    image_icon.setVisibility(View.GONE);
                    return;
                }
                runRssTicker();
            }
        };
        rssTicker.run();
    }

    private void runRssTicker() {
        rss_time --;
        long next = SystemClock.uptimeMillis() + 1000;
        rssHandler.postAtTime(rssTicker, next);
    }

    private void playVideo(String path) {
        if(def_lay.getVisibility()==View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
//        Log.e("url",path);
        toggleFullscreen(true);
        try {

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("0");//this option is used to show the first subtitle track
            options.add("--subsdec-encoding");

            libvlc = new LibVLC(this, options);

            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);


            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);

            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.setWindowSize(mWidth, mHeight);
            vout.addCallback(this);
            vout.attachViews();
//            vout.setSubtitlesView(tv_subtitle);


            Media m = new Media(libvlc, Uri.parse(path));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
            updateProgressBar();
            updateTimer();

        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }

    }
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mMediaPlayer != null) {
                if (traks == null && subtraks == null) {
                    first = false;
                    traks = mMediaPlayer.getAudioTracks();
                    subtraks = mMediaPlayer.getSpuTracks();
                }
                long totalDuration = mMediaPlayer.getLength();
                long currentDuration = mMediaPlayer.getTime();
                end_txt.setText("" + Utils.milliSecondsToTimer(totalDuration));
                start_txt.setText("" + Utils.milliSecondsToTimer(currentDuration));
                int progress = (int) (Utils.getProgressPercentage(currentDuration, totalDuration));
                seekBar.setProgress(progress);
                mHandler.postDelayed(this, 500);
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        if (!is_create) {
            if (libvlc != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
            surfaceView = (SurfaceView) findViewById(R.id.surface_view);
            holder = surfaceView.getHolder();
            holder.setFormat(PixelFormat.RGBX_8888);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int SCREEN_HEIGHT = displayMetrics.heightPixels;
            int SCREEN_WIDTH = displayMetrics.widthPixels;
            holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            mHeight = displayMetrics.heightPixels;
            mWidth = displayMetrics.widthPixels;
            playVideo(cont_url);
        } else {
            is_create = false;
        }
    }
    private void toggleFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("AUDIO_TRACK", 0);
        editor.commit();

        SharedPreferences pref2 = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = pref2.edit();
        editor1.putInt("SUB_TRACK", 0);
        releaseMediaPlayer();
    }
    @Override
    protected void onUserLeaveHint()
    {
        releaseMediaPlayer();
        finish();
        super.onUserLeaveHint();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }
    private void releaseMediaPlayer() {
        if (libvlc == null)
            return;

            mMediaPlayer.stop();
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mWidth = 0;
        mHeight = 0;
    }
    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VideoPlayActivity> mOwner;

        public MediaPlayerListener(VideoPlayActivity owner) {
            mOwner = new WeakReference<VideoPlayActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoPlayActivity player = mOwner.get();
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    //동영상 끝까지 재생되었다면..
                    player.releaseMediaPlayer();
                    player.is_create = false;
                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
//                    Toast.makeText(player, "Playing", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
//                    Toast.makeText(player, "Stop", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Buffering:
//                    Toast.makeText(player, "Buffering", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.EncounteredError:
//                    Toast.makeText(player, "Error", Toast.LENGTH_SHORT).show();
                    player.def_lay.setVisibility(View.VISIBLE);
                    break;

                //아래 두 이벤트는 계속 발생됨
                case MediaPlayer.Event.TimeChanged: //재생 시간 변화
                    break;
                case MediaPlayer.Event.PositionChanged: //동영상 재생 구간 변화시
                    //Log.d(TAG, "PositionChanged");
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:
                if (traks != null) {
                    if (traks.length > 0) {
                        showAudioTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.ly_subtitle:
                if (subtraks != null) {
                    if (subtraks.length > 0) {
                        showSubTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.ly_resolution:
                current_resolution++;
                if (current_resolution == resolutions.length)
                    current_resolution = 0;

                mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                break;

            case R.id.ly_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    img_play.setImageResource(R.drawable.exo_play);
                } else {
                    mMediaPlayer.play();
                    img_play.setImageResource(R.drawable.exo_pause);
                }
                break;
            case R.id.ly_fav:
                for(int i = 0;i<vod_favlist.size();i++){
                    if(vod_favlist.get(i).getStream_id().equalsIgnoreCase(MyApp.vod_model.getStream_id())){
                        pkg_datas.set(0,"Remove from Fav");
                        is_exit = true;
                        fav_pos = i;
                        break;
                    }else {
                        pkg_datas.set(0,"Add to Fav");
                    }
                }
                if(is_exit){
                    vod_favlist.remove(fav_pos);
                    Toast.makeText(this,"This movie has been removed from favorites.",Toast.LENGTH_SHORT).show();
                } else{
                    vod_favlist.add(MyApp.vod_model);
                    Toast.makeText(this,"This movie has been added to favorites.",Toast.LENGTH_SHORT).show();
                }
                MyApp.instance.getPreference().put(Constants.MOVIE_FAV,vod_favlist);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        long totalDuration = mMediaPlayer.getLength();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        mMediaPlayer.setTime(currentPosition);
        updateProgressBar();
    }


    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }


    private void updateTimer() {
        handler.removeCallbacks(mTicker);
        startTimer();
    }

    int maxTime;
    private void startTimer() {
        maxTime = 10;
        mTicker = new Runnable() {
            public void run() {
                if (maxTime < 1) {
                    if (bottom_lay.getVisibility() == View.VISIBLE)
                        bottom_lay.setVisibility(View.GONE);
                    return;
                }
                runNextTicker();
            }
        };
        mTicker.run();
    }

    private void runNextTicker() {
        maxTime --;
        long next = SystemClock.uptimeMillis() + 1000;
        handler.postAtTime(mTicker, next);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            long curr_pos = mMediaPlayer.getTime();
            long max_pos = mMediaPlayer.getLength();
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                            img_play.setImageResource(R.drawable.exo_play);
                        } else {
                            mMediaPlayer.play();
                            img_play.setImageResource(R.drawable.exo_pause);
                        }
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        updateTimer();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        dration_time += 30;
                        if (curr_pos < dration_time * 1000)
                            mMediaPlayer.setTime(1);
                        else {
                            long st = (long) (curr_pos - (long) dration_time * 1000);
                            mMediaPlayer.setTime(st);
                        }
                        dration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        dration_time += 30;
                        if (max_pos < dration_time * 1000)
                            mMediaPlayer.setTime((long) (max_pos - 10));
                        else mMediaPlayer.setTime((long) (curr_pos + (long) dration_time * 1000));
                        dration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        if(bottom_lay.getVisibility()==View.VISIBLE){
                            bottom_lay.setVisibility(View.GONE);
                            return true;
                        }
                        releaseMediaPlayer();
                        finish();
                        break;
                    case KeyEvent.KEYCODE_MENU:
                        for(int i = 0;i<vod_favlist.size();i++){
                            if(vod_favlist.get(i).getStream_id().equalsIgnoreCase(MyApp.vod_model.getStream_id())){
                                pkg_datas.set(0,"Remove from Fav");
                                is_exit = true;
                                fav_pos = i;
                                break;
                            }else {
                                pkg_datas.set(0,"Add to Fav");
                            }
                        }
                        PackageDlg packageDlg = new PackageDlg(VideoPlayActivity.this, pkg_datas, new PackageDlg.DialogPackageListener() {
                            @Override
                            public void OnItemClick(Dialog dialog, int position) {
                                dialog.dismiss();
                                is_long = false;
                                switch (position) {
                                    case 0:
                                        if(is_exit)
                                            vod_favlist.remove(fav_pos);
                                        else
                                            vod_favlist.add(MyApp.vod_model);
                                        MyApp.instance.getPreference().put(Constants.MOVIE_FAV,vod_favlist);
                                        break;
                                    case 1:
                                        if (subtraks != null) {
                                            if (subtraks.length > 0) {
                                                showSubTracksList();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case 2:
                                        if (traks != null) {
                                            if (traks.length > 0) {
                                                showAudioTracksList();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case 3:
                                        current_resolution++;
                                        if (current_resolution == resolutions.length)
                                            current_resolution = 0;

                                        mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                                        break;
                                }
                            }
                        });
                        packageDlg.show();
                        break;
                }
            }
        }catch (Exception e){

        }
        return super.dispatchKeyEvent(event);
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

    private void showAudioTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this);
        builder.setTitle("Audio track");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < traks.length; i++) {
            names.add(traks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("AUDIO_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("AUDIO_TRACK", selected_item);
                editor.commit();

                mMediaPlayer.setAudioTrack(traks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSubTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this);
        builder.setTitle("Subtitle");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < subtraks.length; i++) {
            names.add(subtraks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("SUB_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("SUB_TRACK", selected_item);
                editor.commit();
                mMediaPlayer.setSpuTrack(subtraks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
