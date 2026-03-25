package net.glasslauncher.glassbrigadier.impl.command.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.permission.PermissionNode;
import net.glasslauncher.glassbrigadier.impl.argument.GlassArgumentBuilder;
import net.glasslauncher.glassbrigadier.impl.argument.GlassCommandBuilder;
import net.glasslauncher.glassbrigadier.impl.permission.Role;
import net.glasslauncher.glassbrigadier.impl.permission.RoleChain;
import net.glasslauncher.glassbrigadier.impl.permission.RoleManagerImpl;
import net.glasslauncher.glassbrigadier.impl.permission.UserPermissionManagerImpl;
import net.modificationstation.stationapi.api.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.glasslauncher.glassbrigadier.GlassBrigadier.*;
import static net.glasslauncher.glassbrigadier.api.argument.permissionnode.PermissionNodeArgumentType.permissionNode;
import static net.glasslauncher.glassbrigadier.api.argument.role.RoleArgumentType.role;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.booleanPermission;

public class RoleCommand implements CommandProvider {

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassCommandBuilder.literal("roles")
                .alias("r")
                .requires(booleanPermission("glassbrigadier.role"))
                .then(GlassArgumentBuilder.literal("list")
                        .executes(this::listRoles)
                )
                .then(GlassArgumentBuilder.literal("create")
                        .then(GlassArgumentBuilder.argument("role", string())
                                .executes(this::createRole)
                        )
                )
                .then(GlassArgumentBuilder.literal("prefix")
                        .then(GlassArgumentBuilder.argument("role", role())
                                .executes(this::setEmptyPrefix)
                                .then(GlassArgumentBuilder.argument("prefix", string())
                                        .executes(this::setPrefix)
                                )
                        )
                )
                .then(GlassArgumentBuilder.literal("suffix")
                        .then(GlassArgumentBuilder.argument("role", role())
                                .executes(this::setEmptySuffix)
                                .then(GlassArgumentBuilder.argument("suffix", string())
                                        .executes(this::setSuffix)
                                )
                        )
                )
//                .then(GlassArgumentBuilder.literal("chain")
//                        .then(GlassArgumentBuilder.literal("list")
//                                .executes(this::listChains)
//                        )
//                )
                .then(GlassArgumentBuilder.literal("permission")
                        .then(GlassArgumentBuilder.literal("set")
                                .then(GlassArgumentBuilder.argument("role", role())
                                        .then(GlassArgumentBuilder.argument("permission", permissionNode())
                                                .executes(this::setPermissionTrue)
                                                .then(GlassArgumentBuilder.argument("value", string())
                                                        .executes(this::setPermission)
                                                )
                                        )
                                )
                        )
                        .then(GlassArgumentBuilder.literal("remove")
                                .then(GlassArgumentBuilder.argument("role", role())
                                        .then(GlassArgumentBuilder.argument("permission", permissionNode())
                                                .executes(this::removePermission)
                                        )
                                )
                        )
                )
                ;
    }

    private int removePermission(CommandContext<GlassCommandSource> context) {
        Role role = context.getArgument("role", Role.class);
        PermissionNode<?> node = context.getArgument("permission", PermissionNode.class);
        role.setPermission(node, null);
        return 0;
    }

    private int setPermissionTrue(CommandContext<GlassCommandSource> context) {
        return setPermission(context, "true");
    }

    private int setPermission(CommandContext<GlassCommandSource> context) {
        return setPermission(context, context.getArgument("value", String.class));
    }

    private int setPermission(CommandContext<GlassCommandSource> context, String value) {
        Role role = context.getArgument("role", Role.class); // Sure would be nice to not have to specify this cause the field type's there- oh right, kotlin does this.
        PermissionNode<?> node = context.getArgument("permission", PermissionNode.class);
        if (role.setPermission(node, value)) {
            sendFeedbackAndLog(context.getSource(), systemMessage("Set \"" + node.path() + "\" to \"" + value + "\"."));
        }
        else {
            context.getSource().sendFeedback(Formatting.RED + "Failed to set \"" + node.path() + "\" to \"" + value + "\". Did you type the value correctly?");
        }
        return 0;
    }

    private int listChains(CommandContext<GlassCommandSource> context) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Available chains:");
        for (RoleChain chain : RoleChain.getStartingWith("")) {
            builder.append("\n ");
            builder.append(chain.getName());
            builder.append(" (");
            builder.append(chain.getRoles().size());
            builder.append(")");
        }
        context.getSource().sendFeedback(builder.toString());
        return 0;
    }

    private int listRoles(CommandContext<GlassCommandSource> context) {
        final StringBuilder builder = new StringBuilder();
        builder.append(systemMessagePrefix());
        builder.append(" ");
        builder.append("Available roles:");
        for (Role role : Role.getStartingWith("")) {
            builder.append("\n");
            builder.append(systemBulletPointPrefix());
            builder.append(" ");
            builder.append(role.getName());
            builder.append(" (");
            int players = UserPermissionManagerImpl.getUsers(role).size();
            builder.append(players);
            builder.append(" ");
            builder.append(players == 1 ? "player" : "players");
            builder.append(")");
        }
        context.getSource().sendFeedback(builder.toString());
        return 0;
    }

    private int setEmptyPrefix(CommandContext<GlassCommandSource> context) {
        return setPrefix(context, null);
    }

    private int setEmptySuffix(CommandContext<GlassCommandSource> context) {
        return setSuffix(context, null);
    }

    private int setSuffix(CommandContext<GlassCommandSource> context) {
        return setSuffix(context, context.getArgument("suffix", String.class));
    }

    private int setPrefix(CommandContext<GlassCommandSource> context) {
        return setPrefix(context, context.getArgument("prefix", String.class));
    }

    private int setSuffix(CommandContext<GlassCommandSource> context, String suffix) {
        Role role = context.getArgument("role", Role.class);
        role.setSuffix(suffix);
        RoleManagerImpl.updateAndSaveRolesFile();
        sendFeedbackAndLog(context.getSource(), systemMessage("Successfully updated suffix for " + role.getName() + ". Example: " + role.getDisplay("User")));
        return 0;
    }

    private int setPrefix(CommandContext<GlassCommandSource> context, String prefix) {
        Role role = context.getArgument("role", Role.class);
        role.setPrefix(prefix);
        RoleManagerImpl.updateAndSaveRolesFile();
        sendFeedbackAndLog(context.getSource(), systemMessage("Successfully updated prefix for " + role.getName() + ". Example: " + role.getDisplay("User")));
        return 0;
    }

    private int createRole(CommandContext<GlassCommandSource> context) {
        String role = context.getArgument("role", String.class);
        if (!RoleManagerImpl.addRole(new Role(role))) {
            context.getSource().sendFeedback(Formatting.RED + "Failed to create \"" + role + "\", does it already exist?");
            return 0;
        }

        RoleManagerImpl.updateAndSaveRolesFile();
        sendFeedbackAndLog(context.getSource(), systemMessage("Successfully created role \"" + role + "\"."));
        return 0;
    }
}
