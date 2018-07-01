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
package me.ifydev.dimensify.api.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.ifydev.dimensify.api.DimensifyConstants;

import java.util.Optional;

/**
 * @author Innectic
 * @since 5/11/2018
 */
public class MetaParser {

    @Getter
    @AllArgsConstructor
    public static class MetaResult {
        private Optional<String> error;
        private Optional<WorldMeta> meta;
    }

    public static MetaResult parse(String[] meta) {
        boolean structures = false;
        Optional<String> environment = Optional.empty();
        Optional<String> seed = Optional.empty();

        for (String part : meta) {
            if (part.equalsIgnoreCase("structure")) structures = true;
            else if (part.toLowerCase().startsWith("env=")) {
                String[] split = part.split("env=");
                if (split.length != 2)
                    return new MetaResult(Optional.of(DimensifyConstants.ENV_NOT_PROVIDED), Optional.empty());
                environment = Optional.of(split[1]);
            } else if (part.toLowerCase().startsWith("seed=")) {
                String[] split = part.split("seed=");
                if (split.length != 2)
                    return new MetaResult(Optional.of(DimensifyConstants.SEED_NOT_PROVIDED), Optional.empty());
                seed = Optional.of(split[1]);
            }
        }
        return new MetaResult(Optional.empty(), Optional.of(new WorldMeta(structures, environment, seed)));
    }
}
