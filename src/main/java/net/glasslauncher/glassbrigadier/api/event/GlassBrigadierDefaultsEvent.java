package net.glasslauncher.glassbrigadier.api.event;

import lombok.experimental.SuperBuilder;
import net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile;
import net.glasslauncher.glassbrigadier.api.storage.world.WorldModStorageFile;
import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.event.EventPhases;
import org.simpleyaml.configuration.Configuration;

import java.util.Map;

@SuperBuilder
public class GlassBrigadierDefaultsEvent extends Event {

    public void addPlayerDefault(String key, Object entry) {
        PlayerStorageFile.PLAYER_DATA_DEFAULTS.addDefault(key, entry);
    }

    public void addPlayerDefaults(Map<String, Object> map) {
        PlayerStorageFile.PLAYER_DATA_DEFAULTS.addDefaults(map);
    }

    public void addPlayerDefaults(Configuration defaults) {
        PlayerStorageFile.PLAYER_DATA_DEFAULTS.addDefaults(defaults);
    }

    public Configuration getPlayerDefaults() {
        return PlayerStorageFile.PLAYER_DATA_DEFAULTS;
    }

    public void addWorldDefault(String key, Object entry) {
        WorldModStorageFile.WORLD_DATA_DEFAULTS.addDefault(key, entry);
    }

    public void addWorldDefaults(Map<String, Object> map) {
        WorldModStorageFile.WORLD_DATA_DEFAULTS.addDefaults(map);
    }

    public void addWorldDefaults(Configuration defaults) {
        WorldModStorageFile.WORLD_DATA_DEFAULTS.addDefaults(defaults);
    }

    public Configuration getWorldDefaults() {
        return WorldModStorageFile.WORLD_DATA_DEFAULTS;
    }

}
