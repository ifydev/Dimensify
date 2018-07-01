package me.ifydev.dimensify.api.backend;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 06/26/2018
 */
@AllArgsConstructor
public enum ConnectionError {

    REJECTED("Connection to the database server rejected."),
    DATABASE_EXCEPTION("Exception in the database handler.");

    @Getter private String display;
}
