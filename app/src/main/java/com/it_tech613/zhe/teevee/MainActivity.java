package com.it_tech613.zhe.teevee;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.it_tech613.zhe.teevee.activity.LoginAcitivity;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;
import com.it_tech613.zhe.teevee.dialog.UpdateDlg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SharedPreferences serveripdetails;
    String version,app_Url;
    static {
        System.loadLibrary("notifications");
    }
    public native String getOne();
    public native String getTwo();
    public native String getThree();
    public native String getFour();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if(MyApp.instance.getPreference().get(Constants.PIN_CODE)==null){
            MyApp.instance.getPreference().put(Constants.PIN_CODE,"0000");
        }
        if(MyApp.instance.getPreference().get(Constants.OSD_TIME)==null){
            MyApp.instance.getPreference().put(Constants.OSD_TIME,7);
        }

        if (MyApp.instance.getPreference().get(Constants.DIRECT_VPN_PIN_CODE)==null){
            MyApp.instance.getPreference().put(Constants.DIRECT_VPN_PIN_CODE,"8888");
        }
        if(MyApp.instance.getPreference().get(Constants.CURRENT_PLAYER)==null){
            MyApp.instance.getPreference().put(Constants.CURRENT_PLAYER,0);
        }
        if(MyApp.instance.getPreference().get(Constants.SORT)==null){
            MyApp.instance.getPreference().put(Constants.SORT,0);
        }
        if(MyApp.instance.getPreference().get(Constants.TIME_FORMAT)==null){
            MyApp.instance.getPreference().put(Constants.TIME_FORMAT,"12hour");
        }
        serveripdetails = this.getSharedPreferences("serveripdetails", Context.MODE_PRIVATE);

        try {
            SharedPreferences.Editor server_editor = serveripdetails.edit();
            server_editor.putString("url",new String(Base64.decode(getTwo(),Base64.DEFAULT)));
            server_editor.putString("key",new String (Base64.decode(getOne(), Base64.DEFAULT)));
            server_editor.apply();
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
        }

        try {
            SharedPreferences.Editor server_editor = serveripdetails.edit();
            server_editor.putString("autho1",new String (Base64.decode(new String(Base64.decode(getFour().substring(12),Base64.DEFAULT)).substring(12),Base64.DEFAULT)));
            server_editor.putString("autho2",new String (Base64.decode(new String(Base64.decode(getThree().substring(12),Base64.DEFAULT)).substring(12),Base64.DEFAULT)));
            server_editor.apply();
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            CheckSDK23Permission();
        } else {
            getRespond();
        }
        FullScreencall();
    }

    public void FullScreencall() {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void getRespond(){
        try {
            String response = MyApp.instance.getIptvclient().login(Constants.GetKey(this));
            Log.e("response",response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("status")) {
                    JSONObject data_obj = object.getJSONObject("data");
                    String url = (String) data_obj.get("url");
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        if (url.endsWith("/")){
                            url = url;
                        }else {
                            url = url+"/";
                        }
                        JSONArray array = data_obj.getJSONArray("image_urls");
                        SharedPreferences.Editor server_editor = serveripdetails.edit();
                        server_editor.putString("ip", url);
                        version = (String )data_obj.get("version");
                        app_Url = (String )data_obj.get("app_url");
                        String dual_screen=data_obj.getString("pin_2");
                        String tri_screen=data_obj.getString("pin_3");
                        String four_way_screen=data_obj.getString("pin_4");
                        server_editor.putString("dual_screen",dual_screen);
                        server_editor.putString("tri_screen",tri_screen);
                        server_editor.putString("four_way_screen",four_way_screen);
                        server_editor.putString("icon_image",(String) array.get(0));
                        server_editor.putString("main_bg",(String )array.get(1));
                        server_editor.putString("login_image",(String) array.get(2));
                        server_editor.putString("ad1",(String)array.get(3));
                        server_editor.putString("ad2",(String )array.get(4));
                        server_editor.commit();
                        getUpdate();
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid Server URL!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }catch (Exception e){

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void CheckSDK23Permission() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("READ / WRITE SD CARD");
        if (!addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE))
            permissionsNeeded.add("READPHONE");
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    124);
            return;
        } else {
            getRespond();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("PermissionsResult", "onRequestPermissionsResult");
        getRespond();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getUpdate(){
        MyApp.instance.versionCheck();
        double code = 0.0;
        try {
            code = Double.parseDouble(version);
        }catch (Exception e){
            code = 0.0;
        }
        MyApp.instance.loadVersion();
        double app_vs = Double.parseDouble(MyApp.version_name);
        if (code > app_vs) {
            UpdateDlg updateDlg = new UpdateDlg(this, new UpdateDlg.DialogUpdateListener() {
                @Override
                public void OnUpdateNowClick(Dialog dialog) {
                    dialog.dismiss();
                    new versionUpdate().execute(app_Url);
                }
                @Override
                public void OnUpdateSkipClick(Dialog dialog) {
                    dialog.dismiss();
                    startActivity(new Intent(MainActivity.this, LoginAcitivity.class));
                    finish();
                }
            });
            updateDlg.show();
        }else {
            startActivity(new Intent(MainActivity.this, LoginAcitivity.class));
            finish();
        }
    }
    class versionUpdate extends AsyncTask<String, Integer, String> {
        ProgressDialog mProgressDialog;
        File file;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage(getResources().getString(R.string.request_download));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                String destination = Environment.getExternalStorageDirectory() + "/";
                String fileName = "supanewui.apk";
                destination += fileName;
                final Uri uri = Uri.parse("file://" + destination);
                file = new File(destination);
                if(file.exists()){
                    file.delete();
                }
                output = new FileOutputStream(file, false);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(getApplicationContext(),"Update Failed",Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, LoginAcitivity.class));
                finish();
            } else
                startInstall(file);
        }
    }

    private void startInstall(File fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(MainActivity.this,BuildConfig.APPLICATION_ID + ".provider",fileName), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
