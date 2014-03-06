package com.bisikletliulasim.map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class InfoWindowActivity extends Activity {
    private static final String LOG_TAG = "BUPHarita_info";

    public void onShareClick(View view){
        String title = (String) ((TextView) findViewById(R.id.title)).getText();

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String share_msg = "Bisikletli Ulasim Haritasinda: " + title + " http://bisikletliulasim.com/android";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, share_msg);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.info_window);
        Intent thisIntent = getIntent();

        int type = thisIntent.getIntExtra("type", 0);
        HashMap<String, String> marker_info = new HashMap<String,String>();

        TextView title_view = (TextView) findViewById(R.id.title);
        TextView address_view = (TextView) findViewById(R.id.address);
        TextView description_view = (TextView) findViewById(R.id.description);
        TextView campaign_view = (TextView) findViewById(R.id.campaign);
        TextView web_view = (TextView) findViewById(R.id.web);
        TextView mobile_view = (TextView) findViewById(R.id.mobile);
        ImageView topImage = (ImageView) findViewById(R.id.topImage);
        ImageView info_icon = (ImageView) findViewById(R.id.info_icon);

        switch (type){
            case Constants.REPAIRSHOP:
                topImage.setImageResource(R.drawable.repair_noimage);

                info_icon.setImageResource(R.drawable.repair);
                for (String key: Constants.REPAIRSHOP_INFO){
                    marker_info.put(key, thisIntent.getStringExtra(key));
                }
                title_view.setText(marker_info.get("title"));
                address_view.setText(marker_info.get("address"));
                web_view.setText(marker_info.get("web"));
                mobile_view.setText(marker_info.get("mobile"));

                TextView[] repairshop_views = {address_view,web_view,mobile_view};
                for (TextView view: repairshop_views){
                    String text = view.getText().toString();
                    if (!text.equals("null") && !text.equals("")){
                        view.setVisibility(View.VISIBLE);
                    }
                }
                topImage.setVisibility(View.VISIBLE);
                break;

            case Constants.BDI:
                topImage.setImageResource(R.drawable.bdi_noimage);
                info_icon.setImageResource(R.drawable.bdi);
                for (String key: Constants.BDI_INFO){
                    marker_info.put(key, thisIntent.getStringExtra(key));
                }
                title_view.setText(marker_info.get("title"));
                address_view.setText(marker_info.get("address"));
                campaign_view.setText(marker_info.get("campaign"));
                web_view.setText(marker_info.get("web"));
                mobile_view.setText(marker_info.get("mobile"));

                TextView[] bdi_views = {campaign_view,address_view,web_view,mobile_view};
                for (TextView view: bdi_views){
                    String text = view.getText().toString();
                    if (!text.equals("null") && !text.equals("")){
                        view.setVisibility(View.VISIBLE);
                    }
                }
                topImage.setVisibility(View.VISIBLE);

                break;

            case Constants.RENT:
                info_icon.setImageResource(R.drawable.rent);
                title_view.setText(marker_info.get("title"));
                description_view.setText(marker_info.get("description"));
                String rent_image_url= marker_info.get("image");

                for (String key: Constants.RENT_INFO){
                    marker_info.put(key, thisIntent.getStringExtra(key));
                }
                Picasso.with(getApplicationContext()).load(rent_image_url).into(topImage);


                TextView[] rent_views = {description_view};
                for (TextView view: rent_views){
                    String text = view.getText().toString();
                    if (!text.equals("null") && !text.equals("")){
                        view.setVisibility(View.VISIBLE);
                    }
                }
                topImage.setVisibility(View.VISIBLE);
                break;

            case Constants.DRAIN:
                topImage.setImageResource(R.drawable.drain_noimage);
                info_icon.setImageResource(R.drawable.skull);
                for (String key: Constants.DRAIN_INFO){
                    marker_info.put(key, thisIntent.getStringExtra(key));
                }
                title_view.setText(marker_info.get("title"));
                description_view.setText(marker_info.get("description"));
                String drain_image_url = marker_info.get("image");

                Picasso.with(getApplicationContext())
                        .load(drain_image_url)
                        .placeholder(R.drawable.drain_noimage)
                        .resize(960,600)
                        .centerCrop()
                        .into(topImage);

                TextView[] drain_views = {description_view};
                for (TextView view: drain_views){
                    String text = view.getText().toString();
                    if (!text.equals("null") && !text.equals("")){
                        view.setVisibility(View.VISIBLE);
                    }
                }
                topImage.setVisibility(View.VISIBLE);
                break;

            case Constants.PARK:
                info_icon.setImageResource(R.drawable.park);
                for (String key: Constants.DRAIN_INFO){
                    marker_info.put(key, thisIntent.getStringExtra(key));
                }
                title_view.setText(marker_info.get("title"));
                description_view.setText(marker_info.get("description"));
                String park_image_url = marker_info.get("image");

                Picasso.with(getApplicationContext())
                        .load(park_image_url)
                        .placeholder(R.drawable.no_image)
                        .resize(960,600)
                        .centerCrop()
                        .into(topImage);

                TextView[] park_views = {description_view};
                for (TextView view: park_views){
                    String text = view.getText().toString();
                    if (!text.equals("null") && !text.equals("")){
                        view.setVisibility(View.VISIBLE);
                    }
                }
                topImage.setVisibility(View.VISIBLE);
                break;

            case Constants.FERRY:
                info_icon.setImageResource(R.drawable.harbor);
                for (String key: Constants.FERRY_INFO){
                    marker_info.put(key, thisIntent.getStringExtra(key));
                }
                title_view.setText(marker_info.get("title"));
                description_view.setText(marker_info.get("description"));

                TextView[] ferry_views = {title_view,description_view};
                for (TextView view: ferry_views){
                    String text = view.getText().toString();
                    if (!text.equals("null") && !text.equals("")){
                        view.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case Constants.PUBLIC_TRANSPORT:
                info_icon.setImageResource(R.drawable.toplutasima);
                for (String key: Constants.PUBLIC_TRANSPORT_INFO){
                    marker_info.put(key, thisIntent.getStringExtra(key));
                }
                title_view.setText(marker_info.get("title"));
                description_view.setText(marker_info.get("description"));

                TextView[] public_transport_views = {title_view,description_view};
                for (TextView view: public_transport_views){
                    String text = view.getText().toString();
                    if (!text.equals("null") && !text.equals("")){
                        view.setVisibility(View.VISIBLE);
                    }
                }
                break;

        }

    }

}
