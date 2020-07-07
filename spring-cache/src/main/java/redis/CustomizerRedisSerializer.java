package redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Assert;

/**
 * @author renchao
 * @since v1.0
 */
public class CustomizerRedisSerializer<T> implements RedisSerializer<T> {

    private Class clazzType;

    private ObjectMapper mapper;

    public CustomizerRedisSerializer() {
        this(new ObjectMapper());
    }

    public CustomizerRedisSerializer(ObjectMapper mapper) {
        Assert.notNull(mapper, "ObjectMapper must not be null!");
        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(T source) throws SerializationException {
        if (source == null) {
            return new byte[0];
        }

        try {
            clazzType = source.getClass();
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return (T) this.mapper.readValue(bytes, clazzType);
        } catch (Exception ex) {
            throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

}
