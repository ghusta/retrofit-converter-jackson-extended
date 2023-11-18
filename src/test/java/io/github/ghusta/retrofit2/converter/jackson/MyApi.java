package io.github.ghusta.retrofit2.converter.jackson;

import com.fasterxml.jackson.annotation.JsonView;
import io.github.ghusta.retrofit2.converter.jackson.data.User;
import io.github.ghusta.retrofit2.converter.jackson.data.Views;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyApi {

    /**
     * Endpoint not using {@link JsonView}
     */
    @GET("users/{id}")
    Call<User> getUser(@Path("id") String id);

    @GET("users/{id}")
    @JsonView(Views.All.class)
    Call<User> getUserWithAllData(@Path("id") String id);

    @GET("users/{id}")
    @JsonView(Views.Public.class)
    Call<User> getUserWithPublicData(@Path("id") String id);

    @GET("users/{id}")
    @JsonView(Views.Secret.class)
    Call<User> getUserWithSecretData(@Path("id") String id);

    /**
     * Illegal use of {@link JsonView} !
     */
    @GET("users/{id}")
    @JsonView({Views.Public.class, Views.Secret.class})
    Call<User> getUserWithMultipleViews(@Path("id") String id);

    @POST("users")
    @JsonView(Views.Public.class)
    Call<Void> createUser(@Body User newUser);

}
