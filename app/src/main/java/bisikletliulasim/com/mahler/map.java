package bisikletliulasim.com.mahler;

import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class map extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public SlidingUpPanelLayout sliding_layout = null;
    public View map_fragment = null;
    private static final String LOG_TAG = "BUPHarita";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public LocationClient mLocationClient = null;
    public LocationManager mLocationManager = null;
    Location mostRecentLocation;
    TextView markerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int gms_enabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (gms_enabled != ConnectionResult.SUCCESS){
            Log.e(LOG_TAG,"GMS not available");
            return;
        }
        setContentView(R.layout.activity_map);

        sliding_layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        map_fragment = findViewById(R.id.map);

        getLocation();
        mLocationClient = new LocationClient(this, this, this);
        setUpMapIfNeeded();
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

        for (int i=0; i < Constants.MARKER_TYPES.length; i++) {
            getJson(Constants.TYPE_URLS[i], Constants.MARKER_TYPES[i]);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                sliding_layout.hidePanel();
            }
        });

    }

    private void getJson(String jsonUrl, final int type){
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
                JsonParser parser = new JsonParser();
                final JsonObject properties = (JsonObject) parser.parse(marker.getSnippet());

                int px = (int) Utils.dpToPx(Constants.SLIDING_PANEL_HEIGHT, getApplicationContext());
                sliding_layout.setMinimumHeight(px);
                sliding_layout.setPanelHeight(px);

                String title = properties.get("name").getAsString();
                TextView title_text = (TextView) findViewById(R.id.captionView);
                title_text.setText(title);

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
                }else{
                    card.setVisibility(View.GONE);
                }
                card = (CardView) findViewById(R.id.markerPromotionCard);
                Log.i("BUP", "" + properties.get("kampanya"));
                if (properties.get("kampanya") != null &&
                        properties.get("kampanya").isJsonPrimitive()&&
                        !properties.get("kampanya").getAsString().equalsIgnoreCase("")){
                    View infobox =  findViewById(R.id.promotion);
                    ImageView image = (ImageView) infobox.findViewById(R.id.label);

                    image.setImageResource(R.drawable.ticket);
                    TextView label = (TextView) infobox.findViewById(R.id.info);
                    Log.i("BUP", "" + label.getText());
                    label.setText(properties.get("kampanya").getAsString());
                    label.invalidate();
                    Log.i("BUP", "" + label.getText());
                    card.setVisibility(View.VISIBLE);
                }else{
                    card.setVisibility(View.GONE);
                }



                TextView labeltext = (TextView) findViewById(R.id.labelWeb);
                markerInfo = (TextView) findViewById(R.id.markerWeb);
                if (properties.get("web") != null && properties.get("web").isJsonPrimitive()){
                    markerInfo.setText(properties.get("web").getAsString());
                    markerInfo.setVisibility(View.VISIBLE);
                    labeltext.setVisibility(View.VISIBLE);
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
                }else{
                    labeltext.setVisibility(View.GONE);
                    markerInfo.setVisibility(View.GONE);
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
                sliding_layout.showPanel();
                return true;
            }
        });

        mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(Constants.MARKERS[marker_type]))
                        .position(new LatLng(lat, lon))
                        .visible(visible)
                        .snippet(properties.toString())
        );

    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        mostRecentLocation = mLocationClient.getLastLocation();

    }
    @Override
    public void onDisconnected() {
        // Display the connection status
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

    @Override
    protected void onResume() {
        super.onResume();
        mLocationClient.connect();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
        setUpMapIfNeeded();
    }
    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }
    @Override
    protected void onPause() {
        mLocationClient.disconnect();
        super.onPause();
    }

    private void getLocation() {
        mLocationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = mLocationManager.getBestProvider(criteria,true);

        mLocationManager.requestLocationUpdates(provider, 5000, 5, this);
        mostRecentLocation = mLocationManager.getLastKnownLocation(provider);
    }


}
