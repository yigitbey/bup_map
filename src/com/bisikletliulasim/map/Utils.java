package com.bisikletliulasim.map;

import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils{
    public static String LOG_TAG = Constants.LOG_TAG;

    public static JSONObject LoadJsonFromURL(String s_url){
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            // Connect to the web service
            URL url = new URL(s_url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Read the JSON data into the StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        JSONObject json_object = new JSONObject();
        try{
            json_object = new JSONObject(json.toString());
        }
        catch (JSONException ex){
            Log.e(LOG_TAG,ex.toString());
        }

        return json_object;
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = BUPApplication.context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}