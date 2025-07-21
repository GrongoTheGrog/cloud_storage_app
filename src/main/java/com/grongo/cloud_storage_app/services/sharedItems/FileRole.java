package com.grongo.cloud_storage_app.services.sharedItems;

import java.util.EnumSet;

public enum FileRole {
    VIEW_MODE("VIEW"),
    EDIT_MODE("EDIT"),
    ADMIN_MODE("ADMIN");

    private final EnumSet<FilePermission> filePermissionSet;

    private final String name;

    FileRole(String name){
        this.name = name;

        switch (name){
            case "VIEW":
                filePermissionSet = EnumSet.of(FilePermission.VIEW);
                break;
            case "EDIT":
                filePermissionSet = EnumSet.of(FilePermission.VIEW, FilePermission.UPDATE);
                break;
            case "ADMIN":
                filePermissionSet = EnumSet.of(
                        FilePermission.VIEW,
                        FilePermission.UPDATE,
                        FilePermission.DELETE,
                        FilePermission.SHARE,
                        FilePermission.MOVE
                );
                break;
            default:
                filePermissionSet = EnumSet.noneOf(FilePermission.class);
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

    public boolean hasPermission(FilePermission filePermission){
        return filePermissionSet.contains(filePermission);
    }
}
