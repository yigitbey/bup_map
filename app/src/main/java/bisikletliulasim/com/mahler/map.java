package bisikletliulasim.com.mahler;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;

import java.util.HashMap;

public class map extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        for (int i=0; i < Constants.MARKER_TYPES.length; i++) {
            getJson(Constants.TYPE_URLS[i], Constants.MARKER_TYPES[i]);
        }

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

        Marker marker = mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(Constants.MARKERS[marker_type]))
                        .position(new LatLng(lat, lon))
                        .visible(visible)
                        .snippet(properties.toString())
        );
    }

}
