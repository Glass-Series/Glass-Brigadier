package net.glasslauncher.glassbrigadier.impl.network;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.SneakyThrows;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkHandler;
import net.minecraft.network.packet.Packet;
import net.modificationstation.stationapi.api.network.packet.ManagedPacket;
import net.modificationstation.stationapi.api.network.packet.PacketHelper;
import net.modificationstation.stationapi.api.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GlassBrigadierAutocompleteRequestPacket extends Packet implements ManagedPacket<GlassBrigadierAutocompleteRequestPacket> {
    public static final PacketType<GlassBrigadierAutocompleteRequestPacket> TYPE = PacketType.builder(false, true, GlassBrigadierAutocompleteRequestPacket::new).build();

    private String incompleteCommand;
    private int cursorPos;

    public GlassBrigadierAutocompleteRequestPacket(String incompleteCommand, int cursorPos) {
        this.incompleteCommand = incompleteCommand;
        this.cursorPos = cursorPos;
    }

    private GlassBrigadierAutocompleteRequestPacket() {
    }

    private List<String> applySuggestions(String input, List<Suggestion> suggestions) {
        return suggestions.stream().map(e -> e.apply(input)).toList();
    }

    @Override
    public @NotNull PacketType<GlassBrigadierAutocompleteRequestPacket> getType() {
        return TYPE;
    }

    @Override
    @SneakyThrows
    public void read(DataInputStream stream) {
            incompleteCommand = stream.readUTF();
            cursorPos = stream.readInt();
    }

    @Override
    @SneakyThrows
    public void write(DataOutputStream stream) {
        stream.writeUTF(incompleteCommand);
        stream.writeInt(cursorPos);
    }

    @Override
    public void apply(NetworkHandler networkHandler) {
        GlassCommandSource commandSource;

        if (networkHandler instanceof GlassCommandSource glassCommandSource) {
            commandSource = glassCommandSource;
        }
        else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            commandSource = (GlassCommandSource) Minecraft.INSTANCE;
        }
        else {
            return;
        }

        ParseResults<GlassCommandSource> parseResults = GlassBrigadier.DISPATCHER.parse(incompleteCommand, commandSource);
        Suggestions suggestions;
        try {
            suggestions = GlassBrigadier.DISPATCHER.getCompletionSuggestions(parseResults, cursorPos).get();
        } catch (InterruptedException | ExecutionException e) {
            GlassBrigadier.LOGGER.error(e);
            return;
        }
        if (!suggestions.getList().isEmpty()) {
            PacketHelper.sendTo(commandSource.getPlayer(), new GlassBrigadierAutocompleteResponsePacket(applySuggestions(incompleteCommand, suggestions.getList())));
        }
    }

    @Override
    public int size() {
        return 0;
    }
}
