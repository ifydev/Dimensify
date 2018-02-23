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

import java.util.Arrays;
import java.util.List;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyConstants {
    private static final String DIMENSIFY_PREFIX = "&a&lDimensify> ";

    public static final String YOU_ARENT_A_PLAYER = DIMENSIFY_PREFIX + "You must be a player for this command!";

    // Not enough arguments responses
    private static final String NOT_ENOUGH_ARGUMENTS_BASE = DIMENSIFY_PREFIX + "&c&lNot enough arguments: ";
    public static final String NOT_ENOUGH_ARGUMENTS_CREATE_WORLD = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify create [dimension_type] [world_name]";
    public static final String NOT_ENOUGH_ARGUMENTS_SEND_PLAYER = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify send [player] [world_name]";
    public static final String NOT_ENOUGH_ARGUMENTS_GO = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify go [world_name]";
    public static final String NOT_ENOUGH_ARGUMENTS_DELETE = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify delete [world_name]";

    // General errors
    public static final String PLUGIN_NOT_PRESENT = DIMENSIFY_PREFIX + "&c&lUnable to continue: Plugin not present!";
    public static final String WORLD_EXISTS = DIMENSIFY_PREFIX + "&c&lWorld '<WORLD>' already exists!";
    public static final String INVALID_WORLD = DIMENSIFY_PREFIX + "&c&lWorld '<WORLD>' doesn't exist.";
    public static final String INVALID_DIMENSION_TYPE = DIMENSIFY_PREFIX + "&c&lDimension type '<TYPE>' is invalid.";
    public static final String INVALID_ENVIRONMENT_TYPE = DIMENSIFY_PREFIX + "&c&lEnvironment type '<TYPE>' is invalid.";
    public static final String SEED_NOT_PROVIDED = DIMENSIFY_PREFIX + "&c&lSeed has no value.";
    public static final String ENV_NOT_PROVIDED = DIMENSIFY_PREFIX + "&c&lEnv has no value.";
    public static final String INVALID_PLAYER = DIMENSIFY_PREFIX + "&c&lInvalid player: <PLAYER>";

    // General success
    public static final String CREATING_WORLD = DIMENSIFY_PREFIX + "World '<WORLD>' is being created...";
    public static final String WORLD_CREATED = DIMENSIFY_PREFIX + "World '<WORLD>' created.";
    public static final String WHOOSH = DIMENSIFY_PREFIX + "Whoosh!";
    public static final String WORLD_DELETED = DIMENSIFY_PREFIX + "World '<WORLD>' has been deleted.";
    public static final String YOU_HAVE_BEEN_SENT = DIMENSIFY_PREFIX + "You have been sent to <WORLD>!";
    public static final String PLAYER_HAS_BEEN_SENT = DIMENSIFY_PREFIX + "<PLAYER> has been sent to <WORLD>.";

    // Basic callbacks
//    public static final DimensifyCallback WORLD_SUCCESS_CALLBACK = (Map<String, String> meta) ->
//            System.out.println("World '" + meta.getOrDefault("world", "unknown") + "' has been loaded!");

    // Help response
    // TODO: This should automatically paginate.
    public static final String DIMENSIFY_HELP_HEADER = "&e================== &a&lDimensify  Help &e==================";
    public static final String DIMENSIFY_HELP_FOOTER = "&e=====================================================";
    public static final List<List<String>> HELP_RESPONSE = Arrays.asList(
            Arrays.asList(
                    "&a&l/dimensify create [dimension_type] [world_name]",
                    "&a&l/dimensify send [player] [world_name]",
                    "&a&l/dimensify go [world_name]",
                    "&a&l/dimensify delete [world_name]",
                    "&a&l/dimensify default [world_name?]",
                    "&a&l/dimensify list"
            )
    );
}
