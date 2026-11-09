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
package me.ifydev.dimensifyspigot.inventory;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerState {

    private ItemStack[] inventory = new ItemStack[41];
    private ItemStack[] enderChest = new ItemStack[27];
    private int heldSlot;
    private int level;
    private float experience;
    private int totalExperience;
    private double health = 20;
    private int food = 20;
    private float saturation = 5;
    private float exhaustion;
    private int fireTicks;
    private List<PotionEffect> effects = new ArrayList<>();

    public static PlayerState capture(Player player) {
        PlayerState state = new PlayerState();
        state.inventory = cloneItems(player.getInventory().getContents());
        state.enderChest = cloneItems(player.getEnderChest().getContents());
        state.heldSlot = player.getInventory().getHeldItemSlot();
        state.level = player.getLevel();
        state.experience = player.getExp();
        state.totalExperience = player.getTotalExperience();
        state.health = Math.max(1, player.getHealth());
        state.food = player.getFoodLevel();
        state.saturation = player.getSaturation();
        state.exhaustion = player.getExhaustion();
        state.fireTicks = player.getFireTicks();
        state.effects = new ArrayList<>(player.getActivePotionEffects());
        return state;
    }

    public static PlayerState death(PlayerDeathEvent event) {
        PlayerState state = capture(event.getEntity());
        if (!event.getKeepInventory()) state.inventory = new ItemStack[41];
        if (!event.getKeepLevel()) {
            state.level = event.getNewLevel();
            int nextLevel = state.level >= 30 ? 9 * state.level - 158 : state.level >= 15 ? 5 * state.level - 38 : 2 * state.level + 7;
            state.experience = Math.min(0.9999999f, event.getNewExp() / (float) nextLevel);
            state.totalExperience = event.getNewTotalExp();
        }
        state.health = 20;
        state.food = 20;
        state.saturation = 5;
        state.exhaustion = 0;
        state.fireTicks = 0;
        state.effects.clear();
        return state;
    }

    private static ItemStack[] cloneItems(ItemStack[] items) {
        return Arrays.stream(items).map(item -> item == null ? null : item.clone()).toArray(ItemStack[]::new);
    }

    public void save(ConfigurationSection section) {
        section.set("inventory", Arrays.asList(inventory));
        section.set("ender-chest", Arrays.asList(enderChest));
        section.set("held-slot", heldSlot);
        section.set("level", level);
        section.set("experience", experience);
        section.set("total-experience", totalExperience);
        section.set("health", health);
        section.set("food", food);
        section.set("saturation", saturation);
        section.set("exhaustion", exhaustion);
        section.set("fire-ticks", fireTicks);
        section.set("effects", effects);
    }

    public static PlayerState load(ConfigurationSection section) {
        PlayerState state = new PlayerState();
        if (section == null) return state;
        state.inventory = items(section, "inventory", 41);
        state.enderChest = items(section, "ender-chest", 27);
        state.heldSlot = section.getInt("held-slot");
        state.level = section.getInt("level");
        state.experience = (float) section.getDouble("experience");
        state.totalExperience = section.getInt("total-experience");
        state.health = section.getDouble("health", 20);
        state.food = section.getInt("food", 20);
        state.saturation = (float) section.getDouble("saturation", 5);
        state.exhaustion = (float) section.getDouble("exhaustion");
        state.fireTicks = section.getInt("fire-ticks");
        for (Object effect : section.getList("effects", List.of())) {
            if (!(effect instanceof PotionEffect)) throw new IllegalStateException("Invalid saved potion effect.");
            state.effects.add((PotionEffect) effect);
        }
        if (state.heldSlot < 0 || state.heldSlot > 8 || state.level < 0 || state.totalExperience < 0
                || !Float.isFinite(state.experience) || state.experience < 0 || state.experience >= 1
                || !Double.isFinite(state.health) || state.health <= 0 || state.food < 0 || state.food > 20
                || !Float.isFinite(state.saturation) || state.saturation < 0 || state.saturation > 20
                || !Float.isFinite(state.exhaustion) || state.exhaustion < 0)
            throw new IllegalStateException("Invalid saved player state.");
        return state;
    }

    private static ItemStack[] items(ConfigurationSection section, String key, int size) {
        List<?> items = section.getList(key);
        if (items == null || items.size() != size) throw new IllegalStateException("Invalid saved " + key);
        ItemStack[] result = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            if (items.get(i) != null && !(items.get(i) instanceof ItemStack)) throw new IllegalStateException("Invalid saved item.");
            result[i] = items.get(i) == null ? null : ((ItemStack) items.get(i)).clone();
        }
        return result;
    }

    public void apply(Player player, List<String> separate) {
        if (separate.contains("inventory")) {
            player.getInventory().setContents(cloneItems(inventory));
            player.getInventory().setHeldItemSlot(heldSlot);
        }
        if (separate.contains("ender-chest")) player.getEnderChest().setContents(cloneItems(enderChest));
        if (separate.contains("experience")) {
            player.setTotalExperience(totalExperience);
            player.setLevel(level);
            player.setExp(experience);
        }
        if (separate.contains("potion-effects")) {
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.addPotionEffects(effects);
            player.setFireTicks(fireTicks);
        }
        if (separate.contains("health")) player.setHealth(Math.min(health, player.getMaxHealth()));
        if (separate.contains("hunger")) {
            player.setFoodLevel(food);
            player.setSaturation(saturation);
            player.setExhaustion(exhaustion);
        }
    }
}
