package net.glasslauncher.glassbrigadier.mixin.server;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.ServerCommandHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommandHandler.class)
public class ServerCommandHandlerMixin {

    @ModifyVariable(method = "executeCommand", at = @At(value = "STORE", target = "Lnet/minecraft/server/command/Command;commandAndArgs:Ljava/lang/String;"))
    String sanitizeExecVanilla(String value) {
        if (value.startsWith("executevanilla ")) {
            return value.substring(15);
        }
        else if (value.startsWith("ev ")) {
            return value.substring(3);
        }
        return value;
    }

    @Inject(method = "executeCommand", at = @At(value = "HEAD"), cancellable = true)
    void hijackCommands(Command command, CallbackInfo ci) {
        if (command.commandAndArgs.startsWith("executevanilla ") || command.commandAndArgs.startsWith("ev ")) {
            return;
        }
        try {
            GlassBrigadier.DISPATCHER.execute(command.commandAndArgs, (GlassCommandSource) command.output);
        } catch (CommandSyntaxException e) {
            command.output.sendMessage(e.getMessage());
        }
        ci.cancel();
    }

}
