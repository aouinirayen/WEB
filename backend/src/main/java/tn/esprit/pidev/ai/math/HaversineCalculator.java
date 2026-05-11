package tn.esprit.pidev.ai.math;

import java.util.Map;

public class HaversineCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    // Villes tunisiennes avec coordonnees GPS
    private static final Map<String, double[]> TUNISIAN_CITIES;
    static {
        TUNISIAN_CITIES = new java.util.HashMap<>();
        // === Grand Tunis ===
        TUNISIAN_CITIES.put("tunis", new double[]{36.8065, 10.1815});
        TUNISIAN_CITIES.put("ariana", new double[]{36.8625, 10.1956});
        TUNISIAN_CITIES.put("ben arous", new double[]{36.7533, 10.2283});
        TUNISIAN_CITIES.put("manouba", new double[]{36.8101, 10.0956});
        TUNISIAN_CITIES.put("la marsa", new double[]{36.8764, 10.3253});
        TUNISIAN_CITIES.put("carthage", new double[]{36.8528, 10.3233});
        TUNISIAN_CITIES.put("rades", new double[]{36.7680, 10.2760});
        TUNISIAN_CITIES.put("ennasr", new double[]{36.8438, 10.1619});
        TUNISIAN_CITIES.put("esprit", new double[]{36.8989, 10.1893});
        TUNISIAN_CITIES.put("el mourouj", new double[]{36.7370, 10.1980});
        TUNISIAN_CITIES.put("mourouj", new double[]{36.7370, 10.1980});
        TUNISIAN_CITIES.put("la soukra", new double[]{36.8534, 10.2158});
        TUNISIAN_CITIES.put("soukra", new double[]{36.8534, 10.2158});
        TUNISIAN_CITIES.put("den den", new double[]{36.8200, 10.1300});
        TUNISIAN_CITIES.put("bardo", new double[]{36.8093, 10.1346});
        TUNISIAN_CITIES.put("le bardo", new double[]{36.8093, 10.1346});
        TUNISIAN_CITIES.put("sidi bou said", new double[]{36.8687, 10.3417});
        TUNISIAN_CITIES.put("la goulette", new double[]{36.8182, 10.3050});
        TUNISIAN_CITIES.put("megrine", new double[]{36.7674, 10.2384});
        TUNISIAN_CITIES.put("hammam lif", new double[]{36.7262, 10.3378});
        TUNISIAN_CITIES.put("hammam chatt", new double[]{36.7410, 10.3453});
        TUNISIAN_CITIES.put("borj cedria", new double[]{36.7130, 10.3919});
        TUNISIAN_CITIES.put("ez zahra", new double[]{36.7460, 10.3087});
        TUNISIAN_CITIES.put("fouchana", new double[]{36.7061, 10.1650});
        TUNISIAN_CITIES.put("mnihla", new double[]{36.8283, 10.2053});
        TUNISIAN_CITIES.put("raoued", new double[]{36.8970, 10.2270});
        TUNISIAN_CITIES.put("oued ellil", new double[]{36.8300, 10.0600});
        TUNISIAN_CITIES.put("tebourba", new double[]{36.8330, 9.8330});
        TUNISIAN_CITIES.put("bab el khadra", new double[]{36.8100, 10.1700});
        TUNISIAN_CITIES.put("bab saadoun", new double[]{36.8140, 10.1660});
        TUNISIAN_CITIES.put("lac", new double[]{36.8323, 10.2340});
        TUNISIAN_CITIES.put("lac 1", new double[]{36.8323, 10.2340});
        TUNISIAN_CITIES.put("lac 2", new double[]{36.8470, 10.2680});
        TUNISIAN_CITIES.put("les berges du lac", new double[]{36.8400, 10.2500});
        TUNISIAN_CITIES.put("centre urbain nord", new double[]{36.8460, 10.1870});
        TUNISIAN_CITIES.put("el menzah", new double[]{36.8350, 10.1700});
        TUNISIAN_CITIES.put("el manar", new double[]{36.8300, 10.1500});
        // === Cap Bon ===
        TUNISIAN_CITIES.put("nabeul", new double[]{36.4513, 10.7357});
        TUNISIAN_CITIES.put("hammamet", new double[]{36.4000, 10.6167});
        TUNISIAN_CITIES.put("menzel bouzelfa", new double[]{36.6848, 10.5843});
        TUNISIAN_CITIES.put("menzel temime", new double[]{36.7831, 10.9831});
        TUNISIAN_CITIES.put("kelibia", new double[]{36.8464, 11.0939});
        TUNISIAN_CITIES.put("korba", new double[]{36.5773, 10.8583});
        TUNISIAN_CITIES.put("grombalia", new double[]{36.6037, 10.5019});
        TUNISIAN_CITIES.put("soliman", new double[]{36.6887, 10.4884});
        TUNISIAN_CITIES.put("beni khalled", new double[]{36.6500, 10.5900});
        TUNISIAN_CITIES.put("bou argoub", new double[]{36.5480, 10.5540});
        TUNISIAN_CITIES.put("beb alioua", new double[]{36.7930, 10.1790});
        // === Nord ===
        TUNISIAN_CITIES.put("bizerte", new double[]{37.2744, 9.8739});
        TUNISIAN_CITIES.put("beja", new double[]{36.7256, 9.1817});
        TUNISIAN_CITIES.put("jendouba", new double[]{36.5012, 8.7802});
        TUNISIAN_CITIES.put("le kef", new double[]{36.1680, 8.7096});
        TUNISIAN_CITIES.put("zaghouan", new double[]{36.4029, 10.1429});
        TUNISIAN_CITIES.put("siliana", new double[]{36.0849, 9.3708});
        TUNISIAN_CITIES.put("tabarka", new double[]{36.9544, 8.7580});
        TUNISIAN_CITIES.put("menzel bourguiba", new double[]{37.1530, 9.7862});
        // === Centre (Sahel) ===
        TUNISIAN_CITIES.put("sousse", new double[]{35.8245, 10.6346});
        TUNISIAN_CITIES.put("monastir", new double[]{35.7643, 10.8113});
        TUNISIAN_CITIES.put("mahdia", new double[]{35.5047, 11.0622});
        TUNISIAN_CITIES.put("kairouan", new double[]{35.6781, 10.0963});
        TUNISIAN_CITIES.put("msaken", new double[]{35.7318, 10.5820});
        TUNISIAN_CITIES.put("kalaa kebira", new double[]{35.8711, 10.5292});
        TUNISIAN_CITIES.put("enfidha", new double[]{36.1357, 10.3816});
        // === Sud ===
        TUNISIAN_CITIES.put("sfax", new double[]{34.7398, 10.7600});
        TUNISIAN_CITIES.put("kasserine", new double[]{35.1672, 8.8365});
        TUNISIAN_CITIES.put("sidi bouzid", new double[]{35.0382, 9.4849});
        TUNISIAN_CITIES.put("gabes", new double[]{33.8815, 10.0982});
        TUNISIAN_CITIES.put("medenine", new double[]{33.3540, 10.5055});
        TUNISIAN_CITIES.put("tataouine", new double[]{32.9297, 10.4518});
        TUNISIAN_CITIES.put("gafsa", new double[]{34.4250, 8.7842});
        TUNISIAN_CITIES.put("tozeur", new double[]{33.9197, 8.1336});
        TUNISIAN_CITIES.put("kebili", new double[]{33.7072, 8.9653});
        TUNISIAN_CITIES.put("djerba", new double[]{33.8076, 10.8451});
        TUNISIAN_CITIES.put("houmt souk", new double[]{33.8760, 10.8577});
        TUNISIAN_CITIES.put("zarzis", new double[]{33.5036, 11.1122});
    }

    /**
     * Geocode a Tunisian city name to [lat, lng]. Returns null if not found.
     */
    public static double[] geocodeCity(String cityName) {
        if (cityName == null || cityName.isBlank()) return null;
        String key = cityName.trim().toLowerCase();
        // Direct lookup
        if (TUNISIAN_CITIES.containsKey(key)) return TUNISIAN_CITIES.get(key);
        // Partial match
        for (Map.Entry<String, double[]> entry : TUNISIAN_CITIES.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static double calculate(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS_KM * c;
    }

    public static double cosineAngleBetweenVectors(double lat1, double lng1, double lat2, double lng2,
                                                     double lat3, double lng3, double lat4, double lng4) {
        double v1Lat = lat2 - lat1;
        double v1Lng = lng2 - lng1;
        double v2Lat = lat4 - lat3;
        double v2Lng = lng4 - lng3;

        double dot = v1Lat * v2Lat + v1Lng * v2Lng;
        double mag1 = Math.sqrt(v1Lat * v1Lat + v1Lng * v1Lng);
        double mag2 = Math.sqrt(v2Lat * v2Lat + v2Lng * v2Lng);

        if (mag1 == 0 || mag2 == 0) return 0;
        return dot / (mag1 * mag2);
    }
}
