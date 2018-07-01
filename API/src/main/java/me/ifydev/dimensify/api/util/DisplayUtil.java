package me.ifydev.dimensify.api.util;

import me.ifydev.dimensify.api.backend.ConnectionError;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/30/2018
 */
public interface DisplayUtil {
    /**
     * Display an error to online players with the `permissify.admin` permission.
     *
     * @param error     type type of error thrown.
     * @param exception the exception thrown
     */
    void displayError(ConnectionError error, Optional<Exception> exception);
}
