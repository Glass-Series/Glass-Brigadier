package net.glasslauncher.glassbrigadier.mixin.client.vanillajank;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.glasslauncher.glassbrigadier.impl.client.mixinhooks.SplittingTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public class FixChatLineBreaksMixin {

    @Shadow private List messages;

    @WrapOperation(method = "addChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I"))
    int byeByeOldCode(TextRenderer instance, String s, Operation<Integer> original) {
        return 0;
    }

    @WrapOperation(method = "addChatMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
    void byeByeOldCode2(List<?> instance, int i, Object e, Operation<Void> original) {}

    @Inject(method = "addChatMessage", at = @At("HEAD"))
    void fineIllDoItMyself(String message, CallbackInfo ci) {
        for (String line : ((SplittingTextRenderer) Minecraft.INSTANCE.textRenderer).glass_Brigadier$split(message, 320)) {
            //noinspection unchecked
            messages.add(0, new ChatHudLine(line));
        }
    }
}
