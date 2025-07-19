package com.grongo.cloud_storage_app.services.sharedItems;

import com.grongo.cloud_storage_app.models.items.File;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public enum FileRoles {
    VIEW_MODE("VIEW"),
    EDIT_MODE("EDIT"),
    ADMIN_MODE("ADMIN");

    private final EnumSet<FilePermissions> filePermissionsSet;

    private final String name;

    FileRoles(String name){
        this.name = name;

        switch (name){
            case "VIEW":
                filePermissionsSet = EnumSet.of(FilePermissions.VIEW);
                break;
            case "EDIT":
                filePermissionsSet = EnumSet.of(FilePermissions.VIEW, FilePermissions.UPDATE);
                break;
            case "ADMIN":
                filePermissionsSet = EnumSet.of(
                        FilePermissions.VIEW,
                        FilePermissions.UPDATE,
                        FilePermissions.DELETE,
                        FilePermissions.SHARE,
                        FilePermissions.MOVE
                );
                break;
            default:
                filePermissionsSet = EnumSet.noneOf(FilePermissions.class);
                break;
        }

    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getName(){
        return name;
    }

    public boolean hasPermission(FilePermissions filePermission){
        return filePermissionsSet.contains(filePermission);
    }
}
