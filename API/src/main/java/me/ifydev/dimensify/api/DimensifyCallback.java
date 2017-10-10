package me.ifydev.dimensify.api;

import java.util.Map;

/**
 * @author Innectic
 * @since 10/9/2017
 */
public interface DimensifyCallback {

    void success(Map<String, String> meta);
    default void failed(String reason) {}
}
