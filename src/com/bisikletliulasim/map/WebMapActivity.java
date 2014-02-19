package com.bisikletliulasim.map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.parse.ParseAnalytics;
import com.parse.PushService;

public class WebMapActivity extends Activity implements LocationListener {

    private static String MAP_URL = "http://bisikletliulasim.com/app/mobile_maps.html";
    private static String INFO_URL = "http://bisikletliulasim.com/app/hakkimizda.html";
    private static String NO_CONNECTION = "file:///android_asset/no_connection.html";
    private WebView webView;
    private Location mostRecentLocation;

    public boolean isDeviceConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;

    }

    @Override
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        PushService.setDefaultPushCallback(this, WebMapActivity.class);
        ParseAnalytics.trackAppOpened(getIntent());
        setupWebView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setupWebView(){
        String url;
        Double lat, lon;
        getLocation();

        if (!isDeviceConnectedToInternet()) {
            url = NO_CONNECTION;
        }
        else{
            url = MAP_URL;
        }

        if (mostRecentLocation == null){
            lat = 41.00;
            lon = 28.97;
        }
        else{
            lat = mostRecentLocation.getLatitude();
            lon = mostRecentLocation.getLongitude();
        }

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        final String centerJS = "javascript:main(" + lat + "," +  lon + ")";

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url)
            {
                webView.loadUrl(centerJS);
            }

        });
        webView.loadUrl(url);

    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = locationManager.getBestProvider(criteria,true);

        locationManager.requestLocationUpdates(provider, 1, 0, this);
        mostRecentLocation = locationManager.getLastKnownLocation(provider);
     }

    /** Sets the mostRecentLocation object to the current location of the device **/
    @Override
    public void onLocationChanged(Location location) {
        mostRecentLocation = location;

        Double lat = mostRecentLocation.getLatitude();
        Double lon = mostRecentLocation.getLongitude();

        final String newLocationJS = "javascript:changedLocation(" + lat + "," +  lon + ")";

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(newLocationJS);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupWebView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                openInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openInfo(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.info_screen);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
					dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


}