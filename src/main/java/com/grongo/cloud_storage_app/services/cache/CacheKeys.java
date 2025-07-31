package com.grongo.cloud_storage_app.services.cache;



public class CacheKeys {

    public static String fileLinkKey(Long fileId){
        return "file:" + fileId;
    }

    public static String itemQueryKey(int hashedDto){return "itemQuery:" + hashedDto; }
}
