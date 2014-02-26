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




