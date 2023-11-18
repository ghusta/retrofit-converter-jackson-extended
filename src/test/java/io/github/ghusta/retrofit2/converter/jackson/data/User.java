package io.github.ghusta.retrofit2.converter.jackson.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    @JsonView(Views.Public.class)
    private String twitterHandle;

    @JsonProperty
    @JsonView(Views.Secret.class)
    private String password;

}
