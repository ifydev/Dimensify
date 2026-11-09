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
package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensifyspigot.commands.handlers.BasicHandler;
import me.ifydev.dimensifyspigot.commands.handlers.PortalHandler;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.world.GenerationOptions;
import me.ifydev.dimensifyspigot.world.WorldController;
import me.ifydev.dimensifyspigot.world.WorldFiles;
import me.ifydev.dimensifyspigot.world.WorldProperties;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.Registry;
import org.bukkit.World;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.Map;
import java.util.stream.Collectors;

public class DimensifyCommand implements TabExecutor {

    private static final Map<String, String> PERMISSIONS = Map.ofEntries(
            Map.entry("create", "dimensify.dimension.create"), Map.entry("delete", "dimensify.dimension.delete"),
            Map.entry("list", "dimensify.list.dimensions"), Map.entry("go", "dimensify.go"), Map.entry("send", "dimensify.send"),
            Map.entry("default", "dimensify.default"), Map.entry("unload", "dimensify.dimension.unload"),
            Map.entry("import", "dimensify.dimension.import"), Map.entry("remove", "dimensify.dimension.remove"),
            Map.entry("load", "dimensify.dimension.load"), Map.entry("clone", "dimensify.dimension.clone"),
            Map.entry("regen", "dimensify.dimension.regen"), Map.entry("modify", "dimensify.dimension.modify"),
            Map.entry("gamerule", "dimensify.dimension.modify"), Map.entry("info", "dimensify.dimension.info"),
            Map.entry("spawn", "dimensify.spawn"), Map.entry("setspawn", "dimensify.setspawn"),
            Map.entry("anchor", "dimensify.anchor"), Map.entry("inventory", "dimensify.inventory"),
            Map.entry("pair", "dimensify.dimension.link"), Map.entry("link", "dimensify.dimension.link"),
            Map.entry("reload", "dimensify.reload"), Map.entry("generators", "dimensify.dimension.create"),
            Map.entry("cache", "dimensify.cache"), Map.entry("portal", "dimensify.command.portal"));
    private static final List<String> HELP = List.of(
            "create <type> <world> [seed=... env=... structures=true generator=... biome=... generator-settings={...}]",
            "import <world> <environment> [generator]", "load <world>", "unload <world> [save=true|false]",
            "remove <world>", "delete <world>", "clone <world> <new-world>", "regen <world> [seed=...]", "confirm",
            "list", "info <world>", "modify <world> <property> <value>", "gamerule <world> <rule> [value]",
            "go <world|@anchor>", "send <player> <world|@anchor>", "spawn [world]", "setspawn", "default [world]",
            "pair <overworld> [nether end]", "link <world> <nether|end|overworld> <destination|none>",
            "inventory <status|group> [world group]", "anchor <set|delete|list> [name]",
            "portal <create|delete|link|list> [name destination]", "generators", "reload", "cache [purge]");
    private final Map<String, Runnable> confirmations = new HashMap<>();
    private final Map<String, Long> expires = new HashMap<>();
    private final Map<String, String> confirmationPermissions = new HashMap<>();

    private boolean allowed(CommandSender sender, String permission) {
        return sender.hasPermission(DimensifyConstants.DIMENSIFY_ADMIN) || sender.hasPermission(permission);
    }

    private void requirePermission(CommandSender sender, String permission) {
        if (!allowed(sender, permission)) throw new IllegalArgumentException("You don't have permission for this command.");
    }

    private void requireArguments(String[] args, int count, String usage) {
        if (args.length < count) throw new IllegalArgumentException("Usage: /dimensify " + usage);
    }

    private Player player(CommandSender sender) {
        if (!(sender instanceof Player)) throw new IllegalArgumentException("You must be a player for this command.");
        return (Player) sender;
    }

