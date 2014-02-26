package com.bisikletliulasim.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by yigit on 2/26/14.
 */
public class InfoWindowActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.marker_info);

        Intent thisIntent = getIntent(); // gets the previously created intent
        String title = thisIntent.getStringExtra("title");
        TextView title_view = (TextView) findViewById(R.id.title);
        title_view.setText(title);

    }

}
