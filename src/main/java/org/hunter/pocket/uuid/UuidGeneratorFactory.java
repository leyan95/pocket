package org.hunter.pocket.uuid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/2/14
 */
public class UuidGeneratorFactory {
    private static Map<String, UuidGenerator> GENERATOR_POOL = new ConcurrentHashMap<>(6);
    private static UuidGeneratorFactory ourInstance = new UuidGeneratorFactory();

    public static UuidGeneratorFactory getInstance() {
        return ourInstance;
    }

    private UuidGeneratorFactory() {
    }

    public UuidGenerator getUuidGenerator(String generatorId) {
        return GENERATOR_POOL.get(generatorId);
    }

    public void registerGenerator(UuidGenerator uuidGenerator) {
        synchronized (this) {
            if (GENERATOR_POOL.containsKey(uuidGenerator.getGeneratorId())) {
                throw new RuntimeException("This logo already exists. Please use another one.");
            } else {
                GENERATOR_POOL.put(uuidGenerator.getGeneratorId(), uuidGenerator);
            }
        }
    }
}