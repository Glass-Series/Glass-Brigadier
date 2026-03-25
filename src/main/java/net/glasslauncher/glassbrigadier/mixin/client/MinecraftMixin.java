package net.glasslauncher.glassbrigadier.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.event.CommandRegisterEvent;
import net.glasslauncher.glassbrigadier.api.event.GlassBrigadierDefaultsEvent;
import net.glasslauncher.glassbrigadier.api.permission.PermissionNodeInstance;
import net.glasslauncher.glassbrigadier.api.storage.player.PlayerStorageFile;
import net.glasslauncher.glassbrigadier.api.storage.world.WorldModStorageFile;
import net.glasslauncher.glassbrigadier.impl.permission.RoleChain;
import net.glasslauncher.glassbrigadier.impl.permission.RoleManagerImpl;
import net.glasslauncher.glassbrigadier.impl.permission.UserPermissionManagerImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.StationAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow public World world;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWorldRemote()Z", ordinal = 0))
    boolean allowChatScreenInSP(Minecraft instance, Operation<Boolean> original) {
        return GlassBrigadier.CONFIG.singlePlayerChat;
    }

    @Inject(method = "init", at = @At("RETURN"))
    void registerCommands(CallbackInfo ci) {
        GlassBrigadier.LOGGER.info("Initializing commands...");
        StationAPI.EVENT_BUS.post(CommandRegisterEvent.builder().commandDispatcher(GlassBrigadier.DISPATCHER).build());
        StationAPI.EVENT_BUS.post(GlassBrigadierDefaultsEvent.builder().build());
        GlassBrigadier.LOGGER.info("Registered {} commands.", GlassBrigadier.DISPATCHER.getRoot().getChildren().size());
    }

    @Inject(method = "setWorld(Lnet/minecraft/world/World;Ljava/lang/String;Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("HEAD"))
    void resetPlayerDataCache(World message, String player, PlayerEntity par3, CallbackInfo ci) {
        if (world == null) {
            PlayerStorageFile.invalidateAll();
            WorldModStorageFile.invalidateAll();
            PermissionNodeInstance.invalidateAll();
            RoleChain.invalidateAll();
        }
    }


    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setWorld(Lnet/minecraft/world/World;Ljava/lang/String;)V", shift = At.Shift.AFTER))
    void loadRoles(String name, String seed, long par3, CallbackInfo ci) {
        RoleManagerImpl.setupRoleManager();
        UserPermissionManagerImpl.setupPermissionManager();
    }
}
