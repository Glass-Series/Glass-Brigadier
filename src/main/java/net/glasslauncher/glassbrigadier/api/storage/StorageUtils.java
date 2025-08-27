package net.glasslauncher.glassbrigadier.api.storage;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.AlphaWorldStorage;
import net.modificationstation.stationapi.api.registry.DimensionRegistry;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;

import java.io.File;

public class StorageUtils {

    public static File getPlayerStorageFile(String playerName) {
        return new File(getPlayersDir(), playerName + "." + GlassBrigadier.NAMESPACE + ".yaml");
    }

    public static File getPlayerStorageFile(PlayerEntity player) {
        return new File(getPlayersDir(), player.name + "." + GlassBrigadier.NAMESPACE + ".yaml");
    }

    public static File getModStorageFile(Identifier identifier) {
        return new File(getWorldDir(), GlassBrigadier.NAMESPACE + "_storages/" + identifier.namespace + "/" + identifier.path + ".yaml");
    }


    public static File getPlayersDir() {
        return getWorldStorage().playerDataDir;
    }

    public static File getWorldDir() {
        return getWorldStorage().dir;
    }

    public static AlphaWorldStorage getWorldStorage() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return (AlphaWorldStorage) Minecraft.INSTANCE.world.storage;
        }

        //noinspection deprecation
        MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
        //noinspection OptionalGetWithoutIsPresent If this is null, we're ALL fucked.
        return (AlphaWorldStorage) server.getWorld(DimensionRegistry.INSTANCE.getLegacyId(Identifier.of(Namespace.MINECRAFT, "overworld")).getAsInt()).storage;
    }
}
