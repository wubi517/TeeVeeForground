package com.it_tech613.zhe.teevee.apps;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.it_tech613.zhe.teevee.BuildConfig;
import com.it_tech613.zhe.teevee.MyForeGroundService;
import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.models.CategoryModels;
import com.it_tech613.zhe.teevee.models.FullModel;
import com.it_tech613.zhe.teevee.models.LoginModel;
import com.it_tech613.zhe.teevee.models.MovieModel;
import com.it_tech613.zhe.teevee.models.SeriesModel;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadAction;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadAction;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadAction;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import iptvclient.ApiClient;
import iptvclient.Iptvclient;

import static com.it_tech613.zhe.teevee.apps.Constants.id1;

public class MyApp extends MultiDexApplication {
    public static MyApp instance;
    private MyPreference preference;
    public static LoginModel loginModel;
    public static List<FullModel> fullModels = new ArrayList<>();
    public static List<CategoryModels> vod_categories=new ArrayList<>();
    public static List<CategoryModels> live_categories=new ArrayList<>();
    public static List<CategoryModels> series_categories=new ArrayList<>();
    public static List<FullModel> fullModels_filter = new ArrayList<>();
    public static List<CategoryModels> vod_categories_filter = new ArrayList<>();
    public static List<CategoryModels> live_categories_filter = new ArrayList<>();
    public static List<CategoryModels> series_categories_filter = new ArrayList<>();
    public static List<MovieModel> movieModels;
    public static List<MovieModel>movieModels0;
    public static List<MovieModel> recentMovieModels = new ArrayList<>();
    public static List<SeriesModel>seriesModels = new ArrayList<>();
    public static List<SeriesModel> recentSeriesModels = new ArrayList<>();
    public static  MovieModel vod_model;

    public static List<String> maindatas;
    private ApiClient iptvclient;
    public static Map backup_map;
    public static String version_name,mac_address,version_str,user,pass,created_at,status,is_trail,active_cons,max_cons;
    public static int SCREEN_WIDTH, SCREEN_HEIGHT, ITEM_V_WIDTH, ITEM_V_HEIGHT,SURFACE_WIDTH,SURFACE_HEIGHT,top_margin,right_margin,
            channel_size,episode_pos,EPG_WIDTH,EPG_HEIGHT,EPG_TOP,EPG_RIGHT;
    public static boolean is_welcome,is_first,key,touch = false,is_vpn;


    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;
    private static final DownloadAction.Deserializer[] DOWNLOAD_DESERIALIZERS =
            new DownloadAction.Deserializer[] {
                    DashDownloadAction.DESERIALIZER,
                    HlsDownloadAction.DESERIALIZER,
                    SsDownloadAction.DESERIALIZER,
                    ProgressiveDownloadAction.DESERIALIZER
            };

    protected String userAgent;

    private File downloadDirectory;
    private Cache downloadCache;
    private DownloadManager downloadManager;
    private DownloadTracker downloadTracker;

