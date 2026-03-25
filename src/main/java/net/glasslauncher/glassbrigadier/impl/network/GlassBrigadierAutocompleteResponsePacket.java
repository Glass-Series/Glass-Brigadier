package net.glasslauncher.glassbrigadier.impl.network;

import lombok.SneakyThrows;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.impl.client.mixinhooks.ChatScreenHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.NetworkHandler;
import net.minecraft.network.packet.Packet;
import net.modificationstation.stationapi.api.network.packet.ManagedPacket;
import net.modificationstation.stationapi.api.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlassBrigadierAutocompleteResponsePacket extends Packet implements ManagedPacket<GlassBrigadierAutocompleteResponsePacket> {
    public static final PacketType<GlassBrigadierAutocompleteResponsePacket> TYPE = PacketType.builder(true, false, GlassBrigadierAutocompleteResponsePacket::new).build();

    private List<String> completions;

    private GlassBrigadierAutocompleteResponsePacket() {
    }

    GlassBrigadierAutocompleteResponsePacket(List<String> completions) {
        this.completions = completions;
    }

    private List<String> bytesToStrings(byte[] bytes) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0b00000000)
                break;

            final int stringStart = i;
            while (i < bytes.length && bytes[i] != 0b00000000) {
                i++;
            }
            final int stringEnd = i;
            strings.add(new String(Arrays.copyOfRange(bytes, stringStart, stringEnd), StandardCharsets.UTF_8));
        }
        return strings;
    }

    private byte[] stringsToBytes(List<String> strings) {
        List<Byte> bytesList = new ArrayList<>();
        for (String string : strings) {
            int length = string.length();
            boolean tooLong = length > 100;
            if (!tooLong) {
                for (byte bt : string.getBytes(StandardCharsets.UTF_8)) {
                    bytesList.add(bt);
                }
                bytesList.add((byte) 0b00000000);
            }
        }
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }

    @Override
    public @NotNull PacketType<GlassBrigadierAutocompleteResponsePacket> getType() {
        return TYPE;
    }

    @Override
    @SneakyThrows
    public void read(DataInputStream stream) {
        completions = bytesToStrings(stream.readUTF().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @SneakyThrows
    public void write(DataOutputStream stream) {
        stream.writeUTF(new String(stringsToBytes(completions), StandardCharsets.UTF_8));
    }

    @Override
    public void apply(NetworkHandler networkHandler) {
        GlassBrigadier.currentCompletion = 0;
        Screen screen = Minecraft.INSTANCE.currentScreen;
        if (screen instanceof ChatScreen chatScreen) {
            if (!completions.isEmpty()) {
                ((ChatScreenHooks) chatScreen).glass_Brigadier$setCompletions(completions);
            }
        }
    }

    @Override
    public int size() {
        return 0;
    }
}
