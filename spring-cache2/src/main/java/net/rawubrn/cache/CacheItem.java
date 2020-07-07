package net.rawubrn.cache;

import lombok.Data;

@Data
public class CacheItem {
    private String name;
    private long expireTime;
    private long refreshIntervalTime;
}