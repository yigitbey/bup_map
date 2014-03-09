package com.bisikletliulasim.map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.analytics.tracking.android.EasyTracker;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.parse.ParseAnalytics;
import com.parse.PushService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GMapsActivity extends Activity implements LocationListener {

    private static final String LOG_TAG = "BUPHarita";
    HashMap json_urls = new HashMap();
    private Location mostRecentLocation;
    private GoogleMap map;
    HashMap bus_polylines = new HashMap();
    HashMap pt_markers = new HashMap();
    Menu top_menu;
    Polyline directions_route = null;
    Double active_marker_lat = null;
    Double active_marker_lon = null;
    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int gms_enabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(BUPApplication.context);
        if (gms_enabled != ConnectionResult.SUCCESS){
            Log.e(LOG_TAG,"GMS not available");
            return;
        }

        setContentView(R.layout.gmaps);

        getLocation();

        json_urls.put(Constants.REPAIRSHOP, Constants.REPAIRSHOP_JSON);
        json_urls.put(Constants.BDI, Constants.BDI_JSON);
        json_urls.put(Constants.RENT, Constants.RENT_JSON);
        json_urls.put(Constants.PARK, Constants.PARK_JSON);
        json_urls.put(Constants.DRAIN, Constants.DRAIN_JSON);
        json_urls.put(Constants.FERRY, Constants.FERRY_JSON);
        json_urls.put(Constants.PUBLIC_TRANSPORT, Constants.PUBLIC_TRANSPORT_JSON);
        json_urls.put(Constants.TRANSPORT_ROADS, Constants.TRANSPORT_ROADS_JSON);
        json_urls.put(Constants.LEISURE_ROADS, Constants.LEISURE_ROADS_JSON);
        json_urls.put(Constants.IETT_ROADS, Constants.IETT_ROADS_JSON);

        setupMap();

        Intent thisIntent = getIntent();
        int action = thisIntent.getIntExtra("action",0);

        if (action == 1){
            active_marker_lat = thisIntent.getDoubleExtra("lat",0);
            active_marker_lon = thisIntent.getDoubleExtra("lon",0);
            String title = thisIntent.getStringExtra("title");
            TextView title_label = (TextView) findViewById(R.id.title_label);
            title_label.setText(title);
            getDirections(active_marker_lat, active_marker_lon);
        }

        PushService.setDefaultPushCallback(this, GMapsActivity.class);
        ParseAnalytics.trackAppOpened(getIntent());


    }

    private void setupMap () throws NullPointerException{
        Double lat;
        Double lon;

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        if (mostRecentLocation == null){
            lat = 41.01079;
            lon = 29.00877;
        }
        else{
            lat = mostRecentLocation.getLatitude();
            lon = mostRecentLocation.getLongitude();
        }

        LatLng currentLocation = new LatLng(lat, lon);

        map.setPadding(0, Utils.dpToPx(50), 0, 0);
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        new Thread(new Runnable() {
            public void run() {
                Iterator it = json_urls.entrySet().iterator();
                while (it.hasNext()){
                    try {
                        Map.Entry pairs = (Map.Entry) it.next();
                        getJson(pairs.getValue().toString(), Integer.parseInt(pairs.getKey().toString()));
                    }
                    catch (IOException e) {
                        Log.e(LOG_TAG, "Cannot retrieve json", e);
                        return;
                    }
                    catch (JSONException ex){
                        Log.e(LOG_TAG, "Cannot retrieve json", ex);
                        return;
                    }
                }

            }
        }).start();

    }

    public void getDirections(){
        getDirections(active_marker_lat, active_marker_lon);
    }

    public void getDirections(Double lat, Double lon){
        getLocation();

        if (directions_route != null){
            directions_route.remove();
        }

        Double cur_lat = mostRecentLocation.getLatitude();
        Double cur_lon = mostRecentLocation.getLongitude();

        String url = String.format(Constants.Directions_API, cur_lat, cur_lon, lat, lon);

        Ion.with(BUPApplication.context, url)
                .asString()
                .setCallback(
                        new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                try{
                                    createDirectionsFromJson(result);
                                }
                                catch (JSONException ex){
                                    Log.e(LOG_TAG, ex.toString());
                                }

                            }
                        }
                );
    }

    public class Nearby_get extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String...type) {
            return get_closest(type[0]);
        }

        @Override
        protected void onPostExecute(JSONObject point) {
            try{
                Double lat = point.getDouble("latitude");
                Double lon = point.getDouble("longitude");
                String title = point.getString("title");

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 15));

                TextView title_label = (TextView) findViewById(R.id.title_label);
                title_label.setText(title);
                getDirections(lat, lon);
            }
            catch (JSONException ex){
                Log.e(LOG_TAG,ex.toString());
            }

        }
    }

    public JSONObject get_closest(String type){
        getLocation();
        Double lon = mostRecentLocation.getLongitude();
        Double lat = mostRecentLocation.getLatitude();

        String lons = String.valueOf(lon);
        String lats = String.valueOf(lat);

        String json_url = String.format(Constants.nearby_query_misc,lons,lats,type);
        if (type.equals("bisiklet_tamircileri")){
            json_url = String.format(Constants.nearby_query,lons,lats,type);
        }

        JSONObject point = new JSONObject();
        try{
            final JSONObject jsonObj = Utils.LoadJsonFromURL(json_url);
            JSONArray rows = jsonObj.getJSONArray("rows");
            point = (JSONObject) rows.get(0);
        }
        catch (JSONException ex){
            Log.e(LOG_TAG, ex.toString());
        }
        return point;

    }

    public void toggle_pt_markers(){
        MenuItem toggler = top_menu.findItem(R.id.toggle_pt);
        Iterator it = bus_polylines.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Polyline line = (Polyline) pairs.getValue();
            if (line.isVisible()){
                line.setVisible(false);
                toggler.setIcon(R.drawable.btn_check_off);
            }
            else{
                line.setVisible(true);
                toggler.setIcon(R.drawable.btn_check_on);
            }
        }
        it = pt_markers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Marker marker = (Marker) pairs.getValue();
            if (marker.isVisible()){
                marker.setVisible(false);
            }
            else{
                marker.setVisible(true);
            }
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

    protected void getJson(String json_url,final int type) throws IOException, JSONException {
        final JSONObject jsonobj = Utils.LoadJsonFromURL(json_url);
        jsonobj.put("type", type);

        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (type == Constants.TRANSPORT_ROADS || type == Constants.LEISURE_ROADS || type == Constants.IETT_ROADS){
                        createRoadsFromJson(jsonobj.toString());
                    }
                    else{
                        createMarkersFromJson(jsonobj.toString());
                    }
                } catch (JSONException e) {

                    Log.e(LOG_TAG, "Error processing JSON: " + e.toString());
                }
            }
        });
    }

    JSONObject generateMarkerInfo(int type, JSONObject properties) throws JSONException{
        JSONObject marker_info = new JSONObject();
        marker_info.put("title", properties.getString("name"));
        marker_info.put("type", type);

        if (type == Constants.REPAIRSHOP){
            String address = properties.getString("adres");
            address = address.replaceAll("\\s+", " ");
            String web = properties.getString("web");
            String mobile = properties.getString("telefon");

            marker_info.put("address", address);
            marker_info.put("web", web);
            marker_info.put("mobile", mobile);
        }
        else if (type == Constants.BDI){
            String address = properties.getString("adres");
            address = address.replaceAll("\\s+", " ");
            String web = properties.getString("web");
            String mobile = properties.getString("telefon");
            String advantage = properties.getString("kampanya");

            marker_info.put("address", address);
            marker_info.put("web", web);
            marker_info.put("mobile", mobile);
            marker_info.put("campaign", advantage);
        }
        else if (type == Constants.DRAIN ){
            String topImage = properties.getString("resim");
            String description = properties.getString("description");
            description = description.trim().replaceAll(" +", " ");

            marker_info.put("description", description);
            marker_info.put("image", topImage);
            marker_info.put("updated", false);
        }

        else if (type == Constants.RENT || type == Constants.PARK || type == Constants.FERRY || type == Constants.PUBLIC_TRANSPORT){
            String description = properties.getString("description");
            description = description.trim().replaceAll(" +", " ");

            marker_info.put("description", description);
        }

        return marker_info;
    }



    void createDirectionsFromJson(String json) throws JSONException{
        JSONObject jsonObject = new JSONObject(json);
        JSONObject routes = jsonObject.getJSONArray("routes").getJSONObject(0);
        JSONObject bounds = routes.getJSONObject("bounds");
        JSONObject route = routes.getJSONArray("legs").getJSONObject(0);

        Double distance = (double) route.getJSONObject("distance").getInt("value");

        Double start_lat = route.getJSONObject("start_location").getDouble("lat");
        Double start_lon = route.getJSONObject("start_location").getDouble("lng");

        JSONArray steps = route.getJSONArray("steps");
        PolylineOptions line = new PolylineOptions().width(Utils.dpToPx(8)).color(Color.argb(150,0,0,255)).geodesic(true);
        line.add(new LatLng(start_lat,start_lon));

        DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();

        for (int i=0; i < steps.length(); i++){
            JSONObject step = steps.getJSONObject(i);
            String polyline = step.getJSONObject("polyline").getString("points");
            List<LatLng> list = directionsJSONParser.decodePoly(polyline);

            for (int k=0; k<list.size(); k++ ){
                LatLng point =  list.get(k);
                line.add(point);
            }

        }
        directions_route = map.addPolyline(line);
        LatLng sw = new LatLng(bounds.getJSONObject("southwest").getDouble("lat"), bounds.getJSONObject("southwest").getDouble("lng"));
        LatLng ne = new LatLng(bounds.getJSONObject("northeast").getDouble("lat"), bounds.getJSONObject("northeast").getDouble("lng"));

        LatLngBounds bound = new LatLngBounds(sw,ne);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, Utils.dpToPx(75)));

        TextView distance_label = (TextView) findViewById(R.id.distance);
        RelativeLayout distance_layout = (RelativeLayout) findViewById(R.id.distance_layout);
        distance_label.setText(distance/1000 + " km");
        map.setPadding(0, Utils.dpToPx(100), 0, 0);
        distance_layout.setVisibility(View.VISIBLE);

    }
    
    void createRoadsFromJson(String json) throws  JSONException{
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("features");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);

            JSONObject propertiesObj = jsonObj.getJSONObject("properties");
            JSONObject geometryObj = jsonObj.getJSONObject("geometry");
            JSONArray coordinates = geometryObj.getJSONArray("coordinates");

            JSONObject route_info = new JSONObject();
            route_info.put("title", propertiesObj.getString("name"));
            route_info.put("description", propertiesObj.getString("description"));
            String category = propertiesObj.getString("kategori");

            int color = 0;
            if (category.equals("ulasim")){
                color = Color.parseColor("#3E7BB6");
            }
            else if (category.equals("gezi")){
                color = Color.parseColor("#FF6600");
            }
            else if (category.equals("iett")){
                color = Color.parseColor("#A53ED5");
            }

            PolylineOptions line = new PolylineOptions().width(8).color(color).geodesic(true);
            coordinates = (JSONArray) coordinates.get(0);

            for (int k=0; k < coordinates.length(); k++){
                JSONArray ko = (JSONArray) coordinates.get(k);
                Double lat = ko.getDouble(1);
                Double lon = ko.getDouble(0);

                line.add(new LatLng(lat, lon));
            }

            Polyline polyline = map.addPolyline(line);
            if (category.equals("iett")){
                polyline.setVisible(false);
                bus_polylines.put(polyline.getId(), polyline);
            }

            top_menu.findItem(R.id.toggle_pt).setEnabled(true);
        }

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

            boolean visible = true;
            if (marker_type == Constants.PUBLIC_TRANSPORT || marker_type == Constants.FERRY){
                visible = false;
            }

            Marker marker = map.addMarker(
                    new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(Constants.MARKERS[marker_type]))
                            .position(new LatLng(lat, lon))
                            .visible(visible)
                            .snippet(marker_info.toString())
            );
            if (marker_type == Constants.PUBLIC_TRANSPORT || marker_type == Constants.FERRY){
                pt_markers.put(marker.getId(),marker);
            }
            marker_info.put("id", marker.getId());
            marker_info.put("lat",lat);
            marker_info.put("lon",lon);
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
                View v = new View(BUPApplication.context);

                try{
                    final JSONObject marker_info = new JSONObject(marker.getSnippet());

                    active_marker_lat = marker_info.getDouble("lat");
                    active_marker_lon = marker_info.getDouble("lon");

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
                                infoIntent.putExtra("lat", active_marker_lat);
                                infoIntent.putExtra("lon", active_marker_lon);
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

    public void closeRouteClick(View view){
        RelativeLayout distance_layout = (RelativeLayout) findViewById(R.id.distance_layout);
        distance_layout.setVisibility(View.GONE);
        directions_route.remove();
        map.setPadding(0, Utils.dpToPx(50), 0, 0);
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = locationManager.getBestProvider(criteria,true);
        if (locationManager != null){
            locationManager.requestLocationUpdates(provider, 5000, 5, this);
            mostRecentLocation = locationManager.getLastKnownLocation(provider);
        }
        else{
            mostRecentLocation = null;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                openInfo();
                return true;
            case R.id.toggle_pt:
                toggle_pt_markers();
                return true;
            case R.id.nearby_repairshop:
                new Nearby_get().execute("bisiklet_tamircileri");
                return true;
            case R.id.nearby_rent:
                new Nearby_get().execute("kiralama");
                return true;
            case R.id.nearby_park:
                new Nearby_get().execute("park");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        top_menu = menu;
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onLocationChanged(Location location) {
        mostRecentLocation = location;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLocation();
    }

    @Override
    public void onPause() {
        super.onResume();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
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