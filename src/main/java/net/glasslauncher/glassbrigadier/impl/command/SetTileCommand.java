package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.argument.tileid.BlockId;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.util.math.Vec3i;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.glasslauncher.glassbrigadier.api.argument.coordinate.CoordinateArgumentType.getCoordinate;
import static net.glasslauncher.glassbrigadier.api.argument.coordinate.CoordinateArgumentType.intCoordinate;
import static net.glasslauncher.glassbrigadier.api.argument.tileid.BlockIdArgumentType.getTileId;
import static net.glasslauncher.glassbrigadier.api.argument.tileid.BlockIdArgumentType.tileId;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;

public class SetTileCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("settile")
                .requires(booleanPermission("command.settile"))
                .then(GlassArgumentBuilder.argument("pos", intCoordinate())
                        .then(GlassArgumentBuilder.argument("id", tileId())
                                .executes(this::placeBlock)
                                .then(GlassArgumentBuilder.argument("meta", integer())
                                        .executes(this::placeBlockWithMeta)
                                )
                        )
                );
    }

    public int placeBlock(CommandContext<GlassCommandSource> context) {
        Vec3i pos = getCoordinate(context, "pos").getVec3i(context.getSource());
        BlockId tile = getTileId(context, "id");
        (context.getSource()).getWorld().setBlock(pos.x, pos.y, pos.z, tile.numericId);
        sendFeedbackAndLog(context.getSource(), "Set block at " + pos.x + " " + pos.y + " " + pos.z + " to " + tile.numericId);
        return 0;
    }

    public int placeBlockWithMeta(CommandContext<GlassCommandSource> context) {
        Vec3i pos = getCoordinate(context, "pos").getVec3i(context.getSource());
        BlockId tile = getTileId(context, "id");
        int meta = getInteger(context, "meta");
        (context.getSource()).getWorld().setBlock(pos.x, pos.y, pos.z, tile.numericId, meta);
        sendFeedbackAndLog(context.getSource(), "Set block at " + pos.x + " " + pos.y + " " + pos.z + " to " + tile.numericId + ":" + meta);
        return 0;
    }
}
