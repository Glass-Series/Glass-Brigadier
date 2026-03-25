package net.glasslauncher.glassbrigadier.impl.command.vanilla;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.argument.command.CommandArgumentType;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.DescriptiveLiteralCommandNode;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.modificationstation.stationapi.api.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.glasslauncher.glassbrigadier.GlassBrigadier.systemMessageColor;
import static net.modificationstation.stationapi.api.util.Formatting.RED;

public class HelpCommand implements CommandProvider {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(() -> Formatting.RED + "Unknown command or insufficient permissions");

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassArgumentBuilder.literal("help").executes(this::showHelpPage)
                .then(GlassArgumentBuilder.argument("page", integer(0))
                        .executes(this::showHelpPage)
                )
                .then(GlassArgumentBuilder.argument("command", CommandArgumentType.commandArgument())
                        .executes(this::showCommandHelp)
                );
    }

    public int showHelpPage(CommandContext<GlassCommandSource> context) {
        int page = 1;
        try {
            page = context.getArgument("page", Integer.class);
        } catch (IllegalArgumentException ignored) {}
        Map<CommandNode<GlassCommandSource>, String> commands = GlassBrigadier.DISPATCHER.getSmartUsage(GlassBrigadier.DISPATCHER.getRoot(), context.getSource());
        int maxPages = (int) Math.ceil(commands.size() / 9d);

        if (page > maxPages) {
            context.getSource().sendFeedback(RED + "There are only " + systemMessageColor() + maxPages + RED + " pages.");
            return 0;
        }

        CommandNode<?>[] commandKeys = commands.keySet().toArray(CommandNode[]::new);

        context.getSource().sendFeedback(GlassBrigadier.systemMessage("Showing help page " + RED + page + systemMessageColor() + " of " + RED + maxPages + systemMessageColor() + "."));
        int startIndex = (page - 1) * 9;
        for (int i = startIndex; i < commands.size() && i < startIndex + 9; ++i) {
            CommandNode<?> commandNode = commandKeys[i];
            String usageText = commands.get(commandNode);

            if (commandNode instanceof DescriptiveLiteralCommandNode<?> descriptiveLiteralCommandNode) {
                context.getSource().sendFeedback(systemMessageColor() + "/" + usageText + Formatting.GRAY + ": " + descriptiveLiteralCommandNode.getShortDescription());
                continue;
            }

            context.getSource().sendFeedback(systemMessageColor() + "/" + usageText);
        }

        return commands.size();
    }

    public int showCommandHelp(CommandContext<GlassCommandSource> context) throws CommandSyntaxException {
        String command = StringArgumentType.getString(context, "command");
        ParseResults<GlassCommandSource> parseResults = GlassBrigadier.DISPATCHER.parse(command, context.getSource());
        List<ParsedCommandNode<GlassCommandSource>> commandNodes = parseResults.getContext().getNodes();
        if (!parseResults.getExceptions().isEmpty()) {
            //noinspection OptionalGetWithoutIsPresent Intellij needs to update its linter.
            throw parseResults.getExceptions().values().stream().findFirst().get();
        }
        else if (commandNodes.isEmpty()) {
            throw FAILED_EXCEPTION.create();
        }
        else {
            CommandNode<GlassCommandSource> commandNode = Iterables.getLast(parseResults.getContext().getNodes()).getNode();

            Map<CommandNode<GlassCommandSource>, String> map = GlassBrigadier.DISPATCHER.getSmartUsage(commandNode, context.getSource());

            context.getSource().sendFeedback(GlassBrigadier.systemMessage(" Showing usage for " + RED + command + systemMessageColor() + ":"));
            if (commandNode instanceof DescriptiveLiteralCommandNode<?> literalArgumentBuilder) {
                for (String helpLine : getHelpLines(literalArgumentBuilder.getDescription())) {
                    context.getSource().sendFeedback(helpLine);
                }
            }
            for (String string : map.values()) {
                context.getSource().sendFeedback(GlassBrigadier.systemBulletPoint("/" + parseResults.getReader().getString() + " " + string));
            }

            return map.size();
        }
    }

    public List<String> getHelpLines(String linesString) {
        if (linesString == null) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();

        for (String line : linesString.split("\n")) {
            if (line.length() > 110) {
                lines.addAll(List.of(addNewlines(line, 110).split("\n")));
                continue;
            }
            lines.add(line);
        }
        return lines;
    }

    public static String addNewlines(String input, int maxLineLength) {
        StringTokenizer wordReader = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (wordReader.hasMoreTokens()) {
            String word = wordReader.nextToken();
            if (lineLen != 0) {
                output.append(" ");
                ++lineLen;
            }
            if (lineLen + word.length() > maxLineLength) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word);
            lineLen += word.length();
        }
        return output.toString();
    }
}
