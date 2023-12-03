package ch.so.agi.isochrone;

// https://github.com/ValentinMinder/Swisstopo-WGS84-LV03/blob/master/scripts/java/WGS84_CH1903.java
public class ApproxSwissProj {

    /**
     * Convert WGS84 to LV95 Return an array of double that contain east,
     * north, and height
     * 
     * @param latitude
     * @param longitude
     * @param ellHeight
     * @return
     */
    public static double[] WGS84toLV95(double latitude, double longitude, double ellHeight) {
        // , ref double east, ref double north, ref double height
        double d[] = new double[3];

        d[0] = WGStoCHy(latitude, longitude);
        d[1] = WGStoCHx(latitude, longitude);
        d[2] = WGStoCHh(latitude, longitude, ellHeight);
        return d;
    }

    // Convert WGS lat/long (° dec) and height to CH h
    private static double WGStoCHh(double lat, double lng, double h) {
        // Converts dec degrees to sex seconds
        lat = DecToSexAngle(lat);
        lng = DecToSexAngle(lng);

        // Auxiliary values (% Bern)
        double lat_aux = (lat - 169028.66) / 10000;
        double lng_aux = (lng - 26782.5) / 10000;

        // Process h
        h = (h - 49.55) + (2.73 * lng_aux) + (6.94 * lat_aux);

        return h;
    }

    // Convert WGS lat/long (° dec) to CH x
    private static double WGStoCHx(double lat, double lng) {
        // Converts dec degrees to sex seconds
        lat = DecToSexAngle(lat);
        lng = DecToSexAngle(lng);

        // Auxiliary values (% Bern)
        double lat_aux = (lat - 169028.66) / 10000;
        double lng_aux = (lng - 26782.5) / 10000;

        // Process X
        double x = ((1200147.07 + (308807.95 * lat_aux)
                + (3745.25 * Math.pow(lng_aux, 2)) + (76.63 * Math.pow(lat_aux,
                2))) - (194.56 * Math.pow(lng_aux, 2) * lat_aux))
                + (119.79 * Math.pow(lat_aux, 3));

        return x;
    }

    // Convert WGS lat/long (° dec) to CH y
    private static double WGStoCHy(double lat, double lng) {
        // Converts dec degrees to sex seconds
        lat = DecToSexAngle(lat);
        lng = DecToSexAngle(lng);

        // Auxiliary values (% Bern)
        double lat_aux = (lat - 169028.66) / 10000;
        double lng_aux = (lng - 26782.5) / 10000;

        // Process Y
        double y = (2600072.37 + (211455.93 * lng_aux))
                - (10938.51 * lng_aux * lat_aux)
                - (0.36 * lng_aux * Math.pow(lat_aux, 2))
                - (44.54 * Math.pow(lng_aux, 3));

        return y;
    }
    
    // Convert decimal angle (degrees) to sexagesimal angle (seconds)
    public static double DecToSexAngle(double dec) {
        int deg = (int) Math.floor(dec);
        int min = (int) Math.floor((dec - deg) * 60);
        double sec = (((dec - deg) * 60) - min) * 60;

        return sec + min*60.0 + deg*3600.0;
    }

    private ApproxSwissProj() {
        // Only static
    }
}