    public MyPreference getPreference() {
        return preference;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preference = new MyPreference(getApplicationContext(), Constants.APP_INFO);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String p = pref.getString("set_locale", "");
        if (!p.equals("")) {
            Locale locale;
            // workaround due to region code
            if(p.equals("zh-TW")) {
                locale = Locale.TRADITIONAL_CHINESE;
            } else if(p.startsWith("zh")) {
                locale = Locale.CHINA;
            } else if(p.equals("pt-BR")) {
                locale = new Locale("pt", "BR");
            } else if(p.equals("bn-IN") || p.startsWith("bn")) {
                locale = new Locale("bn", "IN");
            } else {
                /**
                 * Avoid a crash of
                 * java.lang.AssertionError: couldn't initialize LocaleData for locale
                 * if the user enters nonsensical region codes.
                 */
                if(p.contains("-"))
                    p = p.substring(0, p.indexOf('-'));
                locale = new Locale(p);
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config,
                    getResources().getDisplayMetrics());
        }

        instance = this;
        iptvclient = Iptvclient.newApiClient();
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        getScreenSize();
        createchannel();
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(24*3600*1000);
                    doWork();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {
                }
            }
        }
    }
    public void doWork() {
        Log.e("update","started");
//        if (MyApp.instance.getPreference().get(Constants.LOGIN_INFO1)!=null) startActivity(new Intent(this, SplashActivity.class));
//        else startActivity(new Intent(this, LoginAcitivity.class));
        Intent number5 = new Intent(getBaseContext(), MyForeGroundService.class);
        number5.putExtra("times", 5);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(number5);
        } else {
            //lower then Oreo, just start the service.
            startService(number5);
        }
    }
    private void createchannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel mChannel = new NotificationChannel(id1,
                    getString(R.string.channel_name),  //name of the channel
                    NotificationManager.IMPORTANCE_LOW);   //importance level
            //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
            // Configure the notification channel.
            mChannel.setDescription(getString(R.string.channel_description));
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setShowBadge(true);
            nm.createNotificationChannel(mChannel);
        }
    }
    private void getScreenSize() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        SCREEN_WIDTH = displayMetrics.widthPixels;
        SCREEN_HEIGHT = displayMetrics.heightPixels;
        if(SCREEN_WIDTH < SCREEN_HEIGHT){
            int a = SCREEN_WIDTH;
            SCREEN_WIDTH = SCREEN_HEIGHT;
            SCREEN_HEIGHT = a;
        }
        SURFACE_WIDTH = (int)(SCREEN_WIDTH/2.65);
        SURFACE_HEIGHT = (int)(SURFACE_WIDTH*0.6);
        top_margin = SCREEN_HEIGHT/7;
        right_margin = SCREEN_WIDTH/14;
        ITEM_V_WIDTH = (int) (SCREEN_WIDTH /8);
        ITEM_V_HEIGHT = (int) (ITEM_V_WIDTH * 1.6);

        EPG_WIDTH = (int)(SCREEN_WIDTH/4);
        EPG_HEIGHT = (int)(EPG_WIDTH*0.65);
        EPG_TOP = SCREEN_HEIGHT/8;
        EPG_RIGHT = SCREEN_WIDTH/20;
    }

    public void loadVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version_name = pInfo.versionName;
    }

    public void versionCheck(){
        if (android.os.Build.VERSION.SDK_INT > 11) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }


    /** Returns a {@link DataSource.Factory}. */
    public DataSource.Factory buildDataSourceFactory(TransferListener<? super DataSource> listener) {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(this, listener, buildHttpDataSourceFactory(listener));
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    /** Returns a {@link HttpDataSource.Factory}. */
    public HttpDataSource.Factory buildHttpDataSourceFactory(
            TransferListener<? super DataSource> listener) {
        return new DefaultHttpDataSourceFactory(userAgent, listener);
    }

    /** Returns whether extension renderers should be used. */
    public boolean useExtensionRenderers() {
        return "withExtensions".equals(BuildConfig.FLAVOR);
    }

    public DownloadManager getDownloadManager() {
        initDownloadManager();
        return downloadManager;
    }

    public DownloadTracker getDownloadTracker() {
        initDownloadManager();
        return downloadTracker;
    }

    private synchronized void initDownloadManager() {
        if (downloadManager == null) {
            DownloaderConstructorHelper downloaderConstructorHelper =
                    new DownloaderConstructorHelper(
                            getDownloadCache(), buildHttpDataSourceFactory(/* listener= */ null));
            downloadManager =
                    new DownloadManager(
                            downloaderConstructorHelper,
                            MAX_SIMULTANEOUS_DOWNLOADS,
                            DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                            new File(getDownloadDirectory(), DOWNLOAD_ACTION_FILE),
                            DOWNLOAD_DESERIALIZERS);
            downloadTracker =
                    new DownloadTracker(
                            /* context= */ this,
                            buildDataSourceFactory(/* listener= */ null),
                            new File(getDownloadDirectory(), DOWNLOAD_TRACKER_ACTION_FILE),
                            DOWNLOAD_DESERIALIZERS);
            downloadManager.addListener(downloadTracker);
        }
    }

    private synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }

    public ApiClient getIptvclient() {
        return iptvclient;
    }

}
