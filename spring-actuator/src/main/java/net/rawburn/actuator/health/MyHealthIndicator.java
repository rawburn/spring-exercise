package net.rawburn.actuator.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.stereotype.Component;

import java.net.PortUnreachableException;
import java.util.HashMap;
import java.util.Map;

/**
 * 实现 HealthAggregator 自定义状态类型或者 management.health.status.order 配置
 *
 * @author renchao
 */
@Component
public class MyHealthIndicator extends AbstractHealthIndicator {

    @LocalServerPort
    private int port;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // perform some specific health check
        int port = portCheck();
        Map<String, Integer> details = new HashMap<>();
        if (port != 8080) {
            details.put("expect port", 8080);
            details.put("actual port", port);

            PortUnreachableException ex = new PortUnreachableException(
                    String.format("Invalid server port: %s", port));
            builder.status(Status.DOWN)
                    .withDetails(details)
                    .withException(ex);
        }
        builder.up();
    }

    private int portCheck() {
        return this.port;
    }

}
