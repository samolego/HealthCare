package org.samo_lego.healthcare.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

public class PermissionHelper {
    public static boolean checkPermission(CommandSourceStack commandSource, String permission, int defaultLevel) {
        return Permissions.check(commandSource, permission, defaultLevel);
    }
}
