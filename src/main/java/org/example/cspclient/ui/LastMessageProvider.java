package org.example.cspclient.ui;

/** Simple provider for last-message preview text */
@FunctionalInterface
public interface LastMessageProvider<T> {
    String previewFor(T thread);
}
