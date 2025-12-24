# Burning

**Burning** is a library mod that adds a unified interface for managing the amount of burning fuel within most, if not all, furnace-like blocks from vanilla Minecraft and other mods.

Uses Fabric's Lookup and Transaction APIs.

## Documentation

**Burning** revolves around the [`BurningStorage`](src/main/java/niv/burning/api/BurningStorage.java) interface, which manages the insertions and extractions of burning fuel, and the [`Burning`](src/main/java/niv/burning/api/Burning.java) class, which represents the burning fuel itself.

You may read the documentation or the source code to learn more.

### Installation

Install it as you'd with any other mod, and without further configuration, every block which entity extends the `AbstractFurnaceBlockEntity` (Mojang mappings' name) abstract class, be them the vanilla _Furnace_, _Blast Furnace_, or _Smoker_, or any third-party block, will get a `BurningStorage` automatically and thus will be ready to interop with every other mod using this library.

### A furnace is breaking the game? Blacklist it

You may not want some blocks extending `AbstractFurnaceBlockEntity` (Mojang mappings' name) to get a `BurningStorage`, whether because it doesn't work or because of your preference.

By tagging any such block under the `#burning:blacklist` block tag, you can prevent it from automatically getting a BurningStorage.

To do so, and for every such block, you must figure out the name of that block, which is usually `mod_name:block_name`.

Then you must create a data pack like the one in the following example.

<details>
<summary>Expand</summary>

```tree
<datapack_name>.zip
├── data
│   └── <datapack_name>
│       └── burning
│           └── tags
│               └── blocks (before 1.21) or block (after 1.21)
│                   └── blacklist.json
├── pack.mcmeta
└── pack.png (optional)
```

</details>

</br>

Where the `blacklist.json` file shall look something like this:

<details>
<summary>Expand</summary>

```json
{
    "replace": false,
    "values": [
        "mod_name:block_name_1",
        "mod_name:block_name_2",
        ...
    ]
}
```

</details>

### A furnace isn't getting a BurningStorage? Dynamically register it

Some mods add block entities that can burn fuel, but that, for one reason or another, don't extend the `AbstractFurnaceBlockEntity` abstract class, and thus, they won't automatically get a `BurningStorage`.

Fret not, for **Burning** offers the `burning:dynamic_storage` dynamic registry, which, through data packs and a bit of reflection, resolves this specific problem.

How to do so.

For every such block entity, you must figure out a couple of things beforehand, that is:
* The name of that block entity's type.
* The name of the field of that block entity that is functionally equivalent to `AbstractFurnaceBlockEntity.litTimeRemaining` (Mojang mappings' name, `litTime` before 1.21.4).
* The name of the field of that block entity that is functionally equivalent to `AbstractFurnaceBlockEntity.litTotalTime` (Mojang mappings' name, `litDuration` before 1.21.4).

Then you must create a data pack like the one in the following example.

<details>
<summary>Expand</summary>

```tree
<datapack_name>.zip
├── data
│   └── <datapack_name>
│       └── burning
│           └── dynamic_storage
│               └── <block_entity_type_1>.json
│               └── <block_entity_type_2>.json
│               └── ...
├── pack.mcmeta
└── pack.png (optional)
```

</details>

</br>

Where each `*.json` file shall look something like this (comments are for illustration only, remove them in actual JSON):

<details>
<summary>Expand</summary>

```json
{
    // The name of that block entity's type
    "type": "example_mod:custom_furnace_entity_type",
    // The name of the field of that block entity that is functionally equivalent to `litTimeRemaining`
    "lit_time": "burnTime",
    // The name of the field of that block entity that is functionally equivalent to `litTotalTime`
    "lit_duration": "fuelTime"
}
```

</details>

### As for mod developers

First things first, to add `Burning` as a dependency, dd the following to your `gradle.build`:

```gradle
repositories {
    // Add Modrinth maven repository
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    // Add Burning dependency
    modImplementation "maven.modrinth:burning:<burning_version>"
}
```

For more information about the Modrinth maven repository read [here](https://support.modrinth.com/en/articles/8801191-modrinth-maven).

Then, you can either use it to find `BurningStorage`s to interact with through the `BurningStorage.SIDED` [`BlockApiLookup`](https://github.com/FabricMC/fabric/blob/1.21.11/fabric-api-lookup-api-v1/src/main/java/net/fabricmc/fabric/api/lookup/v1/block/BlockApiLookup.java) object.

Or you can start implementing your own `BurningStorage`. Starting from a [SimpleBurningStorage](src/main/java/niv/burning/api/base/SimpleBurningContext.java) and go on from there!
