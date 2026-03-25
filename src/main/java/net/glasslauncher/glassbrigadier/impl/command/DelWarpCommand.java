package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.storage.world.WorldModStorageFile;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.modificationstation.stationapi.api.util.Formatting;
import org.simpleyaml.configuration.ConfigurationSection;

import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;

public class DelWarpCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("delwarp")
                .requires(booleanPermission("command.delwarp"))
                .then(GlassArgumentBuilder.argument("name", word())
                        .executes(this::delWarp)
                );
    }

    public int delWarp(CommandContext<GlassCommandSource> context) {
        String name = context.getArgument("name", String.class);

        WorldModStorageFile serverStorage = WorldModStorageFile.of(GlassBrigadier.NAMESPACE.id("warps"));
        ConfigurationSection warps = serverStorage.getNotNullSection("warps");

        if (!warps.contains(name)) {
            context.getSource().sendFeedback(Formatting.RED + "No warp named \"" + name + "\".");
            return 0;
        }

        warps.remove(name);

        try {
            serverStorage.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        context.getSource().sendFeedbackAndLog(GlassBrigadier.systemMessage("Deleted warp \"" + name + "\"."));
        return 0;
    }
}
