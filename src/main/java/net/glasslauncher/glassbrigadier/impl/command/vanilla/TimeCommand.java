package net.glasslauncher.glassbrigadier.impl.command.vanilla;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.minecraft.world.World;

import java.util.function.Function;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static net.glasslauncher.glassbrigadier.GlassBrigadier.systemMessage;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;

public class TimeCommand implements CommandProvider {

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("time")
                .requires(booleanPermission("command.time"))
                .then(
                        GlassArgumentBuilder.literal("set")
                                .then(GlassArgumentBuilder.argument("time", longArg(0))
                                        .executes(setTime(context -> getLong(context, "time")))
                                ).then(GlassArgumentBuilder.literal("day")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 0)))
                                ).then(GlassArgumentBuilder.literal("noon")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 6000L)))
                                ).then(GlassArgumentBuilder.literal("night")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 12000L)))
                                ).then(GlassArgumentBuilder.literal("midnight")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 18000L)))
                                )
                )
                .then(
                        GlassArgumentBuilder.literal("get")
                                .executes(context -> {
                                    long time = context.getSource().getWorld().getTime();

                                    long hours = ((time / 1000) + 6) % 24;
                                    long minutes = (time % 1000) * 60 / 1000;

                                    context.getSource().sendFeedback("It is day " + (int) Math.floor((double) time / 24000L) + " at " + hours + ":" + (String.valueOf(minutes).length() == 1 ? "0" + minutes : minutes) + ".");
                                    return 0;
                                })
                )
                .then(
                        GlassArgumentBuilder.literal("add")
                                .then(GlassArgumentBuilder.argument("time", longArg())
                                        .executes(context -> {
                                            World level = (context.getSource()).getWorld();
                                            level.setTime(level.getTime()+getLong(context, "time"));
                                            sendFeedbackAndLog(context.getSource(), systemMessage("Set time to " + level.getTime()));
                                            return 0;
                                        }))
                );
    }

    public Command<GlassCommandSource> setTime(Function<CommandContext<GlassCommandSource>, Long> time) {
        return context -> {
            logWarn("Time was " + context.getSource().getWorld().getTime());
            long timeValue = time.apply(context);
            context.getSource().getWorld().setTime(timeValue);
            sendFeedbackAndLog(context.getSource(), systemMessage("Set time to " + timeValue));
            return 0;
        };
    }

    long nextDayIfPast(long time, long targetTimeOfDay) {
        long remainder = time % 24000;

        if (remainder > targetTimeOfDay) {
            return time + 24000 - remainder + targetTimeOfDay;
        }

        return time - remainder + targetTimeOfDay;
    }
}
