package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.predicate.HasPermission;
import net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.util.math.Vec3d;
import org.simpleyaml.configuration.ConfigurationSection;

import java.io.IOException;
import java.util.ArrayList;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.glasslauncher.glassbrigadier.GlassBrigadier.systemMessage;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.api.predicate.IsPlayer.isPlayer;

public class SetHomeCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        HasPermission hasPermission = booleanPermission("command.sethome");
        return GlassArgumentBuilder.literal("sethome")
                .requires(source -> isPlayer().test(source) && hasPermission.test(source))
                .executes(this::setHome)
                .then(GlassArgumentBuilder.argument("name", word())
                        .executes(this::setHomeNamed)
                );
    }

    public int setHomeNamed(CommandContext<GlassCommandSource> context) {
        setHome(context, context.getArgument("name", String.class));
        return 0;
    }

    public int setHome(CommandContext<GlassCommandSource> context) {
        setHome(context, "home");
        return 0;
    }

    public void setHome(CommandContext<GlassCommandSource> context, String name) {
        PlayerStorageFile playerStorage = context.getSource().getStorage();
        ConfigurationSection homes = playerStorage.getNotNullSection("homes");

        Vec3d position = context.getSource().getPosition();
        homes.set(name, new ArrayList<Double>() {{add(position.x); add(position.y); add(position.z);}});
        playerStorage.set("homes", homes);
        try {
            playerStorage.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        context.getSource().sendFeedback(systemMessage("Set home \"" + name + "\"."));
    }
}
