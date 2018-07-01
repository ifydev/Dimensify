/*
*
* This file is part of Dimensify, licensed under the MIT License (MIT).
* Copyright (c) Innectic
* Copyright (c) contributors
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
 */
package me.ifydev.dimensify.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyConstants {
    private static final String DIMENSIFY_PREFIX = "&a&lDimensify> ";

    public static final String YOU_ARENT_A_PLAYER = DIMENSIFY_PREFIX + "You must be a player for this command!";
    public static final String YOU_DONT_HAVE_PERMISSION = DIMENSIFY_PREFIX + "&c&lYou don't have permission for this command!";

    // Not enough arguments responses
    private static final String NOT_ENOUGH_ARGUMENTS_BASE = DIMENSIFY_PREFIX + "&c&lNot enough arguments: ";
    public static final String NOT_ENOUGH_ARGUMENTS_CREATE_WORLD = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify create <dimension_type> <world_name>";
    public static final String NOT_ENOUGH_ARGUMENTS_SEND_PLAYER = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify send <player> <dimension>";
    public static final String NOT_ENOUGH_ARGUMENTS_GO = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify go <dimension>";
    public static final String NOT_ENOUGH_ARGUMENTS_DELETE = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify delete <dimension>";
    public static final String NOT_ENOUGH_ARGUMENTS_PORTAL = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify portal <create|delete|link> [args...]";
    public static final String NOT_ENOUGH_ARGUMENTS_PORTAL_LINK = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify portal link <source_portal> <destination_dimension>";
    public static final String NOT_ENOUGH_ARGUMENTS_PORTAL_CREATE = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify portal create <name>";
    public static final String NOT_ENOUGH_ARGUMENTS_PORTAL_DELETE = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify portal delete <name>";
    public static final String NOT_ENOUGH_ARGUMENTS_UNLOAD_DIMENSION = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify unload <world> [save_map?]";

    // General errors
    public static final String DATABASE_HANDLER_NOT_PRESENT = DIMENSIFY_PREFIX + "&c&lUnable to continue: Database handler not present?!";
    public static final String WORLD_EXISTS = DIMENSIFY_PREFIX + "&c&lWorld '<WORLD>' already exists!";
    public static final String INVALID_WORLD = DIMENSIFY_PREFIX + "&c&lWorld '<WORLD>' doesn't exist.";
    public static final String INVALID_DIMENSION_TYPE = DIMENSIFY_PREFIX + "&c&lDimension type '<TYPE>' is invalid.";
    public static final String INVALID_ENVIRONMENT_TYPE = DIMENSIFY_PREFIX + "&c&lEnvironment type '<TYPE>' is invalid.";
    public static final String SEED_NOT_PROVIDED = DIMENSIFY_PREFIX + "&c&lSeed has no value.";
    public static final String INVALID_SEED = DIMENSIFY_PREFIX + "&c&lInvalid seed value!";
    public static final String ENV_NOT_PROVIDED = DIMENSIFY_PREFIX + "&c&lEnv has no value.";
    public static final String INVALID_PLAYER = DIMENSIFY_PREFIX + "&c&lInvalid player: <PLAYER>";
    public static final String INTERNAL_ERROR = DIMENSIFY_PREFIX + "&c&lAn internal error has occurred. Please check your console for more details";
    public static final String PORTAL_DOES_NOT_EXIST = DIMENSIFY_PREFIX + "&c&lPortal '<PORTAL>' does not exist!";
    public static final String MUST_LOOK_AT_PORTAL_BLOCKS = DIMENSIFY_PREFIX + "&c&lMust be looking at a portal block.";
    public static final String INVALID_PORTAL = DIMENSIFY_PREFIX + "&c&lInvalid portal!";
    public static final String PORTAL_NAME_ALREADY_USED = DIMENSIFY_PREFIX + "&c&lPortal name '<NAME>' already used!";
    public static final String CANNOT_DELETE_MAIN_WORLD = DIMENSIFY_PREFIX + "&c&lCannot delete the main world!";
    public static final String COULD_NOT_CONNECT_TO_DATABASE = DIMENSIFY_PREFIX + "&c&lCannot connect to the database!";
    public static final String CANNOT_ENTER_THIS_DIMENSION = DIMENSIFY_PREFIX + "&c&lYou don't have permission to enter this domain!";
    public static final String THIS_DIMENSION_DOES_NOT_EXIST_ANYMORE = DIMENSIFY_PREFIX + "&c&lThis dimension does not exist anymore!";
    public static final String COULD_NOT_SET_DEFAULT_WORLD = DIMENSIFY_PREFIX + "&2&lCould not set default world.";
    public static final String COULD_NOT_UNLOAD_DIMENSION = DIMENSIFY_PREFIX + "&2&lCould not unload dimension!";
    public static final String INVALID_HELP_PAGE = DIMENSIFY_PREFIX + "&2&lInvalid help page!";

    // General success
    public static final String CREATING_WORLD = DIMENSIFY_PREFIX + "World '<WORLD>' is being created...";
    public static final String WORLD_CREATED = DIMENSIFY_PREFIX + "World '<WORLD>' created!";
    public static final String WHOOSH = "&a&lWhoosh!";
    public static final String WORLD_DELETED = DIMENSIFY_PREFIX + "World '<WORLD>' has been deleted!";
    public static final String PLAYER_HAS_BEEN_SENT = DIMENSIFY_PREFIX + "<PLAYER> has been sent to <WORLD>!";
    public static final String PORTALS_LINKED = DIMENSIFY_PREFIX + "Portals have been linked!";
    public static final String PORTAL_CREATED = DIMENSIFY_PREFIX + "Portal '<PORTAL>' created!";
    public static final String PORTAL_DELETED = DIMENSIFY_PREFIX + "Portal '<PORTAL>' has been deleted!";
    public static final String PORTAL_LIST_FORMAT = "&3&l<NAME> &8- &E&lX: <X>, Y: <Y>, Z: <Z> &8- &2&lDimension: <DIMENSION> &8- &5&lDestination: <DESTINATION>";
    public static final String DIMENSION_LIST_FORMAT = "&3&l<NAME> &8- &5&lType: <TYPE> &8- &2&lDefault?: <DEFAULT>";
    public static final String THERE_ARE_NONE = DIMENSIFY_PREFIX + "&2&lThere are no <ITEM>!";
    public static final String DEFAULT_WORLD_FORMAT = DIMENSIFY_PREFIX + "&2&lDefault world: <WORLD>";
    public static final String DEFAULT_WORLD_SET = DIMENSIFY_PREFIX + "&2&lDefault world set to: <WORLD>!";
    public static final String DIMENSION_UNLOADED = DIMENSIFY_PREFIX + "&2&lDimension '<NAME>' unloaded!";
    public static final String CACHE_FORMAT = DIMENSIFY_PREFIX + "&2&lCache Status: &e&lDimensions: <DIMENSIONS> &8&l- &e&lPortals <PORTALS>";
    public static final String CACHE_PURGED = DIMENSIFY_PREFIX + "&2&lCache has been purged!";

    // Permissions
    public static final String DIMENSIFY_ADMIN = "dimensify.admin";
    public static final String DIMENSIFY_BASE = "dimensify.command.base";
    public static final String DIMENSIFY_ADD_PORTAL = "dimensify.portal.create";
    public static final String DIMENSIFY_REMOVE_PORTAL = "dimensify.portal.delete";
    public static final String DIMENSIFY_CREATE_DIMENSION = "dimensify.dimension.create";
    public static final String DIMENSIFY_REMOVE_DIMENSION = "dimensify.dimension.delete";
    public static final String DIMENSIFY_LIST_PORTALS = "dimensify.list.portals";
    public static final String DIMENSIFY_LIST_DIMENSIONS = "dimensify.list.dimensions";
    public static final String DIMENSIFY_LINK = "dimensify.portal.link";
    public static final String DIMENSIFY_DEFAULT = "dimensify.default";
    public static final String DIMENSIFY_GO = "dimensify.go";
    public static final String DIMENSIFY_SEND = "dimensify.send";
    public static final String DIMENSIFY_PORTAL = "dimensify.command.portal";
    public static final String DIMENSIFY_UNLOAD = "dimensify.dimension.unload";
    public static final String DIMENSIFY_CACHE = "dimensify.cache";

    // Help response
    // TODO: This should automatically paginate.
    public static final String DIMENSIFY_HELP_HEADER = "&e================== &a&lDimensify  Help &e==================";
    public static final String DIMENSIFY_HELP_FOOTER = "&e=====================================================";
    public static final List<List<String>> HELP_RESPONSE = Arrays.asList(
            Arrays.asList(
                "&a&l/dimensify create <dimension_type> <world_name> [args...]",
                "&a&l/dimensify send <player> <dimension>",
                "&a&l/dimensify go <dimension>",
                "&a&l/dimensify delete <dimension>",
                "&a&l/dimensify portal link <source_portal> <destination_dimension>",
                "&a&l/dimensify portal <create|delete> <name>",
                "&a&l/dimensify portal list",
                "&a&l/dimensify list"
            ),
            Arrays.asList(
                    "&a&l/dimensify unload <world> [save_map?]",
                    "&a&l/dimensify cache [purge]",
                    "&a&l/dimensify default [world]"
            )
    );

    public static final List<String> DIMENSIFY_ERROR = new ArrayList<>(Arrays.asList(
            "&c&lError encountered: <ERROR_TYPE>",
            "&c&lShould this be reported?: <SHOULD_REPORT>"
    ));
}
