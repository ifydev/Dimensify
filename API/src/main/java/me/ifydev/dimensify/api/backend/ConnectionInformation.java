package me.ifydev.dimensify.api.backend;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author Innectic
 * @since 06/26/2018
 */
@AllArgsConstructor
@Getter
public class ConnectionInformation {
    // TODO: This was copy-pasted from Permissify. This should be generified into some-kind of a core plugin.
    private String url;
    private String database;
    private int port;
    private String username;
    private String password;
    private Map<String, Object> meta;
}
