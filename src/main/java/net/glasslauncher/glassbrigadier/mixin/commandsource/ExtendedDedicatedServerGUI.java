package net.glasslauncher.glassbrigadier.mixin.commandsource;

import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.permission.PermissionNodeInstance;
import net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.gui.DedicatedServerGui;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.registry.DimensionRegistry;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import org.lwjgl.util.vector.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// I'm pretty certain this is never ever used, but hey.
@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(DedicatedServerGui.class)
public class ExtendedDedicatedServerGUI implements GlassCommandSource {

    @Shadow private MinecraftServer server;

    @Shadow public String getName() { throw new AssertionError(); }

    @Shadow public void sendMessage(String message) {}

    @Override public String getSourceName() {
        return getName();
    }

    @Override public void sendFeedback(String message) {
        sendMessage(message);
    }

    @Override
    public World getWorld() {
        //noinspection DataFlowIssue
        return server.getWorld(DimensionRegistry.INSTANCE.get(Identifier.of(Namespace.MINECRAFT, "overworld")).getLegacyID());
    }

    @Override
    public Vec3d getPosition() {
        PlayerEntity player = getPlayer();
        if (player != null) {
            return Vec3d.createCached(player.x, player.y, player.z);
        }

        return Vec3d.createCached(0, 0, 0);
    }

    @Override
    public Vector2f getRotation() {
        PlayerEntity player = getPlayer();
        if (player != null) {
            return new Vector2f(player.pitch, player.yaw);
        }
        return new Vector2f(0f, 0f);
    }

    @Override
    public Set<PermissionNodeInstance<?>> getPermissions() {
        return Set.of();
    }

    @Override
    public boolean satisfiesNode(PermissionNodeInstance<?> nodeToCheck) {
        return true;
    }

    @Override
    public Entity getEntity() {
        return null;
    }

    @Nullable
    @Override
    public PlayerEntity getPlayer() {
        return null;
    }

    @Override
    public @Nullable PlayerEntity getPlayerByName(String playerName) {
        return server.playerManager.getPlayer(playerName);
    }

    @Override
    public List<PlayerEntity> getAllPlayers() {
        //noinspection unchecked
        return new ArrayList<PlayerEntity>(server.playerManager.players);
    }

    @Override
    public PlayerStorageFile getStorage() {
        return null;
    }

    @Override
    public boolean isOp() {
        return true;
    }
}
