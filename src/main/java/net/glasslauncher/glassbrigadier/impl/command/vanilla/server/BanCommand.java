package net.glasslauncher.glassbrigadier.impl.command.vanilla.server;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.glasslauncher.glassbrigadier.impl.argument.GlassCommandBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.modificationstation.stationapi.api.util.Formatting;

import static net.glasslauncher.glassbrigadier.GlassBrigadier.systemMessage;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.modificationstation.stationapi.api.util.Formatting.RED;

public class BanCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassCommandBuilder.literal("ban", "Ban a given player.")
                .requires(booleanPermission("command.ban"))
                .then(GlassArgumentBuilder.argument("player", StringArgumentType.word())
                        .executes(this::banPlayer)
                );
    }

    public int banPlayer(CommandContext<GlassCommandSource> context) {
        String player = context.getArgument("player", String.class);

        //noinspection deprecation
        PlayerManager playerManager = ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).playerManager;
        if (playerManager.bannedPlayers.contains(player.toLowerCase().strip())) {
            context.getSource().sendFeedback(RED + player + " is already banned!");
            return 0;
        }

        playerManager.banPlayer(player);
        ServerPlayerEntity playerEntity = ((ServerPlayerEntity) context.getSource().getPlayerByName(player));
        if (playerEntity != null) {
            playerEntity.networkHandler.disconnect(RED + "Banned by admin.");
        }
        sendFeedbackAndLog(context.getSource(), systemMessage(RED + "Banned " + player + "."));

        return 0;
    }
}
