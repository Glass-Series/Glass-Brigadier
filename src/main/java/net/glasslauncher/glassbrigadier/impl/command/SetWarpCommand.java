package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.predicate.HasPermission;
import net.glasslauncher.glassbrigadier.api.storage.world.WorldModStorageFile;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.util.math.Vec3d;
import org.simpleyaml.configuration.ConfigurationSection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.glasslauncher.glassbrigadier.GlassBrigadier.systemMessage;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.api.predicate.IsPlayer.isPlayer;

public class SetWarpCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        HasPermission hasPermission = booleanPermission("command.setwarp");
        return GlassArgumentBuilder.literal("setwarp")
                .requires(source -> isPlayer().test(source) && hasPermission.test(source))
                .then(GlassArgumentBuilder.argument("name", word())
                        .executes(this::setWarp)
                        .then(GlassArgumentBuilder.argument("description", greedyString())
                                .executes(this::setWarpWithDesc)
                        )
                );
    }

    public int setWarp(CommandContext<GlassCommandSource> context) {
        return setWarp(context, null);
    }

    public int setWarpWithDesc(CommandContext<GlassCommandSource> context) {
        return setWarp(context, context.getArgument("description", String.class));
    }

    public int setWarp(CommandContext<GlassCommandSource> context, String description) {
        String name = context.getArgument("name", String.class);

        WorldModStorageFile serverStorage = WorldModStorageFile.of(GlassBrigadier.NAMESPACE.id("warps"));
        ConfigurationSection warps = serverStorage.getNotNullSection("warps");

        Vec3d position = context.getSource().getPosition();

        ConfigurationSection warp = warps.createSection(name, new HashMap<>());
        warp.set("location", new ArrayList<Double>() {{add(position.x); add(position.y); add(position.z);}});
        warp.set("description", description);

        try {
            serverStorage.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sendFeedbackAndLog(context.getSource(), systemMessage("Set warp \"" + name + "\"."));
        return 0;
    }
}
