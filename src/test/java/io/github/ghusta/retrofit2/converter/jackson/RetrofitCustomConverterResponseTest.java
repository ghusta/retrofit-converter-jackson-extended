package io.github.ghusta.retrofit2.converter.jackson;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.ghusta.retrofit2.converter.jackson.data.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
public class RetrofitCustomConverterResponseTest {

    public static final String API_WIREMOCK_BASE_URL = "http://localhost:%d/";

    private static MyApi myApi;

    @BeforeAll
    static void setUpClass(WireMockRuntimeInfo wmRuntimeInfo) {
        assertThat(wmRuntimeInfo).isNotNull();
        // random/dynamic server port
        int mockServerPort = wmRuntimeInfo.getHttpPort();

        String apiBaseUrl = String.format(API_WIREMOCK_BASE_URL, mockServerPort);

        ObjectMapper objectMapper = JsonMapper.builder()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true) // default = true
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(JacksonExtendedConverterFactory.create(objectMapper))
                .build();

        myApi = retrofit.create(MyApi.class);
    }

    @Test
    void shouldEndpointGetUserReturnFullData(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        // Voir : https://wiremock.org/docs/junit-jupiter/
        // Instance DSL can be obtained from the runtime info parameter
        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/users/1")
                .willReturn(okJson(readStringFromFile("wiremock/stubs/response-users-001.json"))));

        User user1 = myApi.getUser("1")
                .execute().body();
        assertThat(user1).isNotNull();
        assertThat(user1.getId()).isEqualTo(1L);
        assertThat(user1.getName()).isNotBlank();
        assertThat(user1.getTwitterHandle()).isNotBlank();
        assertThat(user1.getPassword()).isNotBlank();
    }

    @Test
    void shouldEndpointGetUserWithViewAllReturnData(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        // Voir : https://wiremock.org/docs/junit-jupiter/
        // Instance DSL can be obtained from the runtime info parameter
        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/users/1")
                .willReturn(okJson(readStringFromFile("wiremock/stubs/response-users-001.json"))));

        User user1 = myApi.getUserWithAllData("1")
                .execute().body();
        assertThat(user1).isNotNull();
        assertThat(user1.getId()).isEqualTo(1L);
        assertThat(user1.getName()).isNotBlank();
        assertThat(user1.getTwitterHandle()).isNotBlank();
        assertThat(user1.getPassword()).isNotBlank();
    }

    @Test
    void shouldEndpointGetUserWithViewSecretReturnData(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        // Voir : https://wiremock.org/docs/junit-jupiter/
        // Instance DSL can be obtained from the runtime info parameter
        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/users/1")
                .willReturn(okJson(readStringFromFile("wiremock/stubs/response-users-001.json"))));

        User user1 = myApi.getUserWithSecretData("1")
                .execute().body();
        assertThat(user1).isNotNull();
        assertThat(user1.getId()).isEqualTo(1L);
        // password should be deserialized with Secret view
        assertThat(user1.getPassword()).isNotBlank();
    }

    @Test
    void shouldEndpointCallWithMultipleViewsFail(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        // Voir : https://wiremock.org/docs/junit-jupiter/
        // Instance DSL can be obtained from the runtime info parameter
        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/users/1")
                .willReturn(okJson(readStringFromFile("wiremock/stubs/response-users-001.json"))));

        assertThrows(IllegalArgumentException.class, () -> {
            User user1 = myApi.getUserWithMultipleViews("1")
                    .execute().body();
        });
    }

    /**
     * Recherche fichier dans classpath.
     */
    public static String readStringFromFile(String filename) throws IOException, URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(RetrofitCustomConverterResponseTest.class.getClassLoader().getResource(filename)).toURI());
        return String.join("", Files.readAllLines(path));
    }

}
