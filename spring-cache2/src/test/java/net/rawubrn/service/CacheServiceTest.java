package net.rawubrn.service;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author rawburn·rc
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CacheServiceTest {

    @Autowired
    CacheService cacheService;

    @org.junit.Test
    public void getUser() {
        CacheService.User user0 = cacheService.getUser(1L);

        CacheService.User user1 = cacheService.getUser(1L);

        CacheService.User user2 = cacheService.getUser(2L);

        CacheService.User user3 = cacheService.getUser(2L);
    }
}