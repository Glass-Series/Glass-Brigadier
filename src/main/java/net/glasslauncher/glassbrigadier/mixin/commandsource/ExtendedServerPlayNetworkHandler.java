package net.glasslauncher.glassbrigadier.mixin.commandsource;

import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.permission.PermissionNodeInstance;
import net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile;
import net.glasslauncher.glassbrigadier.impl.permission.UserPermissionManagerImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayNetworkHandler.class)
public class ExtendedServerPlayNetworkHandler implements GlassCommandSource {

    @Shadow public ServerPlayerEntity player;

    @Shadow public MinecraftServer server;

    @Shadow public String getName() { throw new AssertionError(); }

    @Shadow public void sendMessage(String message) {}

    @Override
    public String getSourceName() {
        return getName();
    }

    @Override
    public void sendFeedback(String message) {
        sendMessage(message);
    }

    @Override
    public World getWorld() {
        if (getPlayer() != null) {
            return getPlayer().world;
        }
        return null;
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
        return UserPermissionManagerImpl.getNodes(getSourceName());
    }

    @Override
    public boolean satisfiesNode(PermissionNodeInstance<?> nodeToCheck) {
        if (server.playerManager.isOperator(getSourceName())) {
            return true;
        }
        if (nodeToCheck == null) {
            return false;
        }
        return nodeToCheck.isSatisfiedBy(getPermissions());
    }

    @Override
    public Entity getEntity() {
        return this.getPlayer();
    }

    @Nullable
    @Override
    public PlayerEntity getPlayer() {
        return this.player;
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
        //noinspection DataFlowIssue If you're null then something real bad happened.
        return PlayerStorageFile.of(getPlayer());
    }

    @Override
    public boolean isOp() {
        return server.playerManager.isOperator(Objects.requireNonNull(getPlayer()).name);
    }
}
