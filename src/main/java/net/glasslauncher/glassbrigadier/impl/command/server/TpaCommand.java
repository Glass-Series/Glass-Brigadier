package net.glasslauncher.glassbrigadier.impl.command.server;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.modificationstation.stationapi.api.util.Formatting;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType.getPlayers;
import static net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType.player;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.api.predicate.IsPlayer.isPlayer;

public class TpaCommand implements CommandProvider {
    /// Username to last made request.
    public static final Multimap<String, TpaRequest> TPA_REQUESTS_TO = MultimapBuilder.hashKeys().arrayListValues().build();
    public static final Map<String, TpaRequest> TPA_REQUESTS_FROM = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("tpa")
                .requires(booleanPermission("command.tpa"))
                .requires(isPlayer())
                .then(GlassArgumentBuilder.literal("yes")
                        .executes(this::tpYes)
                        .then(GlassArgumentBuilder.argument("player", player())
                                .executes(this::tpYes)
                        )
                )
                .then(GlassArgumentBuilder.literal("y")
                        .executes(this::tpYes)
                        .then(GlassArgumentBuilder.argument("player", player())
                                .executes(this::tpYes)
                        )
                )
                .then(GlassArgumentBuilder.literal("no")
                        .executes(this::tpNo)
                        .then(GlassArgumentBuilder.argument("player", player())
                                .executes(this::tpNo)
                        )
                )
                .then(GlassArgumentBuilder.literal("n")
                        .executes(this::tpNo)
                        .then(GlassArgumentBuilder.argument("player", player())
                                .executes(this::tpNo)
                        )
                )
                .then(GlassArgumentBuilder.argument("player", player())
                        .executes(this::tpa)
                );
    }

    public int tpYes(CommandContext<GlassCommandSource> context) {
        TpaData tpaData = getTpaData(context);

        if (tpaData == null) {
            return invalid(context);
        }

        context.getSource().sendFeedback(Formatting.AQUA + "TPA request accepted.");
        tpaData.playerEntity().sendMessage(Formatting.AQUA + "Your TPA request was accepted.");
        Vec3d pos = context.getSource().getPosition();
        Vector2f rotation = context.getSource().getRotation();
        tpaData.playerEntity().networkHandler.teleport(pos.x, pos.y, pos.z, rotation.x, rotation.y);
        tpaData.tpaRequest().delete();
        return 0;
    }

    public int tpNo(CommandContext<GlassCommandSource> context) {
        TpaData tpaData = getTpaData(context);

        if (tpaData == null) {
            return invalid(context);
        }

        context.getSource().sendFeedback("Denied TPA from " + tpaData.tpaRequest().sourceName());
        tpaData.playerEntity().sendMessage(Formatting.RED + "Your TPA request was denied.");
        tpaData.tpaRequest.delete();
        return 0;
    }

    public int tpa(CommandContext<GlassCommandSource> context) {
        Optional<PlayerEntity> playerEntity = getPlayers(context, "player").getEntities(context.getSource()).stream().findFirst();
        if (playerEntity.isEmpty()) {
            return 0;
        }

        if (playerEntity.get().name.equals(context.getSource().getSourceName())) {
            context.getSource().sendFeedback(Formatting.RED + "You can't send TPAs to yourself.");
            return 0;
        }

        TpaRequest request = new TpaRequest(playerEntity.get().name, context.getSource().getSourceName(), System.currentTimeMillis() / 1000L);
        TPA_REQUESTS_FROM.put(context.getSource().getSourceName(), request);
        TPA_REQUESTS_TO.put(playerEntity.get().name, request);
        context.getSource().sendFeedback("TPA request sent to " + playerEntity.get().name);
        playerEntity.get().sendMessage(Formatting.AQUA + context.getSource().getSourceName() + " has requested a teleport! Type " + Formatting.RED + "/tpa yes" + Formatting.AQUA + " to accept.");
        return 0;
    }

    private TpaData getTpaData(CommandContext<GlassCommandSource> context) {

        Optional<PlayerEntity> playerEntity;

        try {
            playerEntity = getPlayers(context, "player").getEntities(context.getSource()).stream().findFirst();
        } catch (IllegalArgumentException ignored) {
            playerEntity = Optional.empty();
        }

        Optional<TpaRequest> maybeRequest;
        maybeRequest = playerEntity.flatMap(player -> Optional.of(TPA_REQUESTS_FROM.get(player.name))).or(() -> TPA_REQUESTS_TO.get(context.getSource().getSourceName()).stream().reduce(((tpaRequest, tpaRequest2) -> tpaRequest2)));

        if (maybeRequest.isEmpty()) {
            return null;
        }

        TpaRequest tpaRequest = maybeRequest.get();
        if (tpaRequest.requestTime() < ((System.currentTimeMillis() / 1000L) - 15)) {
            return null;
        }

        PlayerEntity player = playerEntity.orElseGet(() -> context.getSource().getPlayerByName(tpaRequest.sourceName));
        if (player == null) {
            return null;
        }

        return new TpaData((ServerPlayerEntity) player, tpaRequest);
    }

    public int invalid(CommandContext<GlassCommandSource> context) {
        context.getSource().sendFeedback("No valid TPA request found.");
        return 0;
    }

    private record TpaData(ServerPlayerEntity playerEntity, TpaRequest tpaRequest) {}

    public record TpaRequest(String targetName, String sourceName, long requestTime) {

        public void delete() {
            TPA_REQUESTS_FROM.remove(sourceName);
            TPA_REQUESTS_TO.remove(targetName, this);
        }
    }
}
