package com.example.qklahpita.testmapapi10.map_direction;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by qklahpita on 11/26/17.
 */

public interface RetrofitService {
    @GET("json")
    Call<DirectionResponse> getDirection(@Query("origin") String origin,
                                         @Query("destination") String destination,
                                         @Query("key") String key);
}
