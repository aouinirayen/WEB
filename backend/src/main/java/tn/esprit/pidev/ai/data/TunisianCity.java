package tn.esprit.pidev.ai.data;

public class TunisianCity {

    private final String name;
    private final double lat;
    private final double lng;

    public TunisianCity(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }

    public static final TunisianCity[] CITIES = {
        new TunisianCity("Tunis", 36.8065, 10.1815),
        new TunisianCity("Sousse", 35.8245, 10.6346),
        new TunisianCity("Sfax", 34.7398, 10.7600),
        new TunisianCity("Nabeul", 36.4513, 10.7357),
        new TunisianCity("Bizerte", 37.2744, 9.8739),
        new TunisianCity("Kairouan", 35.6781, 10.0963),
        new TunisianCity("Gabes", 33.8815, 10.0982),
        new TunisianCity("Monastir", 35.7643, 10.8113),
        new TunisianCity("Ariana", 36.8625, 10.1956),
        new TunisianCity("Ben Arous", 36.7533, 10.2283),
        new TunisianCity("La Marsa", 36.8764, 10.3253),
        new TunisianCity("Hammamet", 36.4000, 10.6167),
        new TunisianCity("Mahdia", 35.5047, 11.0622),
        new TunisianCity("Tozeur", 33.9197, 8.1335),
        new TunisianCity("Gafsa", 34.4250, 8.7842),
        new TunisianCity("Medenine", 33.3540, 10.5055),
        new TunisianCity("Tataouine", 32.9297, 10.4518),
        new TunisianCity("Jendouba", 36.5014, 8.7802),
        new TunisianCity("Beja", 36.7256, 9.1817),
        new TunisianCity("Le Kef", 36.1747, 8.7047),
        new TunisianCity("Siliana", 36.0849, 9.3708),
        new TunisianCity("Zaghouan", 36.4029, 10.1429),
        new TunisianCity("Manouba", 36.8101, 10.0956),
        new TunisianCity("Kasserine", 35.1676, 8.8365)
    };
}
