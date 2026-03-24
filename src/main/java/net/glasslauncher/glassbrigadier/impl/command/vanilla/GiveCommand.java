package net.glasslauncher.glassbrigadier.impl.command.vanilla;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelector;
import net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.glasslauncher.glassbrigadier.impl.argument.GlassCommandBuilder;
import net.glasslauncher.glassbrigadier.impl.utils.AMIFormatting;
import net.minecraft.entity.player.PlayerEntity;
import net.modificationstation.stationapi.api.util.Formatting;

import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.glasslauncher.glassbrigadier.api.argument.itemid.ItemIdArgumentType.getItemId;
import static net.glasslauncher.glassbrigadier.api.argument.itemid.ItemIdArgumentType.itemId;
import static net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType.entity;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;

public class GiveCommand implements CommandProvider {

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassCommandBuilder.literal("give", "Gives the specified player the specified item.", "Gives the specified player the specified item.")
                .alias("g")
                .requires(booleanPermission("command.give"))
                .then(GlassArgumentBuilder.argument("player", entity())
                        .then(GlassArgumentBuilder.argument("item", itemId())
                                .executes(this::giveItem)
                                .then(GlassArgumentBuilder.argument("count", integer(1))
                                        .executes(this::giveItemWithCount)
                                        .then(GlassArgumentBuilder.argument("meta", integer())
                                                .executes(this::giveItemWithCountAndMeta)
                                        )
                                )
                        )
                );
    }

    public int giveItem(CommandContext<GlassCommandSource> context) {
        List<PlayerEntity> players = TargetSelectorArgumentType.getPlayers(context, "player").getEntities(context.getSource());
        if (players.isEmpty()) {
            sendFeedbackAndLog(context.getSource(), Formatting.RED + "No player named \"" + context.getArgument("player", TargetSelector.class).getName() + "\" found.");
            return 0;
        }

        players.forEach(playerEntity -> {
            int item = getItemId(context, "item").numericId;
            context.getSource().sendFeedback(Formatting.GRAY + "Giving " + playerEntity.name + " some " + item);
            playerEntity.dropItem(item, 1, 0);

        });
        return 0;

    }

    public int giveItemWithCount(CommandContext<GlassCommandSource> context) {
        List<PlayerEntity> players = TargetSelectorArgumentType.getPlayers(context, "player").getEntities(context.getSource());
        if (players.isEmpty()) {
            sendFeedbackAndLog(context.getSource(), Formatting.RED + "No player named \"" + context.getArgument("player", TargetSelector.class).getName() + "\" found.");
            return 0;
        }

        players.forEach(playerEntity -> {

            int item = getItemId(context, "item").numericId;
            int count = getInteger(context, "count");
            sendFeedbackAndLog(context.getSource(), Formatting.GRAY + "Giving " + playerEntity.name + " " + count + " of " + item);
            playerEntity.dropItem(item, count, 0);
        });

        return 0;
    }

    public int giveItemWithCountAndMeta(CommandContext<GlassCommandSource> context) {
        List<PlayerEntity> players = TargetSelectorArgumentType.getPlayers(context, "player").getEntities(context.getSource());
        if (players.isEmpty()) {
            sendFeedbackAndLog(context.getSource(), Formatting.RED + "No player named \"" + context.getArgument("player", TargetSelector.class).getName() + "\" found.");
            return 0;
        }

        players.forEach(playerEntity -> {
            int item = getItemId(context, "item").numericId;
            int count = getInteger(context, "count");
            int meta = getInteger(context, "meta");
            sendFeedbackAndLog(context.getSource(), Formatting.GRAY + "Giving " + playerEntity.name + " " + count + " of " + item + ":" + meta);
            playerEntity.dropItem(item, count, meta);
        });

        return 0;
    }
}
