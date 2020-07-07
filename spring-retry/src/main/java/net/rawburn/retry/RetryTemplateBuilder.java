package net.rawburn.retry;

import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author rawburn·rc
 */
public class RetryTemplateBuilder {

    private RetryPolicy retryPolicy = new SimpleRetryPolicy(3);

    private BackOffPolicy backOffPolicy = new NoBackOffPolicy();

    private List<RetryListener> retryListeners = new ArrayList<>();

    private RetryTemplateBuilder() {
    }

    public static RetryTemplateBuilder newBuilder() {
        return new RetryTemplateBuilder();
    }

    public RetryTemplateBuilder withRetryListener(RetryListener listener) {
        retryListeners.add(listener);
        return this;
    }

    public RetryTemplateBuilder withRetryListener(RetryListener[] listener) {
        retryListeners.addAll(Arrays.asList(listener));
        return this;
    }

    public RetryTemplateBuilder withRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    public RetryTemplateBuilder withBackOffPolicy(BackOffPolicy backOffPolicy) {
        this.backOffPolicy = backOffPolicy;
        return this;
    }

    public RetryTemplate build() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(this.backOffPolicy);
        retryTemplate.setListeners(this.retryListeners.toArray(new RetryListener[retryListeners.size()]));
        retryTemplate.setRetryPolicy(this.retryPolicy);

        return retryTemplate;
    }
}
