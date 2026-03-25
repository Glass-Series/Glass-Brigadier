package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.predicate.HasPermission;
import net.glasslauncher.glassbrigadier.api.storage.world.WorldModStorageFile;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.util.Formatting;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.MemorySection;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.api.predicate.IsPlayer.isPlayer;

public class WarpCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        HasPermission hasPermission = booleanPermission("command.warp");
        return GlassArgumentBuilder.literal("warp")
                .requires(source -> isPlayer().test(source) && hasPermission.test(source))
                .then(GlassArgumentBuilder.argument("name", word())
                        .executes(this::warp)
                );
    }

    public int warp(CommandContext<GlassCommandSource> context) {
        String name = context.getArgument("name", String.class);

        if (booleanPermission("command.warp." + name).test(context.getSource())) {
            context.getSource().sendFeedback(Formatting.RED + "You do not have permission to warp to " + name + ".");
            return 0;
        }

        WorldModStorageFile serverStorage = WorldModStorageFile.of(GlassBrigadier.NAMESPACE.id("warps"));
        MemorySection warps = (MemorySection) serverStorage.get("warps");
        ConfigurationSection warp = (ConfigurationSection) warps.get(name);

        if (warp == null) {
            context.getSource().sendFeedback(Formatting.RED + "No warp named \"" + name + "\".");
            return 0;
        }

        List<Double> warpLoc = warp.getDoubleList("location");

        if (context.getSource().getPlayer() instanceof ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.networkHandler.teleport(warpLoc.get(0), warpLoc.get(1), warpLoc.get(2), serverPlayerEntity.yaw, serverPlayerEntity.pitch);
        }
        else {
            PlayerEntity player = context.getSource().getPlayer();
            player.setPositionAndAnglesKeepPrevAngles(warpLoc.get(0), warpLoc.get(1), warpLoc.get(2), player.yaw, player.pitch);
        }

        context.getSource().sendFeedback(GlassBrigadier.systemMessage("Warped to \"" + name + "\"."));
        return 0;
    }
}
