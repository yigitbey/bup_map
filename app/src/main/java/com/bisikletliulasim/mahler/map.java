package com.bisikletliulasim.mahler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class map extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public SlidingUpPanelLayout sliding_layout = null;
    public View map_fragment = null;
    private static final String LOG_TAG = "BUPHarita";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public GoogleApiClient googleApiClient = null;
    public LocationManager mLocationManager = null;
    public static ArrayList<Bitmap> marker_bitmaps;
    Polyline directions_route = null;
    Location mostRecentLocation;
    TextView markerInfo;
    ImageButton phoneButton;
    ImageButton directionsButton;
    ImageButton shareButton;
    ImageButton captionButton;
    LatLng active_location = null;
    String active_phone = null;
    String active_info = null;
    Boolean directions_mode = false;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Boolean is_online = isOnline();
        if (is_online == false){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_internet_message)
                    .setTitle(R.string.no_internet_title);
            AlertDialog dialog = builder.create();
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            dialog.show();

        }

        int gms_enabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (gms_enabled != ConnectionResult.SUCCESS){
            Log.e(LOG_TAG,"GMS not available");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_gms_message)
                    .setTitle(R.string.no_gms_title);
            AlertDialog dialog = builder.create();
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            dialog.show();


        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        setContentView(R.layout.activity_map);

        phoneButton = (ImageButton) findViewById(R.id.phoneButton);
        directionsButton = (ImageButton) findViewById(R.id.directionsButton);
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        captionButton = (ImageButton) findViewById(R.id.captionButton);
        captionButton.setEnabled(false);
        sliding_layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        int px = (int) Utils.dpToPx(Constants.SLIDING_PANEL_HEIGHT, getApplicationContext());
        sliding_layout.setMinimumHeight(px);
        sliding_layout.setPanelHeight(px);
        sliding_layout.hidePanel();

        marker_bitmaps = new ArrayList<Bitmap>();
        map_fragment = findViewById(R.id.map);

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + Uri.encode(active_phone.trim())));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sliding_layout.hidePanel();
                sliding_layout.setEnabled(false);
                directions_mode = true;
                getDirections(active_location.latitude, active_location.longitude, true);
                captionButton.setEnabled(true);
                captionButton.setImageDrawable(getResources().getDrawable(R.drawable.closecircle));
                captionButton.setColorFilter(Color.BLACK);
            }
        });
        captionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDirectionsMode();
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, active_info);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBarIndeterminate progressBar = (ProgressBarIndeterminate) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.GONE);
            }
        }, 250);


    }

    private void exitDirectionsMode(){
        if (directions_route != null){
            directions_route.remove();
        }
        sliding_layout.hidePanel();
        sliding_layout.setEnabled(true);

        captionButton.setEnabled(false);
        captionButton.setImageDrawable(getResources().getDrawable(R.drawable.information));
        captionButton.clearColorFilter();

    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        if (mostRecentLocation != null) {
            Double lat = mostRecentLocation.getLatitude();
            Double lon = mostRecentLocation.getLongitude();
            LatLng currentLocation = new LatLng(lat, lon);

            mMap.setPadding(0, (int) Utils.dpToPx(50, getApplicationContext()), 0, 0);
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        }
        else{
            LatLng currentLocation = new LatLng(40, 30);

            mMap.setPadding(0, (int) Utils.dpToPx(50, getApplicationContext()), 0, 0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 6));
        }

        mMap.getUiSettings().setZoomControlsEnabled(false);

        for (int i = 0; i < Constants.MARKERS.length; i++) {
            int width = (int) Utils.dpToPx((float) 28.78, getApplicationContext());
            int height = (int) Utils.dpToPx(35, getApplicationContext());

            Bitmap bm = BitmapFactory.decodeResource(getResources(), Constants.MARKERS[i]);
            Bitmap b = Bitmap.createScaledBitmap(bm, width, height, false);
            marker_bitmaps.add(b);
        }

        for (int i = 0; i < Constants.MARKER_TYPES.length; i++) {
            getMarkers(Constants.TYPE_URLS[i], Constants.MARKER_TYPES[i]);
        }

        for (int i = 0; i < Constants.ROAD_TYPES.length; i++) {
            getRoad(Constants.ROAD_URLS[i], Constants.MARKER_TYPES[i]);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!directions_mode) {
                    sliding_layout.hidePanel();
                }
            }
        });
    }

    private void getMarkers(String jsonUrl, final int type){
        Ion.with(getApplicationContext())
                .load(jsonUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonArray result_array = result.get("features").getAsJsonArray();
                        for(int i = 0; i < result_array.size(); i++) {
                            JsonObject result_obj = result_array.get(i).getAsJsonObject();
                            try {
                                JsonObject properties = result_obj.get("properties").getAsJsonObject();
                                JsonObject geometry = result_obj.get("geometry").getAsJsonObject();
                                create_marker(properties, geometry, type);
                            }
                            catch (Exception ex){
                                Log.e(Constants.LOG_TAG, ex.toString());
                                Log.e("G", result_obj.toString());
                            }
                        }
                    }
                });
    }

    private void getRoad(String jsonUrl, final int type){
        Ion.with(getApplicationContext())
                .load(jsonUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonArray result_array = result.get("features").getAsJsonArray();
                        for(int i = 0; i < result_array.size(); i++) {
                            JsonObject result_obj = result_array.get(i).getAsJsonObject();
                            try {
                                JsonObject properties = result_obj.get("properties").getAsJsonObject();
                                JsonObject geometry = result_obj.get("geometry").getAsJsonObject();
                                create_road(properties, geometry, type);

                            }
                            catch (Exception ex){
                                ex.printStackTrace();
                                //Log.e(Constants.LOG_TAG, ex.getStackTrace());
                                Log.e("G", result_obj.toString());
                            }
                        }
                    }
                });
    }

    private void create_road(JsonObject properties, JsonObject geometry, final int road_type){
        boolean visible = true;
        if (road_type == Constants.IETT_ROADS){
            visible = false;
        }
        try {
            String title = properties.get("name").getAsString();
            String description = properties.get("description").getAsString();
        }
        catch (UnsupportedOperationException ex){
            String title = "";
            String description = "";
        }

        String category = properties.get("kategori").getAsString();

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

        JsonArray line_coordinates = geometry.get("coordinates").getAsJsonArray();

        PolylineOptions line = new PolylineOptions().width(8).color(color).geodesic(true);

        line_coordinates = line_coordinates.get(0).getAsJsonArray();

        for (int k=0; k<line_coordinates.size(); k++){
            JsonArray ko = line_coordinates.get(k).getAsJsonArray();
            try {
                Double lat = ko.get(1).getAsDouble();
                Double lon = ko.get(0).getAsDouble();
                line.add(new LatLng(lat, lon));
            }
            catch (Exception ex) {
                Log.e("CD", ex.toString());
                Log.e("CDE", ko.toString());
            }

        }

        Polyline polyline = mMap.addPolyline(line);
        polyline.setVisible(visible);


    }

    private void create_marker(JsonObject properties, JsonObject geometry, final int marker_type){

        Double lat = geometry.get("coordinates").getAsJsonArray().get(1).getAsDouble();
        Double lon = geometry.get("coordinates").getAsJsonArray().get(0).getAsDouble();

        boolean visible = true;
        if (marker_type == Constants.PUBLIC_TRANSPORT || marker_type == Constants.FERRY){
            visible = false;
        }

        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (directions_mode){
                    exitDirectionsMode();
                }
                active_location = marker.getPosition();
                active_info = "";
                sliding_layout.hidePanel();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sliding_layout.clearAnimation();
                        sliding_layout.showPanel();
                    }
                }, 300);

                JsonParser parser = new JsonParser();
                final JsonObject properties = (JsonObject) parser.parse(marker.getSnippet());

                String title = properties.get("name").getAsString();
                TextView title_text = (TextView) findViewById(R.id.captionView);
                title_text.setText(title);
                active_info = title + " ";

                getDirections(marker.getPosition().latitude, marker.getPosition().longitude, false);

                CardView card = (CardView) findViewById(R.id.markerDescriptionCard);
                if (properties.get("description") != null && properties.get("description").isJsonPrimitive()){
                    if (!properties.get("description").getAsString().equalsIgnoreCase("")) {
                        View infobox = findViewById(R.id.description);
                        ImageView image = (ImageView) infobox.findViewById(R.id.label);
                        image.setImageResource(R.drawable.information);
                        TextView label = (TextView) infobox.findViewById(R.id.info);
                        label.setText(properties.get("description").getAsString());
                        label.invalidate();
                        card.setVisibility(View.VISIBLE);

                    }
                }else{
                    card.setVisibility(View.GONE);
                }
                card = (CardView) findViewById(R.id.markerAddressCard);
                if (properties.get("adres") != null &&
                    properties.get("adres").isJsonPrimitive() &&
                    !properties.get("adres").getAsString().equalsIgnoreCase("")
                        ){
                    View infobox =  findViewById(R.id.address);
                    ImageView image = (ImageView) infobox.findViewById(R.id.label);
                    image.setImageResource(R.drawable.mapmarker);
                    TextView label = (TextView) infobox.findViewById(R.id.info);
                    label.setText(properties.get("adres").getAsString());
                    label.invalidate();
                    card.setVisibility(View.VISIBLE);
                    active_info = active_info + properties.get("adres").getAsString() + " ";
                }else{
                    card.setVisibility(View.GONE);
                }
                card = (CardView) findViewById(R.id.markerPromotionCard);

                if (properties.get("kampanya") != null &&
                        properties.get("kampanya").isJsonPrimitive()&&
                        !properties.get("kampanya").getAsString().equalsIgnoreCase("")){
                    View infobox =  findViewById(R.id.promotion);
                    ImageView image = (ImageView) infobox.findViewById(R.id.label);
                    image.setImageResource(R.drawable.ticket);
                    TextView label = (TextView) infobox.findViewById(R.id.info);
                    label.setText(properties.get("kampanya").getAsString());
                    label.invalidate();
                    card.setVisibility(View.VISIBLE);
                }else{
                    card.setVisibility(View.GONE);
                }

                Boolean viewInfo = false;
                TextView labeltext = (TextView) findViewById(R.id.labelWeb);
                markerInfo = (TextView) findViewById(R.id.markerWeb);
                if (properties.get("web") != null && properties.get("web").isJsonPrimitive()){
                    markerInfo.setText(properties.get("web").getAsString());
                    markerInfo.setVisibility(View.VISIBLE);
                    labeltext.setVisibility(View.VISIBLE);
                    viewInfo = true;
                    active_info = active_info + properties.get("web").getAsString() + " ";
                }else{
                    labeltext.setVisibility(View.GONE);
                    markerInfo.setVisibility(View.GONE);
                }

                labeltext = (TextView) findViewById(R.id.labelTelephone);
                markerInfo = (TextView) findViewById(R.id.markerTelephone);
                if (properties.get("telefon") != null && properties.get("telefon").isJsonPrimitive()) {
                    markerInfo.setText(properties.get("telefon").getAsString());
                    markerInfo.setVisibility(View.VISIBLE);
                    labeltext.setVisibility(View.VISIBLE);
                    viewInfo = true;
                    phoneButton.setVisibility(View.VISIBLE);
                    active_phone = properties.get("telefon").getAsString();
                    active_info = active_info + properties.get("telefon").getAsString() + " ";
                }else{
                    labeltext.setVisibility(View.GONE);
                    markerInfo.setVisibility(View.GONE);
                    phoneButton.setVisibility(View.GONE);
                    active_phone = null;
                }

                card = (CardView) findViewById(R.id.markerInfoCard);
                if (viewInfo){

                    card.setVisibility(View.VISIBLE);
                } else {
                    card.setVisibility(View.GONE);
                }

                card = (CardView) findViewById(R.id.markerImageCard);
                if (properties.get("resim") != null && properties.get("resim").isJsonPrimitive()) {
                    ImageView markerImage = (ImageView) findViewById(R.id.markerImage);
                    Ion.with(markerImage)
                            .placeholder(R.drawable.no_image)
                            .error(R.drawable.no_image)
                            .resize(960, 600)
                            .centerCrop()
                            .load(properties.get("resim").getAsString());
                    markerImage.setVisibility(View.VISIBLE);
                    card.setVisibility(View.VISIBLE);
                }else{
                    card.setVisibility(View.GONE);
                }
                return true;
            }
        });



        mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(marker_bitmaps.get(marker_type)))
                        .position(new LatLng(lat, lon))
                        .visible(visible)
                        .snippet(properties.toString())
        );

    }


    public void getDirections(Double lat, Double lon, final Boolean show_route){
        if (directions_route != null){
            directions_route.remove();
        }

        Double cur_lat = mostRecentLocation.getLatitude();
        Double cur_lon = mostRecentLocation.getLongitude();

        String url = String.format(Constants.Directions_API, cur_lat, cur_lon, lat, lon);

        Ion.with(getApplicationContext())
                .load(url)
                .asString()
                .setCallback(
                        new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                try{
                                    createDirectionsFromJson(result, show_route);

                                }
                                catch (JSONException ex){
                                    Log.e(LOG_TAG, ex.toString());
                                }

                            }
                        }
                );
    }


    void createDirectionsFromJson(String json, Boolean show_route) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject routes = jsonObject.getJSONArray("routes").getJSONObject(0);
        JSONObject bounds = routes.getJSONObject("bounds");
        JSONObject route = routes.getJSONArray("legs").getJSONObject(0);

        Double distance = (double) route.getJSONObject("distance").getInt("value");
        if (show_route) {
            Double start_lat = route.getJSONObject("start_location").getDouble("lat");
            Double start_lon = route.getJSONObject("start_location").getDouble("lng");

            JSONArray steps = route.getJSONArray("steps");
            PolylineOptions line = new PolylineOptions().width(Utils.dpToPx(8, getApplicationContext())).color(Color.argb(150, 0, 0, 255)).geodesic(true);
            line.add(new LatLng(start_lat, start_lon));

            DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();

            for (int i = 0; i < steps.length(); i++) {
                JSONObject step = steps.getJSONObject(i);
                String polyline = step.getJSONObject("polyline").getString("points");
                List<LatLng> list = directionsJSONParser.decodePoly(polyline);

                for (int k = 0; k < list.size(); k++) {
                    LatLng point = list.get(k);
                    line.add(point);
                }

            }
            directions_route = mMap.addPolyline(line);
            LatLng sw = new LatLng(bounds.getJSONObject("southwest").getDouble("lat"), bounds.getJSONObject("southwest").getDouble("lng"));
            LatLng ne = new LatLng(bounds.getJSONObject("northeast").getDouble("lat"), bounds.getJSONObject("northeast").getDouble("lng"));

            LatLngBounds bound = new LatLngBounds(sw, ne);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, (int) Utils.dpToPx(75, getApplicationContext())));
        }
        TextView distance_label = (TextView) findViewById(R.id.distanceView);
        distance_label.setText(String.format("%.2f km", distance/1000));
        distance_label.setVisibility(View.VISIBLE);

    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        if (!isOnline()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_internet_message)
                    .setTitle(R.string.no_internet_title);
            AlertDialog dialog = builder.create();

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            dialog.show();

        }
        else{
            createLocationRequest();
            mostRecentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            setUpMapIfNeeded();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.d("BUP", "" + connectionResult.getErrorCode());
            //showErrorDialog(connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mostRecentLocation = location;
        mMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onResume() {
        if (!isOnline()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_internet_message)
                    .setTitle(R.string.no_internet_title);
            AlertDialog dialog = builder.create();

            dialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            dialog.show();

        }
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onStart() {
        if (!isOnline()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_internet_message)
                    .setTitle(R.string.no_internet_title);
            AlertDialog dialog = builder.create();

            dialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            dialog.show();

        }
        super.onStart();
        googleApiClient.connect();
    }
    @Override
    protected void onStop() {
        if (isOnline()){
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
        }

        super.onStop();
    }
    @Override
    protected void onPause() {
        if (isOnline()){
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }

        super.onPause();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
