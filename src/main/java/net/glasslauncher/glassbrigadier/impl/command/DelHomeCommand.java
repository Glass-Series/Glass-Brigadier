package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.predicate.HasPermission;
import net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.modificationstation.stationapi.api.util.Formatting;
import org.simpleyaml.configuration.ConfigurationSection;

import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.api.predicate.IsPlayer.isPlayer;

public class DelHomeCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        HasPermission hasPermission = booleanPermission("command.delhome");
        return GlassArgumentBuilder.literal("delhome")
                .requires(source -> isPlayer().test(source) && hasPermission.test(source))
                .then(GlassArgumentBuilder.argument("name", word())
                        .executes(this::delHome)
                );
    }

    public int delHome(CommandContext<GlassCommandSource> context) {
        String name = context.getArgument("name", String.class);
        PlayerStorageFile playerStorage = context.getSource().getStorage();
        ConfigurationSection homes = playerStorage.getNotNullSection("homes");

        if (!homes.contains(name)) {
            context.getSource().sendFeedback(Formatting.RED + "No such home \"" + name + "\".");
            return 0;
        }

        homes.remove(name);
        try {
            playerStorage.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        context.getSource().sendFeedback(GlassBrigadier.systemMessage("Removed home \"" + name + "\"."));
        return 0;
    }
}
