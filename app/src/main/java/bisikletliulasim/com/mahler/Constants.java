package bisikletliulasim.com.mahler;

import java.util.Map;

/**
 * Created by yigit on 04/07/14.
 */
public class Constants {

    public static final int SLIDING_PANEL_HEIGHT = 100; //dp

    public static final String LOG_TAG = "BUP_HARITA";

    public static final int INFO_IMAGE_DP_HEIGHT = 200;
    public static final int INFO_IMAGE_DP_WIDTH = 360;
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

    public static final String nearby_query = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%%20name%%20as%%20title,ST_X(ST_Centroid(the_geom))%%20as%%20longitude,ST_Y(ST_Centroid(the_geom))%%20as%%20latitude,%%20ST_Distance(the_geom::geography,%%20ST_PointFromText(%%27POINT(%s%%20%s)%%27,%%204326)::geography)%%20AS%%20distance%%20FROM%%20%s%%20ORDER%%20BY%%20distance%%20ASC%%20LIMIT%%201";
    public static final String nearby_query_misc = "http://bisikletliulasim.cartodb.com/api/v2/sql?q=SELECT%%20name%%20as%%20title,ST_X(ST_Centroid(the_geom))%%20as%%20longitude,ST_Y(ST_Centroid(the_geom))%%20as%%20latitude,%%20ST_Distance(the_geom::geography,%%20ST_PointFromText(%%27POINT(%s%%20%s)%%27,%%204326)::geography)%%20AS%%20distance%%20FROM%%20misc%%20WHERE%%20kategori%%20%%20IN%%20(%%27%s%%27)%%20ORDER%%20BY%%20distance%%20ASC%%20LIMIT%%201";

    public static final String Directions_API = "https://maps.googleapis.com/maps/api/directions/json?origin=%s,%s&destination=%s,%s&sensor=true&key=AIzaSyB1d5eN43PKkoeeRT8-esCS3_qa47qKcy4";


    public static final int[] MARKER_TYPES = {
            REPAIRSHOP,
            BDI,
            RENT,
            PARK,
            DRAIN,
            FERRY,
            PUBLIC_TRANSPORT,
    };

    public static final String[] TYPE_URLS = {
            REPAIRSHOP_JSON,
            BDI_JSON,
            RENT_JSON,
            PARK_JSON,
            DRAIN_JSON,
            FERRY_JSON,
            PUBLIC_TRANSPORT_JSON,
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

}
