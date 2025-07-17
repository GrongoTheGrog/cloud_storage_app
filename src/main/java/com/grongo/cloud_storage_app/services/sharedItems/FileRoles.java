package com.grongo.cloud_storage_app.services.sharedItems;

import com.grongo.cloud_storage_app.models.items.File;

import java.util.HashSet;
import java.util.Set;

public enum FileRoles {
    VIEW_MODE("VIEW"),
    EDIT_MODE("EDIT"),
    ADMIN_MODE("ADMIN");

    private final Set<FilePermissions> filePermissionsSet = new HashSet<>();

    private final String name;

    FileRoles(String name){
        this.name = name;

        switch (name){
            case "VIEW":
                filePermissionsSet.add(FilePermissions.VIEW);
                break;
            case "EDIT":
                filePermissionsSet.add(FilePermissions.VIEW);
                filePermissionsSet.add(FilePermissions.UPDATE);
                break;
            case "ADMIN":
                filePermissionsSet.add(FilePermissions.VIEW);
                filePermissionsSet.add(FilePermissions.UPDATE);
                filePermissionsSet.add(FilePermissions.DELETE);
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
