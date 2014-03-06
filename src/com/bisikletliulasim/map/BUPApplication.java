package com.bisikletliulasim.map;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;

public class BUPApplication extends Application {

    public static Context context;
    @Override
    public void onCreate(){
        super.onCreate();
        Parse.initialize(this, "mKpWl67PWNFpurPu97OjAQ2LkPqnLa8xndOo9GUD", "QXBN48VLhpNufJQsrgHb0QqkwDkFIjid5VRq5s26");
        BUPApplication.context = getApplicationContext();
    }
}

