package net.rawubrn.service;

import lombok.Data;
import net.rawubrn.cache.CacheableX;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rawburn·rc
 */
@Service
public class CacheService {

    private static final Map<Long, User> users = new HashMap<>();

    static {
        users.put(1L, new User("Z"));
        users.put(2L, new User("Y"));
    }

    @CacheableX(expireTime = 90, refreshIntervalTime = 120)
    @Cacheable(value = "user", key = "#id")
    public User getUser(Long id) {
        System.out.println(">>>> db operation..");
        return users.get(id);
    }

    @Data
    static class User {
        private String name;

        public User() {
        }

        public User(String name) {
            this.name = name;
        }
    }
}
