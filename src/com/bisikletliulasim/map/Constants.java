package com.bisikletliulasim.map;

public final class Constants{
    public static final int REPAIRSHOP = 0;
    public static final int BDI = 1;
    public static final int RENT = 2;
    public static final int PARK = 3;
    public static final int DRAIN = 4;
    public static final int FERRY = 5;
    public static final int PUBLIC_TRANSPORT = 6;
    public static final int TRANSPORT_ROADS = 7;
    public static final int LEISURE_ROADS = 8;
    public static final int IETT_ROADS = 9;


    public static final String REPAIRSHOP_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20bisiklet_tamircileri&format=GEOJson";
    public static final String BDI_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20bisiklet_dostu_isletmeler&format=GEOJson";
    public static final String RENT_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27kiralama%27)&format=GEOJson";
    public static final String PARK_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27park%27)&format=GEOJson";
    public static final String DRAIN_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27mazgal%27)&format=GEOJson";
    public static final String FERRY_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27iskele%27)&format=GEOJson";
    public static final String PUBLIC_TRANSPORT_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20misc%20WHERE%20kategori%20%20IN%20(%27toplutasima%27)&format=GEOJson";
    public static final String TRANSPORT_ROADS_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20mevcut_bisiklet_yollari%20WHERE%20kategori%20%20IN%20(%27ulasim%27)&format=GEOJson";
    public static final String LEISURE_ROADS_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20mevcut_bisiklet_yollari%20WHERE%20kategori%20%20IN%20(%27gezi%27)&format=GEOJson";
    public static final String IETT_ROADS_JSON = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%20*%20FROM%20mevcut_bisiklet_yollari%20WHERE%20kategori%20%20IN%20(%27iett%27)&format=GEOJson";

    public static final int[] MARKER_TYPES = {
            REPAIRSHOP,
            BDI,
            RENT,
            PARK,
            DRAIN,
            FERRY,
            PUBLIC_TRANSPORT,
    };
    public static final int[] MARKERS={
            R.drawable.repair,
            R.drawable.bdi,
            R.drawable.rent,
            R.drawable.park,
            R.drawable.skull,
            R.drawable.harbor,
            R.drawable.toplutasima,
    };
    public static String[] REPAIRSHOP_INFO={
            "title",
            "address",
            "web",
            "mobile",
    };
    public static String[] BDI_INFO={
            "title",
            "address",
            "web",
            "mobile",
            "campaign",
    };
    public static String[] DRAIN_INFO={
            "title",
            "description",
            "image",
            "mobile",
    };
    public static String[] RENT_INFO={
            "title",
            "description",
            "mobile",
            "image",
    };
    public static String[] PARK_INFO={
            "title",
            "description",
            "image",
            "mobile",
    };
    public static String[] FERRY_INFO={
            "title",
            "description",
            "mobile",
    };
    public static String[] PUBLIC_TRANSPORT_INFO={
            "title",
            "description",
            "mobile",
    };


}




