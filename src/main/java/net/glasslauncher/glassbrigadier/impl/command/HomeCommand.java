package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.util.Formatting;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;
import static net.glasslauncher.glassbrigadier.api.predicate.IsPlayer.isPlayer;

public class HomeCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("home")
                .requires(source -> isPlayer().test(source) && booleanPermission("command.home").test(source))
                .executes(this::home)
                .then(GlassArgumentBuilder.argument("name", word())
                        .executes(this::homeNamed)
                );
    }

    public int homeNamed(CommandContext<GlassCommandSource> context) {
        home(context, context.getArgument("name", String.class));
        return 0;
    }

    public int home(CommandContext<GlassCommandSource> context) {
        home(context, "home");
        return 0;
    }

    public void home(CommandContext<GlassCommandSource> context, String name) {
        PlayerStorageFile playerStorage = context.getSource().getStorage();
        ConfigurationSection homes = playerStorage.getNotNullSection("homes");
        List<Double> homeLoc = homes.getDoubleList(name);

        if (homeLoc == null || homeLoc.isEmpty()) {
            context.getSource().sendFeedback(Formatting.RED + "No home named \"" + name + "\".");
            return;
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && context.getSource().getPlayer() instanceof ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.networkHandler.teleport(homeLoc.get(0), homeLoc.get(1), homeLoc.get(2), serverPlayerEntity.yaw, serverPlayerEntity.pitch);
        }
        else {
            PlayerEntity player = context.getSource().getPlayer();
            player.setPositionAndAnglesKeepPrevAngles(homeLoc.get(0), homeLoc.get(1), homeLoc.get(2), player.yaw, player.pitch);
        }

        context.getSource().sendFeedback(GlassBrigadier.systemMessage("Teleported to home \"" + name + "\"."));
    }
}
