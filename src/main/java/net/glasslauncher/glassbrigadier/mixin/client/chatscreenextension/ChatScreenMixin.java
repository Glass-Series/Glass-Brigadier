package net.glasslauncher.glassbrigadier.mixin.client.chatscreenextension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.impl.client.mixinhooks.ChatScreenHooks;
import net.glasslauncher.glassbrigadier.impl.network.GlassBrigadierAutocompleteRequestPacket;
import net.glasslauncher.mods.gcapi3.api.CharacterUtils;
import net.glasslauncher.mods.gcapi3.impl.screen.widget.ExtensibleTextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.modificationstation.stationapi.api.network.packet.PacketHelper;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static net.glasslauncher.glassbrigadier.GlassBrigadier.currentCompletion;
import static net.glasslauncher.glassbrigadier.GlassBrigadier.isTabbed;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen implements ChatScreenHooks {
    @Shadow public String text;
    @Unique
    private static final int YELLOW = CharacterUtils.getIntFromColour(Color.YELLOW);
    @Unique
    private static final int BLACK = CharacterUtils.getIntFromColour(Color.BLACK);
    @Unique
    private static final int WHITE = CharacterUtils.getIntFromColour(Color.WHITE);

    @Unique
    private static final Pattern COLOR_PATTERN = Pattern.compile("§.");

    @Unique
    private int currentMessageIndex = -1;
    @Unique
    private String currentMessage = "";

    @Unique
    private List<String> completions;

    @Unique
    private ExtensibleTextFieldWidget textFieldWidget;

    @Unique
    private int lockedSuggestionPosition = 0;

    @Override
    public void glass_Essentials$setCompletions(List<String> completions) {
        this.completions = completions;
    }

    @Unique
    private String getCurrentCompletionText() {
        return COLOR_PATTERN.matcher(completions.get(currentCompletion % completions.size())).replaceAll("");
    }

    @Inject(method = "keyPressed(CI)V", at = @At("HEAD"), cancellable = true)
    void checkKeys(char c, int i, CallbackInfo ci) {
        String autoCompleteMessage = textFieldWidget.getText();
        if (!autoCompleteMessage.isEmpty() && autoCompleteMessage.charAt(0) == '/') {
            autoCompleteMessage = autoCompleteMessage.substring(1);
        }
        switch (i) {

            case Keyboard.KEY_TAB:
                if (completions != null && !completions.isEmpty()) {
                    if (isTabbed) {
                        currentCompletion++;
                    }
                    if (lockedSuggestionPosition == 0)
                        lockedSuggestionPosition = textRenderer.getWidth("> " + textFieldWidget.getText()) + 4;
                    textFieldWidget.setText("/" + getCurrentCompletionText());
                    isTabbed = true;
                    break;
                }
                else {
                    invalidateSuggestions();
                    PacketHelper.send(new GlassBrigadierAutocompleteRequestPacket(autoCompleteMessage, autoCompleteMessage.length()));
                }
                break;

            case Keyboard.KEY_UP:
                if (completions != null && !completions.isEmpty()) {
                    currentCompletion++;
                    if (lockedSuggestionPosition == 0)
                        lockedSuggestionPosition = textRenderer.getWidth("> " + textFieldWidget.getText()) + 4;
                    textFieldWidget.setText("/" + completions.get(currentCompletion % completions.size()));
                    break;
                }
                if (GlassBrigadier.PREVIOUS_MESSAGES.size() > currentMessageIndex+1) {
                    if (currentMessageIndex == -1)
                        currentMessage = textFieldWidget.getText();
                    textFieldWidget.setText(GlassBrigadier.PREVIOUS_MESSAGES.get(++currentMessageIndex));
                    invalidateSuggestions();
                }
                break;

            case Keyboard.KEY_DOWN:
                if (completions != null && !completions.isEmpty()) {
                    currentCompletion--;
                    if (currentCompletion < 0) {
                        currentCompletion = completions.size() - 1;
                    }
                    if (lockedSuggestionPosition == 0)
                        lockedSuggestionPosition = textRenderer.getWidth("> " + textFieldWidget.getText()) + 4;
                    textFieldWidget.setText("/" + completions.get(currentCompletion % completions.size()));
                    break;
                }
                if (currentMessageIndex == 0) {
                    currentMessageIndex = -1;
                    textFieldWidget.setText(currentMessage);
                } else if (currentMessageIndex > 0) {
                    textFieldWidget.setText(GlassBrigadier.PREVIOUS_MESSAGES.get(--currentMessageIndex));
                    invalidateSuggestions();
                }
                break;

            case Keyboard.KEY_RIGHT:
                if (
                        completions != null && !completions.isEmpty() &&
                        textFieldWidget.getCursorMax() == textFieldWidget.getText().length() &&
                        textFieldWidget.getCursorMax() == textFieldWidget.getCursorMin() // Make sure nothing's selected
                ) {
                    isTabbed = true;
                    textFieldWidget.setText("/" + getCurrentCompletionText());
                    break;
                }

            default:
                String oldText = textFieldWidget.getText();
                textFieldWidget.keyPressed(c, i);
                if (!oldText.equals(textFieldWidget.getText())) {
                    lockedSuggestionPosition = 0;
                    PacketHelper.send(new GlassBrigadierAutocompleteRequestPacket(autoCompleteMessage, autoCompleteMessage.length()));
                }
        }
    }

    @Unique
    void invalidateSuggestions() {
        glass_Essentials$setCompletions(null);
        lockedSuggestionPosition = 0;
        currentCompletion = 0;
    }

    @Unique
    void revalidateSuggestions() {
        if (completions == null || !textFieldWidget.getText().startsWith("/")) {
            return;
        }
        currentCompletion = 0;
        String noSlash = textFieldWidget.getText().substring(1);
        int noSlashPartSize = noSlash.split(" ").length;
        completions = completions.stream().filter(e -> e.split(" ").length == noSlashPartSize && e.startsWith(noSlash)).toList();
    }

    @Inject(method = "keyPressed", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/screen/ChatScreen;text:Ljava/lang/String;", shift = At.Shift.AFTER))
    void revalidateWhenTextChanges(char c, int i, CallbackInfo ci) {
        revalidateSuggestions();
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ClientPlayerEntity;sendChatMessage(Ljava/lang/String;)V"))
    void addMessageToQueue(char c, int i, CallbackInfo ci) {
        GlassBrigadier.PREVIOUS_MESSAGES.add(0, textFieldWidget.getText().trim());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;fill(IIIII)V"))
    private void renderSelectedCompletion(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (completions == null || textFieldWidget.getText() == null || !textFieldWidget.getText().startsWith("/") || completions.isEmpty()) {
            return;
        }
        textRenderer.draw(completions.get(currentCompletion % completions.size()), textRenderer.getWidth("> /") + 4, height - 12, 13421772);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderCompletions(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (completions == null || textFieldWidget.getText() == null || !textFieldWidget.getText().startsWith("/") || completions.isEmpty()) {
            return;
        }

        int baseX = lockedSuggestionPosition != 0 ? lockedSuggestionPosition : (textRenderer.getWidth("> " + textFieldWidget.getText()) + 4);
        int bottomY = height - 14;

        AtomicInteger longest = new AtomicInteger();

        completions.forEach(line -> {
            int width = textRenderer.getWidth(line);
            if (width > longest.get()) {
                longest.set(width);
            }
        });

        fill(baseX - 1, bottomY, baseX + longest.get() + 1, bottomY - (completions.size() * 11), BLACK);
        int x = lockedSuggestionPosition != 0 ? lockedSuggestionPosition : textRenderer.getWidth("> " + textFieldWidget.getText()) + 4;

        for (int i = 0; i < completions.size(); i++) {
            String[] lineParts = completions.get(i).split(" ");
            String line = lineParts[lineParts.length - 1];
            int y = height - 24 - (i * 11);

            drawTextWithShadow(textRenderer, line, x, y, i == (currentCompletion % completions.size()) ? YELLOW : WHITE);
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void updatePos(CallbackInfo ci) {
        if (textFieldWidget == null) {
            textFieldWidget = new ExtensibleTextFieldWidget(textRenderer) {
                @Override
                public void keyPressed(char c, int i) {
                    super.keyPressed(c, i);
                    text = getText(); // mod compat
                }
            };
            textFieldWidget.setShouldDrawBackground(false);
            textFieldWidget.setText("");
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"))
    private void renderCustom(ChatScreen instance, TextRenderer textRenderer, String s, int x, int y, int c, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int mouseX, @Local(argsOnly = true, ordinal = 1) int mouseY) {
        textRenderer.drawWithShadow("> ", x, y, -1);
        textFieldWidget.setSelected(true);
        textFieldWidget.setXYWH(x + textRenderer.getWidth("> "), y, width - x - 2, 14);
        textFieldWidget.draw(mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void handleMouseClick(int mouseX, int mouseY, int button, CallbackInfo ci) {
        textFieldWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public ExtensibleTextFieldWidget glass_Essentials$getTextField() {
        return textFieldWidget;
    }
}
