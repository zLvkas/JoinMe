package de.einluukas.joinme.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheManager {

    private final Cache<UUID, Integer> cachedTokens = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public void insertIntoCache(@NotNull UUID uniqueId, int tokens) {
        this.cachedTokens.put(uniqueId, tokens);
    }

    @Nullable
    public Integer getTokens(@NotNull UUID uniqueId) {
        return this.cachedTokens.getIfPresent(uniqueId);
    }
}