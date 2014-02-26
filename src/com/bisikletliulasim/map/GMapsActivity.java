package com.bisikletliulasim.map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;

import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseAnalytics;
import com.parse.PushService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GMapsActivity extends Activity implements LocationListener {

    private static String NO_CONNECTION = "file:///android_asset/no_connection.html";

    private static final String LOG_TAG = "BUPHarita";

    private static final String REPAIRSHOP_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20bisiklet_tamircileri&format=GEOJson";
    private static final String BDI_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20bisiklet_dostu_isletmeler&format=GEOJson";
    private static final String RENT_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27kiralama%27)&format=GEOJson";
    private static final String PARK_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27park%27)&format=GEOJson";
    private static final String DRAIN_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27mazgal%27)&format=GEOJson";
    private static final String FERRY_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27iskele%27)&format=GEOJson";
    private static final String PUBLIC_TRANSPORT_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27toplutasima%27)&format=GEOJson";
    private static final String ROADS_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20mevcut_bisiklet_yollari&format=GEOJson";

    HashMap json_urls = new HashMap();

    private Location mostRecentLocation;

    private GoogleMap map;

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

        setContentView(R.layout.gmaps);
        PushService.setDefaultPushCallback(this, GMapsActivity.class);
        ParseAnalytics.trackAppOpened(getIntent());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        json_urls.put(Constants.REPAIRSHOP, REPAIRSHOP_JSON);
        json_urls.put(Constants.BDI, BDI_JSON);
        json_urls.put(Constants.RENT, RENT_JSON);
        json_urls.put(Constants.PARK, PARK_JSON);
        json_urls.put(Constants.DRAIN, DRAIN_JSON);
        json_urls.put(Constants.FERRY, FERRY_JSON);
        json_urls.put(Constants.PUBLIC_TRANSPORT, PUBLIC_TRANSPORT_JSON);

        getLocation();
        setupMap();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void setupMap(){
        // Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        Double lat = mostRecentLocation.getLatitude();
        Double lon = mostRecentLocation.getLongitude();
        LatLng currentLocation = new LatLng(lat, lon);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));

        new Thread(new Runnable() {
            public void run() {
                Iterator it = json_urls.entrySet().iterator();
                while (it.hasNext()){
                    try {
                        Map.Entry pairs = (Map.Entry) it.next();
                        getJson(pairs.getValue().toString(), Integer.parseInt(pairs.getKey().toString()));
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Cannot retrive json", e);
                        return;
                    }
                    catch (JSONException ex){
                        Log.e(LOG_TAG, "Cannot retrive json", ex);
                        return;
                    }
                }

            }
        }).start();

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

    }

    @Override
    public void onResume() {
        super.onResume();
        setupMap();
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

    protected void getJson(String json_url, int type) throws IOException, JSONException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            // Connect to the web service
            URL url = new URL(json_url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Read the JSON data into the StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
            throw new IOException("Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        final JSONObject jsonobj = new JSONObject(json.toString());
        jsonobj.put("type", type);

        // Create markers for the city data.
        // Must run this on the UI thread since it's a UI operation.
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    createMarkersFromJson(jsonobj.toString());
                } catch (JSONException e) {

                    Log.e(LOG_TAG, "Error processing JSON: " + e.toString());
                }
            }
        });
    }

    JSONObject generateMarkerInfo(int type, JSONObject properties) throws JSONException{
        JSONObject marker_info = new JSONObject();
        if (type == Constants.REPAIRSHOP){
            String title = properties.getString("name");
            String address = properties.getString("adres");
            address = address.replaceAll("\\s+", " ");
            String web = properties.getString("web");
            String mobile = properties.getString("telefon");

            marker_info.put("title", title);
            marker_info.put("address", address);
            marker_info.put("web", web);
            marker_info.put("mobile", mobile);
            marker_info.put("type", type);
        }
        else if (type == Constants.BDI){
            String title = properties.getString("name");
            String address = properties.getString("adres");
            address = address.replaceAll("\\s+", " ");
            String web = properties.getString("web");
            String mobile = properties.getString("telefon");
            String advantage = properties.getString("kampanya");

            marker_info.put("title", title);
            marker_info.put("address", address);
            marker_info.put("web", web);
            marker_info.put("mobile", mobile);
            marker_info.put("type", type);
            marker_info.put("campaign", advantage);
        }
        else if (type == Constants.DRAIN ){
            String title = properties.getString("name");
            String topImage = properties.getString("resim");
            String description = properties.getString("description");
            description = description.trim().replaceAll(" +", " ");


            marker_info.put("title", title);
            marker_info.put("description", description);
            marker_info.put("image", topImage);
            marker_info.put("updated", false);
            marker_info.put("type", type);
        }

        else if (type == Constants.RENT || type == Constants.PARK || type == Constants.FERRY || type == Constants.PUBLIC_TRANSPORT){
            String title = properties.getString("name");
            String description = properties.getString("description");
            description = description.trim().replaceAll(" +", " ");

            marker_info.put("title", title);
            marker_info.put("description", description);
            marker_info.put("type", type);

        }

        return marker_info;
    }

    void createMarkersFromJson(String json) throws JSONException {
        // De-serialize the JSON string into an array of city objects
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("features");
        int marker_type = jsonObject.getInt("type");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);

            JSONObject propertiesObj = jsonObj.getJSONObject("properties");
            JSONObject geometryObj = jsonObj.getJSONObject("geometry");

            Double lat = geometryObj.getJSONArray("coordinates").getDouble(1);
            Double lon = geometryObj.getJSONArray("coordinates").getDouble(0);
            JSONObject marker_info = generateMarkerInfo(marker_type, propertiesObj);

            InfoWindowAdapter window = info_window();
            map.setInfoWindowAdapter(window);

            Marker marker = map.addMarker(
                    new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(Constants.MARKERS[marker_type]))
                            .position(new LatLng(lat, lon))
                            .snippet(marker_info.toString())
            );

            marker_info.put("id", marker.getId());
            marker.setSnippet(marker_info.toString());
        }
    }

    public InfoWindowAdapter info_window(){
        InfoWindowAdapter window = new InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                if (marker != null
                        && marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                    marker.showInfoWindow();
                }
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker){
                View v = new View(getApplicationContext());
                try{
                    final JSONObject marker_info = new JSONObject(marker.getSnippet());

                    v = getLayoutInflater().inflate(R.layout.marker_bubble, null);

                    TextView title = (TextView) v.findViewById(R.id.title);
                    title.setText(marker_info.getString("title"));

                    map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            try{
                                Intent infoIntent = new Intent(GMapsActivity.this, InfoWindowActivity.class);
                                for (Iterator<String> key = marker_info.keys(); key.hasNext();){
                                    String key_name = key.next();
                                    infoIntent.putExtra(key_name, marker_info.getString(key_name));
                                }
                                infoIntent.putExtra("type", marker_info.getInt("type"));
                                startActivity(infoIntent);
                            }
                            catch (JSONException ex){
                                Log.e(LOG_TAG, ex.toString());
                            }

                        }
                    });

                }
                catch (JSONException ex){
                        Log.e(LOG_TAG, ex.toString());
                }
                return v;
            }
        };
        return window;
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