package me.ifydev.dimensify.api.backend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.ifydev.dimensify.api.backend.impl.SQLHandler;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Innectic
 * @since 06/26/2018
 */
@AllArgsConstructor
@Getter
public enum BackendType {
    FLAT_FILE(Optional.empty(), "Flat file"),
    MYSQL(Optional.of(SQLHandler.class), "MySQL"),
    SQLITE(Optional.of(SQLHandler.class), "SQLite");

    private Optional<Class<? extends AbstractDataHandler>> handler;
    private String displayName;

    public static Optional<BackendType> getHandlerForType(String type) {
        return Arrays.stream(values()).filter(handler -> handler.getDisplayName().equalsIgnoreCase(type)).findFirst();
    }
}
