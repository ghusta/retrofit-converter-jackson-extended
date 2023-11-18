package io.github.ghusta.retrofit2.converter.jackson.data;

public class Views {
    public interface All extends Public, Secret {
    }

    public interface Public {
    }

    /**
     * Secret view could extend {@link Public} to also fetch other data
     */
    public interface Secret {
    }
}
