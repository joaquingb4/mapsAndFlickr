package com.example.gmaps;
import com.example.gmaps.models.photoSearchModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiCall  {
    @GET("services/rest/?method=flickr.photos.search")
    Call<photoSearchModel> getPhotos(@Query("api_key") String api_key,
                                     @Query("lat") double lat,
                                     @Query("lon") double lon);
}
