package com.it_tech613.zhe.teevee.async;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import converter.JsonUtils;

public class GetAsyncTask extends AsyncTask<String, Void, Map> {

    OnGetResultsListener listener;

    private Context context;
    int request_code;

    public GetAsyncTask(Context context, int request_code) {
        this.context = context;
        this.request_code = request_code;
    }

    public void onGetResultsData(OnGetResultsListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected Map doInBackground(String... params) {
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(params[0]);
            Log.e("url",params[0]);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.setDoInput(true);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setConnectTimeout(10000);
            httpUrlConn.connect();

            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            String res = buffer.toString();
//            Log.e("result", res);
            Map map = JsonUtils.stringObject(res, Map.class);
            return map;
        } catch (MalformedURLException e) {
            return null;
        } catch (ProtocolException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Map response) {
        if (response != null && response.size() > 0) {
            listener.onGetResultsData(response, request_code);
        } else {
            listener.onGetResultsData(response, request_code);
        }
    }

    public interface OnGetResultsListener {
        public void onGetResultsData(Map map, int request_code);
    }
}