    private void respond(CommandSender sender, String response) {
        sender.sendMessage(ColorUtil.makeReadable("&a&lDimensify> &r" + response));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            requirePermission(sender, DimensifyConstants.DIMENSIFY_BASE);
            String action = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
            if (action.equals("help")) {
                int page = args.length > 1 ? Integer.parseInt(args[1]) : 1;
                int pages = (HELP.size() + 7) / 8;
                if (page < 1 || page > pages) throw new IllegalArgumentException("Help page must be 1-" + pages);
                respond(sender, "Help " + page + "/" + pages);
                HELP.subList((page - 1) * 8, Math.min(page * 8, HELP.size())).forEach(usage -> respond(sender, "/dimensify " + usage));
                return true;
            }
            if (action.equals("confirm")) {
                String key = sender.getName();
                Runnable pending = confirmations.remove(key);
                Long expiration = expires.remove(key);
                String permission = confirmationPermissions.remove(key);
                if (pending == null || expiration == null || expiration < System.currentTimeMillis())
                    throw new IllegalArgumentException("No pending operation. Confirmations expire after 60 seconds.");
                requirePermission(sender, permission);
                pending.run();
                return true;
            }
            if (!PERMISSIONS.containsKey(action)) throw new IllegalArgumentException("Unknown command. Use /dimensify help.");
            requirePermission(sender, PERMISSIONS.get(action));
            execute(sender, action, args);
        } catch (IllegalArgumentException | IllegalStateException e) {
            respond(sender, "&c" + e.getMessage());
        } catch (Exception e) {
            DimensifyMain.get().getLogger().log(Level.SEVERE, "Could not run Dimensify command", e);
            respond(sender, "&cCould not complete that operation. Check the server log.");
        }
        return true;
    }

    private void execute(CommandSender sender, String action, String[] args) {
        DimensifyMain plugin = DimensifyMain.get();
        WorldController controller = plugin.getWorldController();
        if (action.equals("create")) {
            requireArguments(args, 3, "create <type> <world> [options...]");
            World world = controller.create(args[2], args[1], Arrays.copyOfRange(args, 3, args.length));
            respond(sender, "World created: " + world.getName());
        } else if (action.equals("import")) {
            requireArguments(args, 3, "import <world> <environment> [generator]");
            respond(sender, "World imported: " + controller.importWorld(args[1], args[2], args.length > 3 ? args[3] : null).getName());
        } else if (action.equals("load")) {
            requireArguments(args, 2, "load <world>");
            respond(sender, "World loaded: " + controller.requireWorld(args[1]).getName());
        } else if (action.equals("unload")) {
            requireArguments(args, 2, "unload <world> [save=true|false]");
            boolean save = args.length < 3 || GenerationOptions.bool(args[2].replace("save=", ""));
            controller.unload(args[1], save);
            respond(sender, "World unloaded.");
        } else if (action.equals("remove")) {
            requireArguments(args, 2, "remove <world>");
            controller.unregister(args[1]);
            respond(sender, "World unregistered. Its files were kept.");
        } else if (action.equals("delete") || action.equals("regen")) {
            requireArguments(args, 2, action + " <world>" + (action.equals("regen") ? " [seed=...]" : ""));
            String name = controller.getSettings().resolve(args[1]);
            WorldFiles.validateName(name);
            if (!controller.names().contains(name)) throw new IllegalArgumentException("World is not registered.");
            String seed = args.length > 2 ? args[2].replaceFirst("^seed=", "") : null;
            confirmations.put(sender.getName(), () -> {
                Path backup = action.equals("delete") ? controller.delete(name) : controller.regenerate(name, seed);
                respond(sender, "World " + (action.equals("delete") ? "deleted" : "regenerated") + ". Original files: " + backup);
            });
            expires.put(sender.getName(), System.currentTimeMillis() + 60000);
            confirmationPermissions.put(sender.getName(), PERMISSIONS.get(action));
            respond(sender, "Run /dimensify confirm within 60 seconds to " + action + " " + name + ".");
        } else if (action.equals("clone")) {
            requireArguments(args, 3, "clone <world> <new-world>");
            controller.cloneWorld(args[1], args[2], response -> respond(sender, response));
            respond(sender, "Copying world...");
        } else if (action.equals("go") || action.equals("spawn")) {
            Player player = player(sender);
            if (action.equals("go")) requireArguments(args, 2, "go <world|@anchor>");
            Location destination = args.length > 1 ? controller.destination(String.join(" ", Arrays.copyOfRange(args, 1, args.length))) : controller.spawn(player.getWorld());
            if (!controller.teleport(player, destination)) throw new IllegalStateException("Teleport was cancelled.");
        } else if (action.equals("send")) {
            requireArguments(args, 3, "send <player> <world|@anchor>");
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) throw new IllegalArgumentException("Player is not online.");
            if (!controller.teleport(player, controller.destination(String.join(" ", Arrays.copyOfRange(args, 2, args.length))))) throw new IllegalStateException("Teleport was cancelled.");
            respond(sender, "Player sent.");
        } else if (action.equals("setspawn")) {
            controller.setSpawn(player(sender));
            respond(sender, "World spawn set.");
        } else if (action.equals("default")) {
            if (args.length > 1) {
                String name = controller.requireWorld(args[1]).getName();
                if (!plugin.getApi().getDatabaseHandler().get().setDefaultDimension(name)) throw new IllegalStateException("Could not set default world.");
            }
            respond(sender, "Default world: " + plugin.getApi().getDatabaseHandler().get().getDefaultDimension(false));
        } else if (action.equals("list")) {
            for (String name : controller.names()) {
                ConfigurationSection section = controller.getSettings().get(name);
                respond(sender, section.getString("alias", name) + " (" + name + ") - " + (Bukkit.getWorld(name) == null ? "unloaded" : "loaded"));
            }
        } else if (action.equals("info")) {
            requireArguments(args, 2, "info <world>");
            String name = controller.getSettings().resolve(args[1]);
            ConfigurationSection section = controller.getSettings().get(name);
            if (section == null) throw new IllegalArgumentException("World is not registered.");
            World world = Bukkit.getWorld(name);
            respond(sender, name + " - " + (world == null ? "unloaded" : world.getPlayers().size() + " players, " + world.getLoadedChunks().length + " loaded chunks"));
            section.getValues(true).forEach((key, value) -> { if (!(value instanceof ConfigurationSection)) respond(sender, key + ": " + value); });
        } else if (action.equals("modify")) {
            requireArguments(args, 4, "modify <world> <property> <value>");
            controller.modify(args[1], args[2].toLowerCase(Locale.ROOT), String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
            respond(sender, "World property updated.");
        } else if (action.equals("gamerule")) {
            requireArguments(args, 3, "gamerule <world> <rule> [value]");
            World world = controller.requireWorld(args[1]);
            GameRule<?> rule = WorldProperties.rule(args[2]);
            if (rule == null) throw new IllegalArgumentException("Unknown game rule.");
            if (args.length > 3) {
                WorldProperties.setRule(world, args[2], args[3]);
                controller.getSettings().get(world.getName()).set("gamerules." + args[2], args[3]);
                controller.getSettings().save();
            }
            respond(sender, args[2] + ": " + world.getGameRuleValue(rule));
        } else if (action.equals("pair")) {
            requireArguments(args, 2, "pair <overworld> [nether end]");
            World world = controller.requireWorld(args[1]);
            if (args.length != 2 && args.length != 4) throw new IllegalArgumentException("Provide both Nether and End worlds, or neither.");
            String nether = args.length == 4 ? args[2] : world.getName() + "_nether";
            String end = args.length == 4 ? args[3] : world.getName() + "_the_end";
            if (args.length == 2) {
                if (!controller.names().contains(nether)) controller.create(nether, "default", new String[] { "env=nether", "seed=" + world.getSeed() });
                if (!controller.names().contains(end)) controller.create(end, "default", new String[] { "env=the_end", "seed=" + world.getSeed() });
            }
            controller.pair(world.getName(), nether, end);
            respond(sender, "Worlds paired.");
        } else if (action.equals("link")) {
            requireArguments(args, 4, "link <world> <nether|end|overworld> <destination|none>");
            controller.link(args[1], args[2], args[3]);
            respond(sender, "Dimension link updated.");
        } else if (action.equals("inventory")) {
            requireArguments(args, 2, "inventory <status|group> [world group]");
            if (args[1].equalsIgnoreCase("group")) {
                requireArguments(args, 4, "inventory group <world> <group>");
                controller.modify(args[2], "inventory-group", args[3]);
                respond(sender, "Inventory group updated.");
            } else if (args[1].equalsIgnoreCase("status")) {
                respond(sender, "Inventory separation: " + plugin.getInventories().enabled());
                for (String name : controller.names()) respond(sender, name + ": " + controller.getSettings().group(name));
            } else throw new IllegalArgumentException("Usage: /dimensify inventory <status|group>");
        } else if (action.equals("anchor")) {
            requireArguments(args, 2, "anchor <set|delete|list> [name]");
            if (args[1].equalsIgnoreCase("list")) respond(sender, String.join(", ", controller.getSettings().anchors().getKeys(false)));
            else {
                requireArguments(args, 3, "anchor <set|delete> <name>");
                WorldFiles.validateName(args[2]);
                if (args[1].equalsIgnoreCase("set")) controller.anchor(player(sender), args[2]);
                else if (args[1].equalsIgnoreCase("delete")) {
                    controller.getSettings().anchors().set(args[2], null);
                    controller.getSettings().save();
                } else throw new IllegalArgumentException("Unknown anchor command.");
                respond(sender, "Anchor updated.");
            }
        } else if (action.equals("portal")) {
            portal(sender, args);
        } else if (action.equals("generators")) {
            respond(sender, "Enabled plugins (generator support depends on the plugin): " + Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .filter(Plugin::isEnabled).map(Plugin::getName).collect(Collectors.joining(", ")));
        } else if (action.equals("reload")) {
            plugin.reloadSettings();
            respond(sender, "Configuration reloaded.");
        } else if (action.equals("cache")) {
            respond(sender, args.length > 1 && args[1].equalsIgnoreCase("purge") ? BasicHandler.purgeCache() : BasicHandler.cacheStatus());
        }
    }

    private void portal(CommandSender sender, String[] args) {
        requireArguments(args, 2, "portal <create|delete|link|list> [name destination]");
        String action = args[1].toLowerCase(Locale.ROOT);
        String permission = action.equals("list") ? "dimensify.list.portals" : "dimensify.portal." + action;
        requirePermission(sender, permission);
        if (action.equals("list")) PortalHandler.listPortals().forEach(response -> respond(sender, response));
        else {
            requireArguments(args, 3, "portal " + action + " <name> [destination]");
            WorldFiles.validateName(args[2]);
            if (action.equals("create")) {
                Player player = player(sender);
                respond(sender, PortalHandler.createPortal(player, player.getTargetBlock(null, 8), args[2]));
            } else if (action.equals("delete")) respond(sender, PortalHandler.deletePortal(args[2]));
            else if (action.equals("link")) {
                requireArguments(args, 4, "portal link <name> <world|@anchor>");
                respond(sender, PortalHandler.linkPortal(args[2], args[3]));
            } else throw new IllegalArgumentException("Unknown portal command.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!allowed(sender, DimensifyConstants.DIMENSIFY_BASE)) return List.of();
        List<String> choices = new ArrayList<>();
        if (args.length == 1) {
            PERMISSIONS.forEach((action, permission) -> { if (allowed(sender, permission)) choices.add(action); });
            choices.addAll(List.of("help", "confirm"));
        } else {
            String action = args[0].toLowerCase(Locale.ROOT);
            if (!PERMISSIONS.containsKey(action) || !allowed(sender, PERMISSIONS.get(action))) return List.of();
            WorldController controller = DimensifyMain.get().getWorldController();
            if (action.equals("modify") && args.length == 3) choices.addAll(WorldProperties.PROPERTIES);
            else if (action.equals("gamerule") && args.length == 3) Registry.GAME_RULE.stream().map(rule -> rule.getKey().getKey()).forEach(choices::add);
            else if (action.equals("create") && args.length == 2) choices.addAll(List.of("default", "flat", "amplified", "large_biomes"));
            else if (action.equals("create") && args.length > 3) choices.addAll(List.of("seed=", "env=normal", "env=nether", "env=the_end", "structures=true", "structures=false", "generator=", "biome=", "generator-settings="));
            else if (action.equals("import") && args.length == 3) choices.addAll(List.of("normal", "nether", "the_end"));
            else if (action.equals("send") && args.length == 2) Bukkit.getOnlinePlayers().forEach(player -> choices.add(player.getName()));
            else if (action.equals("portal") && args.length == 2) choices.addAll(List.of("create", "delete", "link", "list"));
            else if (action.equals("portal") && args.length == 3 && !args[1].equalsIgnoreCase("create"))
                DimensifyMain.get().getApi().getDatabaseHandler().get().getPortals(false).forEach(portal -> choices.add(portal.getName()));
            else if (action.equals("anchor") && args.length == 2) choices.addAll(List.of("set", "delete", "list"));
            else if (action.equals("anchor") && args.length == 3) choices.addAll(controller.getSettings().anchors().getKeys(false));
            else if (action.equals("inventory") && args.length == 2) choices.addAll(List.of("status", "group"));
            else if (action.equals("link") && args.length == 3) choices.addAll(List.of("nether", "end", "overworld"));
            else {
                choices.addAll(controller.names());
                for (String name : controller.names()) {
                    String nameAlias = controller.getSettings().get(name).getString("alias");
                    if (nameAlias != null && !nameAlias.contains(" ")) choices.add(nameAlias);
                }
                if (action.equals("go") || action.equals("send") || action.equals("portal"))
                    controller.getSettings().anchors().getKeys(false).forEach(anchor -> choices.add("@" + anchor));
            }
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return choices.stream().filter(choice -> choice.toLowerCase(Locale.ROOT).startsWith(prefix)).distinct().sorted().collect(Collectors.toList());
    }
}
