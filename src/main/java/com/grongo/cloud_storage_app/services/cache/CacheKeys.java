package com.grongo.cloud_storage_app.services.cache;



public class CacheKeys {

    public static String folderItemsKey(Long userId, Long folderId){
        return "folder:" + folderId;
    }

    public static String fileLinkKey(Long fileId){
        return "file:" + fileId;
    }

    public static String itemQueryKey(int hashedDto){return "itemQuery:" + hashedDto; }
}
