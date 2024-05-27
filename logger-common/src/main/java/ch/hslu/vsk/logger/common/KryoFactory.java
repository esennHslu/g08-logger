package ch.hslu.vsk.logger.common;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.TimeSerializers;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.SerializingInstantiatorStrategy;

import java.time.Instant;

/**
 * Shared factory for producing consistently configured {@link Kryo} instances in all components.
 */
public class KryoFactory {

    private KryoFactory() {
        throw new AssertionError();
    }

    /**
     * Creates and configures a new {@link Kryo} instance to be able to serialize {@link LogMessageDo} objects.
     *
     * @return configured instance
     */
    public static Kryo createConfiguredKryoInstance() {
        var kryo = new Kryo();
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new SerializingInstantiatorStrategy()));
        kryo.register(Instant.class, new TimeSerializers.InstantSerializer(), 1);
        kryo.register(LogLevel.class, new DefaultSerializers.EnumSerializer(LogLevel.class), 2);
        kryo.register(LogMessageDo.class, 3);
        return kryo;
    }
}
