package net.glasslauncher.glassbrigadier.mixin.client.vanillajank;

import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.impl.client.mixinhooks.SplittingTextRenderer;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;
import java.util.List;

@Mixin(TextRenderer.class)
public abstract class ExtendTextRendererMixin implements SplittingTextRenderer {

    @Shadow public abstract int getWidth(String text);

    public List<String> glass_Brigadier$split(String text, int width) {
        return Arrays.asList(this.insertLineBreaks(text, width).split("\n"));
    }

    @Unique
    String insertLineBreaks(String text, int width) {
        int var3 = this.indexAtWidth(text, width);
        if (text.length() <= var3) {
            return text;
        } else {
            String var4 = text.substring(0, var3);
            String var5 = isolateFormatting(var4) + text.substring(var3 + (text.charAt(var3) == ' ' ? 1 : 0));
            return var4 + "\n" + this.insertLineBreaks(var5, width);
        }
    }

    @Unique
    private int indexAtWidth(String text, int width) {
        int var3 = text.length();
        int var4 = 0;
        int var5 = 0;
        int var6 = -1;

        for (boolean isBold = false; var5 < var3; var5++) {
            char var8 = text.charAt(var5);
            switch (var8) {
                case ' ':
                    var6 = var5;
                //noinspection DefaultNotLastCaseInSwitch So, mojank wrote this in a super specific way, and it breaks EVERYTHING if I sort it.
                default:
                    var4 += this.getWidth(String.valueOf(var8));
                    if (isBold) {
                        var4++;
                    }
                    break;
                case '§':
                    char var9 = text.charAt(++var5);
                    if (GlassBrigadier.AMI_LOADED) {
                        if (var9 == 'l' || var9 == 'L') {
                            isBold = true;
                        } else if (var9 == 'r' || var9 == 'R') {
                            isBold = false;
                        }
                    }
            }

            if (var8 == '\n') {
                var6 = ++var5;
                break;
            }

            if (var4 > width) {
                break;
            }
        }

        return var5 != var3 && var6 != -1 && var6 < var5 ? var6 : var5;
    }

    /**
     * Checks if a certain formatting character is a color. The different colors are:
     * <ul>
     * <li><b>0:</b> <span style="color:#000000">black</span></li>
     * <li><b>1:</b> <span style="color:#0000AA">dark blue</span></li>
     * <li><b>2:</b> <span style="color:#00AA00">dark green</span></li>
     * <li><b>3:</b> <span style="color:#00AAAA">dark aqua</span></li>
     * <li><b>4:</b> <span style="color:#AA0000">dark red</span></li>
     * <li><b>5:</b> <span style="color:#AA00AA">dark purple</span></li>
     * <li><b>6:</b> <span style="color:#FFAA00">gold</span></li>
     * <li><b>7:</b> <span style="color:#AAAAAA">gray</span></li>
     * <li><b>8:</b> <span style="color:#555555">dark gray</span></li>
     * <li><b>9:</b> <span style="color:#5555FF">blue</span></li>
     * <li><b>a/A:</b> <span style="color:#55FF55">green</span></li>
     * <li><b>b/B:</b> <span style="color:#55FFFF">aqua</span></li>
     * <li><b>c/C:</b> <span style="color:#FF5555">red</span></li>
     * <li><b>d/D:</b> <span style="color:#FF55FF">light_purple</span></li>
     * <li><b>e/E:</b> <span style="color:#FFFF55">yellow</span></li>
     * <li><b>f/F:</b> <span style="color:#FFFFFF">white</span></li>
     * <li><b>g/G:</b> <span style="color:#DDD605">minecoin gold</span></li>
     * </ul>
     */
    @Unique
    private static boolean isColor(char chr) {
        return chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
    }

    /**
     * Checks the type of formatting character.
     * <ul>
     * <li><b>k:</b> <span style="color:#000000">obfuscated</span></li>
     * <li><b>l:</b> <span style="color:#000000"><b>bold</b></span></li>
     * <li><b>m:</b> <span style="color:#000000"><s>strikethrough</s></span></li>
     * <li><b>n:</b> <span style="color:#000000"><u>underline</u></span></li>
     * <li><b>o:</b> <span style="color:#000000"><i>italic</i></span></li>
     * <li><b>r:</b> <span style="color:#000000">reset</span></li>
     * </ul>
     */
    @Unique
    private static boolean isFormatting(char chr) {
        return chr >= 'k' && chr <= 'o' || chr >= 'K' && chr <= 'O' || chr == 'r' || chr == 'R';
    }

    /**
     * Strips all non formatting characters from the string.
     */
    @Unique
    private static String isolateFormatting(String text) {
        String var1 = "";
        int var2 = -1;
        int var3 = text.length();

        while ((var2 = text.indexOf(167, var2 + 1)) != -1) {
            if (var2 != var3) {
                char var4 = text.charAt(var2 + 1);
                if (isColor(var4)) {
                    var1 = "§" + var4;
                } else if (isFormatting(var4)) {
                    var1 = var1 + "§" + var4;
                }
            }
        }

        return var1;
    }

}
