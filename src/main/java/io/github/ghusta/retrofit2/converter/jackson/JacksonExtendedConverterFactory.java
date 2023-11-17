package io.github.ghusta.retrofit2.converter.jackson;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@link ObjectMapper} is configured by default to also serialize fields
 * not annotated with {@link JsonView @JsonView}.
 * You can disable this like so :
 * <pre>
 *  objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
 * </pre>
 */
public class JacksonExtendedConverterFactory extends Converter.Factory {

    /**
     * Create an instance using a default {@link ObjectMapper} instance for conversion.
     */
    public static JacksonExtendedConverterFactory create() {
        return create(new ObjectMapper());
    }

    /**
     * Create an instance using {@code mapper} for conversion.
     */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static JacksonExtendedConverterFactory create(ObjectMapper mapper) {
        Objects.requireNonNull(mapper);
        return new JacksonExtendedConverterFactory(mapper);
    }

    private final ObjectMapper mapper;

    private JacksonExtendedConverterFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        ObjectReader reader;
        JsonView jsonViewAnnot = findFirstAnnotation(annotations, JsonView.class);
        if (jsonViewAnnot != null) {
            Class<?>[] views = jsonViewAnnot.value();
            if (views.length != 1) {
                throw new IllegalArgumentException("@JsonView only supported for response with exactly 1 class argument");
            }
            reader = mapper.readerWithView(views[0]).forType(javaType);
        } else {
            reader = mapper.readerFor(javaType);
        }

        return new JacksonExtendedResponseBodyConverter<>(reader);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations,
                                                          Retrofit retrofit) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        ObjectWriter writer = mapper.writerFor(javaType);
        return new JacksonExtendedRequestBodyConverter<>(writer);
    }

    private static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> requestedAnnotation) {
        return Arrays.stream(annotations).anyMatch(requestedAnnotation::isInstance);
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A findFirstAnnotation(Annotation[] annotations, Class<A> requestedAnnotation) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(requestedAnnotation)) {
                return (A) annotation;
            }
        }
        return null;
    }

}
