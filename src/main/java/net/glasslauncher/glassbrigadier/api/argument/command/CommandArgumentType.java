package net.glasslauncher.glassbrigadier.api.argument.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandArgumentType implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("help", "time", "give");

    private static final SimpleCommandExceptionType NOT_VALID_COMMAND = new SimpleCommandExceptionType(new LiteralMessage("Invalid Command"));

    private static Set<String> getValidValues(GlassCommandSource source) {
        try {

            return GlassBrigadier.DISPATCHER.getRoot().getChildren().stream()
                    .filter(e -> e.canUse(source) && e instanceof LiteralCommandNode<GlassCommandSource>)
                    .map(e -> ((LiteralCommandNode<GlassCommandSource>) e).getLiteral())
                    .collect(Collectors.toSet());
        }
        catch (Exception e) {
            return Set.of();
        }
    }

    public static CommandArgumentType commandArgument() {
        return new CommandArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String id = reader.readString();
        //noinspection deprecation
        if (!getValidValues((GlassCommandSource) FabricLoader.getInstance().getGameInstance()).contains(id)) {
            reader.setCursor(cursor);
            throw NOT_VALID_COMMAND.createWithContext(reader);
        }
        return id;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String validValue : getValidValues((GlassCommandSource) context.getSource())) {
            if (validValue.startsWith(builder.getRemaining()) || validValue.substring(validValue.indexOf(':')+1, validValue.length()-1).startsWith(builder.getRemaining()))
                builder.suggest(validValue);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
