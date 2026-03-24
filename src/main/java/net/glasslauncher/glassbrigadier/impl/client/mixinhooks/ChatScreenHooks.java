package net.glasslauncher.glassbrigadier.impl.client.mixinhooks;

import net.glasslauncher.mods.gcapi3.impl.screen.widget.ExtensibleTextFieldWidget;

import java.util.List;

public interface ChatScreenHooks {
    void glass_Essentials$setCompletions(List<String> completions);
    ExtensibleTextFieldWidget glass_Essentials$getTextField();

}
