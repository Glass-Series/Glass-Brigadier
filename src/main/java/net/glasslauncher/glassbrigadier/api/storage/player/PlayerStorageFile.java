package net.glasslauncher.glassbrigadier.api.storage.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.storage.StorageFile;
import net.glasslauncher.glassbrigadier.api.storage.StorageUtils;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.MemoryConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * This is used instead of attaching to player NBT because:
 * <br>
 * 1. NBT is shit in beta, actually awful. I need to make an *extensive* API to fix this.
 * <br>
 * 2. NBT files are not easily read and edited. Yes, I know there are programs, but being able to just open a text file is much easier.
 * <br>
 * 3. Invasive mixins could entirely break the NBT approach and cause data corruption. This *needs* to be as reliable as possible.
 */
public class PlayerStorageFile extends StorageFile {
    public static final Configuration PLAYER_DATA_DEFAULTS = new MemoryConfiguration();

    private static final @NotNull Cache<@NotNull String, @NotNull PlayerStorageFile> CACHE = Caffeine.newBuilder().softValues().build();
    private static final @NotNull Function<@NotNull String, @NotNull PlayerStorageFile> PLAYER_STORAGE_FILE_FACTORY = (playerName) -> {
        PlayerStorageFile file = new PlayerStorageFile(StorageUtils.getPlayerStorageFile(playerName));
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

    public static PlayerStorageFile of(PlayerEntity player) {
        return of(player.name);
    }

    public static PlayerStorageFile of(String playerName) {
        return CACHE.get(playerName, PLAYER_STORAGE_FILE_FACTORY);
    }

    protected PlayerStorageFile(File file) {
        super(file);

        setHeader(GlassBrigadier.NAMESPACE.getName().toUpperCase() + " " + """
                PLAYER STORAGE FILE
                DO NOT EDIT WITHOUT BACKING UP FIRST
                """);

        setDefaults(PLAYER_DATA_DEFAULTS);
    }
}
