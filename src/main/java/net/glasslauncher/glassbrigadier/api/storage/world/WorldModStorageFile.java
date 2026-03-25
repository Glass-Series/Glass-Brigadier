package net.glasslauncher.glassbrigadier.api.storage.world;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.storage.StorageFile;
import net.glasslauncher.glassbrigadier.api.storage.StorageUtils;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.MemoryConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * This is used instead of using StAPI's persistent data system because:
 * <br>
 * 1. NBT is shit in beta, actually awful. I need to make an *extensive* API to fix this.
 * <br>
 * 2. NBT files are not easily read and edited. Yes, I know there are programs, but being able to just open a text file is much easier.
 * <br>
 * 3. This way makes it fall in line with {@link net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile}.
 *
 */
public class WorldModStorageFile extends StorageFile {
    public static final Configuration WORLD_DATA_DEFAULTS = new MemoryConfiguration();

    private static final @NotNull Cache<@NotNull Identifier, @NotNull WorldModStorageFile> CACHE = Caffeine.newBuilder().softValues().build();
    private static final @NotNull Function<@NotNull Identifier, @NotNull WorldModStorageFile> WORLD_STORAGE_FILE_FACTORY = (identifier) -> {
        File modFile = StorageUtils.getModStorageFile(identifier);
        if (!modFile.getParentFile().exists()) {
            modFile.getParentFile().mkdirs();
        }
        WorldModStorageFile file = new WorldModStorageFile(modFile);
        try {
            if (file.exists()) {
                file.load();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    };

    public static void invalidateAll() {
        CACHE.invalidateAll();
    }

    public static WorldModStorageFile of(Namespace namespace) {
        return of(namespace.id("storage"));
    }

    public static WorldModStorageFile of(Identifier identifier) {
        return CACHE.get(identifier, WORLD_STORAGE_FILE_FACTORY);
    }

    protected WorldModStorageFile(File file) {
        super(file);

        setHeader(GlassBrigadier.NAMESPACE.getName().toUpperCase() + " " + """
                WORLD STORAGE FILE
                DO NOT EDIT WITHOUT BACKING UP FIRST
                """);

        setDefaults(WORLD_DATA_DEFAULTS);
    }
}
