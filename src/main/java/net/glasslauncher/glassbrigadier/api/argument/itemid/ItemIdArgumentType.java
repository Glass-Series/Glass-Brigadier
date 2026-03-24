package net.glasslauncher.glassbrigadier.api.argument.itemid;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.glasslauncher.glassbrigadier.impl.utils.StringReaderUtils;
import net.glasslauncher.mods.gcapi3.api.CharacterUtils;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ItemIdArgumentType implements ArgumentType<ItemId> {

    private static final Collection<String> EXAMPLES = Arrays.asList("0", "2", "32");

    private static final SimpleCommandExceptionType NOT_VALID_ID = new SimpleCommandExceptionType(new LiteralMessage("Invalid Item ID"));

    private static Set<String> validValues;
    private static Map<Integer, String> intIdentifier2Identifier;

    private static Set<String> getValidValues() {
        if (validValues != null)
            return validValues;

        Set<Identifier> ids = ItemRegistry.INSTANCE.getIds();
        validValues = ids.stream().map(Identifier::toString).collect(Collectors.toSet());

        return validValues;
    }

    private static Map<Integer, String> getIntIdentifier2Identifier() {
        if (intIdentifier2Identifier != null)
            return intIdentifier2Identifier;
        Set<Integer> intIdentifiers = ItemRegistry.INSTANCE.stream().map(item -> item.id).collect(Collectors.toSet());
        intIdentifier2Identifier = intIdentifiers.stream().collect(Collectors.toMap(integer -> integer, integer -> ItemRegistry.INSTANCE.getId(integer).map(Identifier::toString).orElse("")));
        return intIdentifier2Identifier;
    }

    public static ItemIdArgumentType itemId() {
        return new ItemIdArgumentType();
    }

    public static ItemId getItemId(final CommandContext<?> context, final String name) {
        return context.getArgument(name, ItemId.class);
    }

    @Override
    public ItemId parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String id = StringReaderUtils.readId(reader);

        if (CharacterUtils.isInteger(id)) {
            return new ItemId(Integer.parseInt(id));
        }
        if (ItemRegistry.INSTANCE.get(Identifier.of(id)) != null) {
            return new ItemId(id);
        }

        reader.setCursor(cursor);
        throw NOT_VALID_ID.createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String validValue : getValidValues()) {
            if (validValue.startsWith(builder.getRemaining()) || validValue.substring(validValue.indexOf(':')+1, validValue.length()-1).startsWith(builder.getRemaining()))
                builder.suggest(validValue);
        }
        getIntIdentifier2Identifier().forEach((integer, id) -> {
            if (integer.toString().startsWith(builder.getRemaining()))
                builder.suggest(id); //Only ever suggest string ids
        });
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
