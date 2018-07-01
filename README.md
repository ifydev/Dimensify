# Dimensify

Configure and manage multiple worlds within Minecraft.

# Commands

All commands in this plugin have permissions.

To even run the `/dimensify` command, this requires `dimensify.command.base`

## `/dimensify create <dimension_type> <name> [args...]`

Create a new dimension.

Permission: `dimensify.dimension.create`
Arguments:
  * `dimension_type`
    - `flat`
    - `default`
    - `amplified`
    - `large_biomes`
    - `customized`
    - `DEFAULT_1_1`

Dimension type is what kind of generation to use. Possible values are displayed above.
    
Arguments:
  * `name`
  
  This is the name of the dimension. It will be used whenever you're trying to go to the dimension, link portals, send players, etc.

  The remaining arguments of this command are used for customizing the world generation.
  
  For example, if you wanted to have structures in your world, the command might be:
  
  `/dimensify create default testing structures`
  
  To add a seed:
  
  `/dimensify create default testing structures seed=somethingcool`
  
  To set the environment:
  
  `/dimensify create default testing structures seed=somethingcool env=nether`
  
  Possible values for `env`:
  
  - nether
  - normal
  - the_end

## `/dimensify send <player> <dimension>`

Send a player to a dimension.

Permission: `dimensify.send`

* `player`

The player you would like to send

Arguments:
* `dimension`

The name of the dimension you would like to send them to

## `/dimensify go <dimension>`

Send yourself to a dimension

Permission: `dimensify.go`

Arguments:
* `dimension`

The name of the dimension you would like to go to

## `/dimensify delete <dimension>`

Delete a dimension.

Permission: `dimensify.dimension.delete`

Arguments:
* `dimension`

The name of the dimension you would like to delete

## `/dimensify portal create <name>`

Create a new portal for linking.

Permission: `dimensify.portal.create`

Arguments:
* `name`

The name of the new portal

## `/dimensify portal delete <name>`

Delete a portal.

Permission: `dimensify.portal.delete`

Arguments:
* name

The name of the portal that should be deleted

## `/dimensify portal link <source_portal> <destination_dimension>`

Link a portal to a dimension.

Permission: `dimensify.portal.link`

Arguments:
* `source_portal`

The name of the portal that you have created previously using `/dimensify portal create`

Pre-existing links on portals can be overriden using this command.

* `destination_dimension`

The name of the dimension that the portal should send the player to

## `/dimensify portal list`

List all registered portals

Permission: `dimensify.list.portals`

## `/dimensify list`

List all registered worlds

Permission: `dimensify.list.dimensions`

## `/dimensify unload <world> [save_map?]`

Unload a world from memory.

Permission: `dimensify.dimension.unload`

Arguments:
- `world`

The name of the world to unload

- `save_map`

This is an optional argument. It is a boolean value, meaning only `true` and `false` work.

If this value is true, the map will be saved before getting unloaded.

## `/dimensify default [world]`

Get or set the default world.

Permission: `dimensify.default`

If no arguments are provided, this command will give you the default world.

Arguments:
- `world`

The world to set the default to
