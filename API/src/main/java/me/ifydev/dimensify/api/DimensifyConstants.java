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

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyConstants {
    public static final String DIMENSIFY_PREFIX = "&a&lDimensify> ";

    public static final String YOU_ARENT_A_PLAYER = DIMENSIFY_PREFIX + "You must be a player for this command!";

    // Not enough arguments responses
    public static final String NOT_ENOUGH_ARGUMENTS_BASE = DIMENSIFY_PREFIX + "&c&lNot enough arguments: ";
    public static final String NOT_ENOUGH_ARGUMENTS_CREATE_WORLD = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify create [dimension_type] [world_name]";
    public static final String NOT_ENOUGH_ARGUMENTS_SEND_PLAYER = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify send [player] [world_name]";
    public static final String NOT_ENOUGH_ARGUMENTS_GO = NOT_ENOUGH_ARGUMENTS_BASE + "/dimensify go [world_name]";

    // General errors
    public static final String PLUGIN_NOT_PRESENT = DIMENSIFY_PREFIX + "Unable to continue: Plugin not present!";
    public static final String WORLD_EXISTS = DIMENSIFY_PREFIX + "World '<WORLD>' already exists!";
    public static final String INVALID_WORLD = DIMENSIFY_PREFIX + "World '<WORLD>' doesn't exist.";

    // General success
    public static final String CREATING_WORLD = DIMENSIFY_PREFIX + "World '<WORLD>' is being created...";
    public static final String WORLD_CREATED = DIMENSIFY_PREFIX + "World '<WORLD>' created.";
}
