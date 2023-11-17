package io.github.ghusta.retrofit2.converter.jackson;

import com.fasterxml.jackson.databind.ObjectReader;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;

class JacksonExtendedResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final ObjectReader adapter;

    public JacksonExtendedResponseBodyConverter(ObjectReader adapter) {
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            return adapter.readValue(value.charStream());
        } finally {
            value.close();
        }
    }

}
