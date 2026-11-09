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
package me.ifydev.dimensifyspigot.world;

import lombok.RequiredArgsConstructor;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class SingleBiomeProvider extends BiomeProvider {

    private final Biome biome;

    @Override
    public Biome getBiome(WorldInfo world, int x, int y, int z) {
        return biome;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo world) {
        return Collections.singletonList(biome);
    }
}
