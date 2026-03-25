package net.glasslauncher.glassbrigadier.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;

import java.util.function.Function;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static net.glasslauncher.glassbrigadier.GlassBrigadier.systemMessage;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;

public class WeatherCommand implements CommandProvider {

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("weather")
                .requires(booleanPermission("command.weather"))
                .then(
                        GlassArgumentBuilder.literal("set")
                                .then(GlassArgumentBuilder.literal("clear")
                                        .executes(source -> {
                                            WorldProperties worldProperties = source.getSource().getWorld().getProperties();
                                            worldProperties.setRaining(false);
                                            worldProperties.setRainTime(source.getSource().getWorld().random.nextInt(168000) + 12000); // Required or rain starts again ~60s later
                                            return 0;
                                        })
                                ).then(GlassArgumentBuilder.literal("rain")
                                        .executes(source -> {
                                            WorldProperties worldProperties = source.getSource().getWorld().getProperties();
                                            worldProperties.setRaining(true);
                                            worldProperties.setRainTime(source.getSource().getWorld().random.nextInt(12000) + 12000); // Also required, or rain will dissipate very quickly.
                                            return 0;
                                        })
                                ).then(GlassArgumentBuilder.literal("thunderstorm")
                                        .executes(source -> {
                                            WorldProperties worldProperties = source.getSource().getWorld().getProperties();
                                            worldProperties.setRaining(true);
                                            worldProperties.setThundering(true);
                                            int thunderTime = source.getSource().getWorld().random.nextInt(12000) + 12000;
                                            worldProperties.setRainTime(thunderTime); // Also required, or rain and thunder will dissipate very quickly.
                                            worldProperties.setThunderTime(thunderTime);
                                            return 0;
                                        })
                                )
                )
                .then(
                        GlassArgumentBuilder.literal("get")
                                .executes(context -> {
                                    World world = context.getSource().getWorld();
                                    String output = "clear";

                                    if (world.isThundering()) {
                                        output = "thundering";
                                    }
                                    else if (world.isRaining()) {
                                        output = "raining";
                                    }

                                    context.getSource().sendFeedback(systemMessage("It is currently " + output + "."));
                                    return 0;
                                })
                );
    }
}
