package net.glasslauncher.glassbrigadier.impl.command.vanilla;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.entity.EntityUtils;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static net.glasslauncher.glassbrigadier.api.argument.coordinate.CoordinateArgumentType.coordinate;
import static net.glasslauncher.glassbrigadier.api.argument.coordinate.CoordinateArgumentType.getCoordinate;
import static net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType.*;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.api.predicate.IsPlayer.isPlayer;

public class TeleportCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("tp")
                .requires(booleanPermission("command.tp"))
                .then(GlassArgumentBuilder.argument("pos", coordinate())
                        .requires(isPlayer())
                        .executes(this::teleportToPosition)
                )
                .then(GlassArgumentBuilder.argument("target", entity())
                        .requires(isPlayer())
                        .executes(this::teleportToEntity)
                )
                .then(GlassArgumentBuilder.argument("teleportees", entities())
                        .then(GlassArgumentBuilder.argument("pos", coordinate())
                                .executes(this::teleportOtherToPosition)
                        )
                        .then(GlassArgumentBuilder.argument("target", entity())
                                .executes(this::teleportOtherToEntity)
                        )
                );
    }

    private int teleportToPosition(CommandContext<GlassCommandSource> ctx) {
        GlassCommandSource sender = ctx.getSource();
        Vec3d position = getCoordinate(ctx, "pos").getVec3d(sender);
        this.teleport(sender.getEntity(), position, sender.getEntity().yaw, sender.getEntity().pitch, sender);
        return 1;
    }

    private int teleportToEntity(CommandContext<GlassCommandSource> ctx) {
        GlassCommandSource sender = ctx.getSource();
        Entity target = getEntities(ctx, "target").getEntities(sender).get(0);
        this.teleport(sender.getEntity(), EntityUtils.getPosition(target), target.yaw, target.pitch, sender);
        return 1;
    }

    private int teleportOtherToPosition(CommandContext<GlassCommandSource> ctx) {
        GlassCommandSource sender = ctx.getSource();
        List<Entity> teleportees = getEntities(ctx, "teleportees").getEntities(sender);
        Vec3d position = getCoordinate(ctx, "pos").getVec3d(sender);
        teleportees.forEach(e -> this.teleport(e, position, e.yaw, e.pitch, sender));
        return teleportees.size();
    }

    private int teleportOtherToEntity(CommandContext<GlassCommandSource> ctx) {
        GlassCommandSource sender = ctx.getSource();
        List<Entity> teleportees = getEntities(ctx, "teleportees").getEntities(sender);
        Entity target = getEntities(ctx, "target").getEntities(sender).get(0);
        teleportees.forEach(e -> this.teleport(e, EntityUtils.getPosition(target), e.yaw, e.pitch, sender));
        return teleportees.size();
    }

    private void teleport(Entity entity, Vec3d position, float yaw, float pitch, GlassCommandSource sender) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && entity instanceof ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.networkHandler.teleport(position.x, position.y, position.z, yaw, pitch);
        } else {
            entity.setPositionAndAnglesKeepPrevAngles(position.x, position.y, position.z, yaw, pitch);
        }

        sendFeedbackAndLog(sender, String.format("Teleporting %s to %s (%f %f)", EntityUtils.getName(entity), position, yaw, pitch));
    }
}
