package net.glasslauncher.glassbrigadier.mixin.server;

import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.event.CommandRegisterEvent;
import net.glasslauncher.glassbrigadier.api.event.GlassBrigadierDefaultsEvent;
import net.glasslauncher.glassbrigadier.impl.permission.RoleManagerImpl;
import net.glasslauncher.glassbrigadier.impl.permission.UserPermissionManagerImpl;
import net.minecraft.server.MinecraftServer;
import net.modificationstation.stationapi.api.StationAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;loadWorld(Lnet/minecraft/world/storage/WorldStorageSource;Ljava/lang/String;J)V", shift = At.Shift.AFTER))
    void initCommands(CallbackInfoReturnable<Boolean> cir) {
        GlassBrigadier.LOGGER.info("Initializing commands...");
        StationAPI.EVENT_BUS.post(CommandRegisterEvent.builder().commandDispatcher(GlassBrigadier.DISPATCHER).build());
        StationAPI.EVENT_BUS.post(GlassBrigadierDefaultsEvent.builder().build());
        GlassBrigadier.LOGGER.info("Registered {} commands.", GlassBrigadier.DISPATCHER.getRoot().getChildren().size());
        RoleManagerImpl.setupRoleManager();
        UserPermissionManagerImpl.setupPermissionManager();
    }
}
