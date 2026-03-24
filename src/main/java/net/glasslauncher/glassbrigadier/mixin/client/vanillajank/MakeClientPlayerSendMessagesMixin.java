package net.glasslauncher.glassbrigadier.mixin.client.vanillajank;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MakeClientPlayerSendMessagesMixin extends PlayerEntity {

    @Shadow protected Minecraft minecraft;

    public MakeClientPlayerSendMessagesMixin(World world) {
        super(world);
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    void test(String message, CallbackInfo ci) {
        if (world.isRemote) {
            return;
        }
        if (GlassBrigadier.CONFIG.singlePlayerCommands && message.startsWith("/")) {
            try {
                GlassBrigadier.DISPATCHER.execute(message.substring(1), (GlassCommandSource) minecraft);
            } catch (CommandSyntaxException e) {
                minecraft.inGameHud.addChatMessage(e.getMessage());
            }
            return;
        }
        minecraft.inGameHud.addChatMessage("<" + name + "> " + message.strip());
    }

}
