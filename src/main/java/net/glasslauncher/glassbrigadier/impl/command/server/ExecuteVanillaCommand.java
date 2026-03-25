package net.glasslauncher.glassbrigadier.impl.command.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassCommandBuilder;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder.argument;

public class ExecuteVanillaCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassCommandBuilder.literal("executevanilla", "Executes a command that isn't handled by Glass Brigadier.")
                .alias("ev")
                .requires(booleanPermission("command.executevanilla"))
                .then(
                        argument("command", greedyString()).
                        executes(src -> {
                            // This doesn't actually need to do anything.
                            return 0;
                        })
                );
    }
}
