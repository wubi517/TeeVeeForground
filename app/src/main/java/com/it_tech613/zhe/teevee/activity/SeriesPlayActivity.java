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

public class SeriesPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener, IVLCVout.Callback,View.OnClickListener{
    public int mHeight;
    public int mWidth;
    private MediaPlayer mMediaPlayer = null;
    LibVLC libvlc;
    public static SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;

    LinearLayout ly_play,ly_resolution,ly_audio,ly_subtitle;
    ImageView img_lock,img_play;


    MediaPlayer.TrackDescription[] traks;
    MediaPlayer.TrackDescription[] subtraks;
    String ratio;
    String[] resolutions ;
    int current_resolution = 0;
    boolean first = true;
    SeekBar seekBar;
    LinearLayout bottom_lay, def_lay;
    TextView title_txt, start_txt, end_txt;
    ImageView imageView,image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker;
    String cont_url,title,stream_id,img,name,rss="";
    int dration_time = 0,pos,position,selected_item = 0,msg_time = 0;
    List<MovieModel> movieModels;
    boolean is_create = true;
    List<String>  pkg_datas;
    boolean is_long =false,is_rss = false,is_msg = false;
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
        for (int i = 0; i < getResources().getStringArray(R.array.package_list2).length; i++) {
            pkg_datas.add(getResources().getStringArray(R.array.package_list2)[i]);
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
        pos = MyApp.episode_pos;
        movieModels = MyApp.movieModels;
        stream_id = movieModels.get(pos).getStream_id();
        img = movieModels.get(pos).getStream_icon();
        name =movieModels.get(pos).getName();
        String type = movieModels.get(pos).getType();
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.RGBX_8888);
        def_lay = (LinearLayout) findViewById(R.id.def_lay);
        bottom_lay = (LinearLayout) findViewById(R.id.vod_bottom_lay);
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

        title_txt.setText(name);
        cont_url = MyApp.instance.getIptvclient().buildSeriesStreamURL(MyApp.user,MyApp.pass,stream_id,type);
        title = name;
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
        new Thread(this::getRespond).start();
    }

    private void getRespond(){
        String url = "";
        url=Constants.GetKey(this);
        try{
            String response = MyApp.instance.getIptvclient().login(url);
            Log.e("response",response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("status")) {
                    JSONObject data_obj = object.getJSONObject("data");
                    String msg=data_obj.getString("message");
                    try {
                        msg_time = Integer.parseInt(data_obj.getString("message_time"));
                    }catch (Exception e){
                        msg_time = 20;
                    }
                    is_msg = !data_obj.getString("message_on_off").isEmpty() && data_obj.getString("message_on_off").equalsIgnoreCase("1");
                    if (msg.equals("")) msg=getString(R.string.app_name);
                    String finalMsg = msg;
                    runOnUiThread(()->{
                        String rss_feed = "                 "+ finalMsg +"                 ";
                        Paint paint = new Paint();
                        paint.setTextSize(25);
                        paint.setColor(Color.BLACK);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTypeface(Typeface.DEFAULT);
                        Rect result = new Rect();
                        paint.getTextBounds(rss_feed, 0, rss_feed.length(), result);
                        txt_rss.setBackgroundResource(R.color.black);
                        int divide = (MyApp.SCREEN_WIDTH)/Utils.dp2px(this,result.width());
                        if(divide<1){
                            if(rss.equalsIgnoreCase(rss_feed)){
                                image_icon.setVisibility(View.GONE);
                                txt_rss.setVisibility(View.GONE);
                                is_rss = false;
                            }else {
                                image_icon.setVisibility(View.VISIBLE);
                                rss =rss_feed;
                                is_rss = true;
                                txt_rss.setVisibility(View.VISIBLE);
                            }
                            Log.e("rss1",rss);
                            txt_rss.setSelected(true);
                            txt_rss.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            txt_rss.setText(rss);
                        }else {
                            for(int i =0;i<divide+1;i++){
                                rss_feed += rss_feed;
                            }
                            if(rss.equalsIgnoreCase(rss_feed)){
                                txt_rss.setVisibility(View.GONE);
                                is_rss = false;
                            }else {
                                rss =rss_feed;
                                is_rss = true;
                                txt_rss.setVisibility(View.VISIBLE);
                            }
                            Log.e("rss2",rss);
//                            txt_rss.setText(rss);
//                            txt_rss.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.marquee1));
                            txt_rss.setSelected(true);
                            txt_rss.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            txt_rss.setText(rss);
                        }
                        rssTimer();
                        if(is_msg){
                            txt_rss.setVisibility(View.VISIBLE);
                        }else {
                            txt_rss.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(this, "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }catch (Exception e){

        }

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
        if(def_lay.getVisibility()== View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
        toggleFullscreen(true);
        try {
            ArrayList<String> options = new ArrayList<String>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("0");//this option is used to show the first subtitle track
            options.add("--subsdec-encoding");

            libvlc = new LibVLC(this, options);
            holder.setKeepScreenOn(true);
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);

            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);
            vout.setWindowSize(mWidth, mHeight);
            vout.addCallback(this);
            vout.attachViews();
            Media m;
            m = new Media(libvlc, Uri.parse(path));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
            updateProgressBar();
            updateTimer();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
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
        private WeakReference<SeriesPlayActivity> mOwner;

        private MediaPlayerListener(SeriesPlayActivity owner) {
            mOwner = new WeakReference<SeriesPlayActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            SeriesPlayActivity player = mOwner.get();
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releaseMediaPlayer();
                    if(player.pos<player.movieModels.size()-1){
                        MyApp.episode_pos = player.pos+1;
                        player.pos = player.pos+1;
                        player.recreate();
                    }else {
                        player.releaseMediaPlayer();
                        player.finish();
                    }
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

                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
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
                if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                updateTimer();
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
                        PackageDlg packageDlg = new PackageDlg(SeriesPlayActivity.this, pkg_datas, new PackageDlg.DialogPackageListener() {
                            @Override
                            public void OnItemClick(Dialog dialog, int position) {
                                dialog.dismiss();
                                is_long = false;
                                switch (position) {
                                    case 0:
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
                                    case 1:
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
                                    case 2:
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesPlayActivity.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesPlayActivity.this);
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
