package net.rawburn.retry;

import org.springframework.retry.support.RetryTemplate;

/**
 * @author rawburn·rc
 *
 * @see org.springframework.retry.RetryPolicy
 * @see org.springframework.retry.backoff.BackOffPolicy
 * @see org.springframework.retry.RetryCallback
 * @see org.springframework.retry.RecoveryCallback
 * @see org.springframework.retry.RetryListener
 * @see org.springframework.retry.RetryOperations
 * @see org.springframework.retry.support.RetryTemplate
 */
public class Application {

    public static void main(String[] args) {
        RetryTemplate build = RetryTemplateBuilder.newBuilder().build();
    }
}
