package net.glasslauncher.glassbrigadier;

import net.glasslauncher.mods.gcapi3.api.ConfigEntry;

public class GlassBrigadierConfig {
    @ConfigEntry(
            name = "Singleplayer Commands",
            description = "Allows you to run most commands in singleplayer.",
            nameKey = "config.glassbrigadier.spc.name",
            descriptionKey= "config.glassbrigadier.spc.desc"
    )
    public Boolean singlePlayerCommands = true;

    @ConfigEntry(
            name = "Singleplayer Chat",
            description = "Allows you to open chat in singleplayer.",
            nameKey = "config.glassbrigadier.spChat.name",
            descriptionKey= "config.glassbrigadier.spChat.desc"
    )
    public Boolean singlePlayerChat = true;
}
