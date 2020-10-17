package de.einluukas.joinme.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class UUIDFetcher {

    private UUIDFetcher() {
        throw new UnsupportedOperationException();
    }

    private static final JsonParser jsonParser = new JsonParser();

    private static final Pattern PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private static final Cache<String, UUID> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build();

    @Nullable
    public static UUID getUUIDFromName(@NotNull String name) {
        UUID uuid = cache.getIfPresent(name);
        if (uuid != null) {
            return uuid;
        }

        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            httpURLConnection.setDoOutput(false);
            httpURLConnection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            httpURLConnection.setUseCaches(true);
            httpURLConnection.connect();

            try (InputStreamReader reader = new InputStreamReader(httpURLConnection.getInputStream())) {
                JsonElement jsonElement = jsonParser.parse(reader);
                if (jsonElement.isJsonNull()) {
                    return null;
                }

                if (!jsonElement.isJsonObject()) {
                    return null;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (!jsonObject.has("id")) {
                    return null;
                }

                uuid = fromString(PATTERN.matcher(jsonObject.get("id").getAsString()).replaceAll("$1-$2-$3-$4-$5"));
                cache.put(name, uuid);
                return uuid;
            }
        } catch (final IOException ignored) {
        }

        return null;
    }

    @NotNull
    private static UUID fromString(@NotNull String name) throws IllegalArgumentException {
        String[] components = name.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }

        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }

        long mostSigBits = Long.decode(components[0]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]);

        long leastSigBits = Long.decode(components[3]);
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]);

        return new UUID(mostSigBits, leastSigBits);
    }
}