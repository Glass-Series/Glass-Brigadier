package net.glasslauncher.glassbrigadier.impl.command.vanilla.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.glasslauncher.glassbrigadier.impl.argument.GlassCommandBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.modificationstation.stationapi.api.util.Formatting;

import static net.glasslauncher.glassbrigadier.GlassBrigadier.systemMessage;
import static net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType.getPlayers;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;

public class PardonCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassCommandBuilder.literal("pardon", "Unban a player.")
                .alias("unban")
                .requires(booleanPermission("command.pardon"))
                .then(GlassArgumentBuilder.argument("player", TargetSelectorArgumentType.entity())
                        .executes(this::pardonPlayer)
                );
    }

    public int pardonPlayer(CommandContext<GlassCommandSource> context) {
        getPlayers(context, "player").getEntities(context.getSource()).forEach(player -> {
            //noinspection deprecation
            PlayerManager playerManager = ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).playerManager;
            if (!playerManager.bannedPlayers.contains(player.name.toLowerCase().strip())) {
                context.getSource().sendFeedback(Formatting.RED + player.name + " isn't banned!");
                return;
            }
            playerManager.unbanPlayer(player.name);
            sendFeedbackAndLog(context.getSource(), systemMessage("Unbanned " + player.name + "."));
        });
        return 0;
    }
}
