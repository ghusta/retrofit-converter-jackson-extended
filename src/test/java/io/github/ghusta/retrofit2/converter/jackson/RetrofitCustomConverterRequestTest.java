package io.github.ghusta.retrofit2.converter.jackson;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.ghusta.retrofit2.converter.jackson.data.User;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;

class RetrofitCustomConverterRequestTest {

    private MyApi myApi;

    private Request savedRequest;

    @BeforeEach
    void setUp() {
        String apiBaseUrl = "http://localhost:8080/";

        ObjectMapper objectMapper = JsonMapper.builder()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true) // default = true
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    savedRequest = request.newBuilder().build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .callFactory((Call.Factory) okHttpClient) // cast for OkHttp 4.x compatibility (built with Kotlin)
                .addConverterFactory(JacksonExtendedConverterFactory.create(objectMapper))
                .build();

        myApi = retrofit.create(MyApi.class);
    }

    @Test
    void callRequestWithData() throws IOException {
        User newUser = new User();
        newUser.setId(100L);
        newUser.setName("Bob");
        newUser.setTwitterHandle("bob44");
        newUser.setPassword("azerty");

        try {
            Response<Void> response = myApi.createUser(newUser)
                    .execute();
            // assertThat(response.isSuccessful()).isTrue();
        } catch (ConnectException e) {
            // ignore
        }
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.body()).isNotNull();
        assertThat(savedRequest.body().contentLength()).isEqualTo(47);
    }

    static class TestCallFactory implements Call.Factory {

        private final Call.Factory delegate;

        private Request savedRequest;

        public TestCallFactory(Call.Factory delegate) {
            this.delegate = delegate;
        }

        @Override
        public Call newCall(Request request) {
            // copy original request
            this.savedRequest = request.newBuilder().build();
            return delegate.newCall(request);
        }

        public Request getSavedRequest() {
            return savedRequest;
        }
    }

}
