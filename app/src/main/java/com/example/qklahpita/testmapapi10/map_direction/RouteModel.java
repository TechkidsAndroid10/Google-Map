package com.example.qklahpita.testmapapi10.map_direction;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by qklahpita on 11/26/17.
 */

public class RouteModel {
    public String distance;
    public String duration;
    public LatLng endLocation;
    public LatLng startLocation;

    public List<LatLng> points;
}
