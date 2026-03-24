package net.glasslauncher.glassbrigadier;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.event.CommandRegisterEvent;
import net.glasslauncher.glassbrigadier.impl.command.*;
import net.glasslauncher.glassbrigadier.impl.command.server.ExecuteVanillaCommand;
import net.glasslauncher.glassbrigadier.impl.command.server.PermissionsCommand;
import net.glasslauncher.glassbrigadier.impl.command.server.RoleCommand;
import net.glasslauncher.glassbrigadier.impl.command.server.TpaCommand;
import net.glasslauncher.glassbrigadier.impl.command.vanilla.*;
import net.glasslauncher.glassbrigadier.impl.command.vanilla.server.*;
import net.glasslauncher.glassbrigadier.impl.network.GlassBrigadierAutocompleteRequestPacket;
import net.glasslauncher.glassbrigadier.impl.network.GlassBrigadierAutocompleteResponsePacket;
import net.glasslauncher.glassbrigadier.impl.network.GlassBrigadierPermissionsExportPacket;
import net.glasslauncher.mods.gcapi3.api.ConfigRoot;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.network.packet.PacketRegisterEvent;
import net.modificationstation.stationapi.api.registry.PacketTypeRegistry;
import net.modificationstation.stationapi.api.registry.Registry;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.glasslauncher.glassbrigadier.impl.utils.AMIFormatting.BOLD;
import static net.modificationstation.stationapi.api.util.Formatting.AQUA;
import static net.modificationstation.stationapi.api.util.Formatting.GOLD;

public class GlassBrigadier {
    @ConfigRoot(value = "config", visibleName = "Config", nameKey = "config.glassbrigadier.config.name")
    public static final GlassBrigadierConfig CONFIG = new GlassBrigadierConfig();
    public static final List<String> ALL_PERMISSIONS = new ArrayList<>();

    public static final List<String> PREVIOUS_MESSAGES = new ArrayList<>();
    @SuppressWarnings("UnstableApiUsage")
    public static final Namespace NAMESPACE = Namespace.resolve();
    public static final Logger LOGGER = NAMESPACE.getLogger("Glass Brigadier");
    public static final boolean AMI_LOADED = FabricLoader.getInstance().isModLoaded("alwaysmoreitems");

    public static final CommandDispatcher<GlassCommandSource> DISPATCHER = new CommandDispatcher<>();

    public static File getConfigFile(String... path) {
        return new File("config/" + NAMESPACE, String.join("/", path));
    }

    public static int currentCompletion = 0;
    public static boolean isTabbed = false;

    @EventListener(phase = CommandRegisterEvent.INTERNAL_PHASE)
    public void internalInit(CommandRegisterEvent event) {
        // Register permission nodes here
    }

    @EventListener(phase = CommandRegisterEvent.VANILLA_PHASE)
    public void vanillaInit(CommandRegisterEvent event) {
        event.register(new HelpCommand());
        event.register(new MeCommand());
        event.register(new GiveCommand());
        event.register(new TimeCommand());
        event.register(new TeleportCommand());
        event.register(new SayCommand());
    }

    @Environment(EnvType.SERVER)
    @EventListener(phase = CommandRegisterEvent.VANILLA_PHASE)
    public void vanillaServerInit(CommandRegisterEvent event) {
        event.register(new MsgCommand());
        event.register(new OpCommand());
        event.register(new DeopCommand());
        event.register(new BanCommand());
        event.register(new BanIpCommand());
        event.register(new PardonCommand());
        event.register(new PardonIpCommand());
        event.register(new SaveOffCommand());
        event.register(new SaveOnCommand());
        event.register(new SaveAllCommand());
        event.register(new StopCommand());
        event.register(new ExecuteVanillaCommand());
    }

    @EventListener
    public void customInit(CommandRegisterEvent event) {
        event.register(new SetTileCommand());
        event.register(new SummonCommand());
        event.register(new SetHomeCommand());
        event.register(new DelHomeCommand());
        event.register(new HomeCommand());
        event.register(new HomesCommand());
        event.register(new SetWarpCommand());
        event.register(new DelWarpCommand());
        event.register(new WarpCommand());
        event.register(new WarpsCommand());
    }

    @Environment(EnvType.SERVER)
    @EventListener
    public void customServerInit(CommandRegisterEvent event) {
        event.register(new PermissionsCommand());
        event.register(new RoleCommand());
        event.register(new TpaCommand());
    }

    @EventListener
    public void onInitialize(PacketRegisterEvent event) {
        Registry.register(PacketTypeRegistry.INSTANCE, GlassBrigadier.NAMESPACE.id("autocomplete_request"), GlassBrigadierAutocompleteRequestPacket.TYPE);
        Registry.register(PacketTypeRegistry.INSTANCE, GlassBrigadier.NAMESPACE.id("autocomplete_response"), GlassBrigadierAutocompleteResponsePacket.TYPE);
        Registry.register(PacketTypeRegistry.INSTANCE, GlassBrigadier.NAMESPACE.id("permissions_export"), GlassBrigadierPermissionsExportPacket.TYPE);
    }

    public static String systemCursorColor() {
        return AQUA.toString() + BOLD;
    }

    public static String systemMessageColor() {
        return GOLD.toString();
    }

    public static String systemMessagePrefix() {
        return systemCursorColor() + ">" + systemMessageColor();
    }

    public static String systemMessage(String message) {
        return systemMessagePrefix() + " " + message;
    }

    public static String systemBulletPointPrefix() {
        return systemCursorColor() + "*" + systemMessageColor();
    }

    public static String systemBulletPoint(String message) {
        return systemBulletPointPrefix() + " " + message;
    }
}
