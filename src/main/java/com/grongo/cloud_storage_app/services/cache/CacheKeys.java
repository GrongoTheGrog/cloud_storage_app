package com.grongo.cloud_storage_app.services.cache;



public class CacheKeys {

    public static String fileLinkKey(Long fileId){
        return "file:" + fileId;
    }

    public static String itemQueryKey(int hashedDto){return "itemQuery:" + hashedDto; }

    public static String resetCodeKey(String email){return "resetCode:" + email;}

    public static String refreshTokenKey(String code){return "refreshCode:" + code;}
}
