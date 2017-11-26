package com.example.qklahpita.testmapapi10.map_direction;

import java.util.List;

/**
 * Created by qklahpita on 11/26/17.
 */

public class DirectionResponse {
    public List<RouteJSON> routes;
    public String status;

    public class RouteJSON {
        public OverviewJSON overview_polyline;
        public List<LegsJSON> legs;

        public class OverviewJSON {
            public String points;
        }

        public class LegsJSON {
            public DistanceJSON distance;
            public DurationJSON duration;
            public EndLocationJSON end_location;
            public StartLocationJSON start_location;

            public class DistanceJSON {
                public String text;
            }

            public class DurationJSON {
                public String text;
            }

            public class EndLocationJSON {
                public double lat;
                public double lng;
            }

            public class StartLocationJSON {
                public double lat;
                public double lng;
            }
        }
    }
}
