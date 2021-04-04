package org.samo_lego.healthcare.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

public class PermissionHelper {
    public static boolean checkPermission(ServerCommandSource commandSource, String permission) {
        return Permissions.check(commandSource, permission, 4);
    }
}
