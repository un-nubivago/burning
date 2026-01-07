# Documentation

## Features Overview

+ Out-of-the-box compatibility with vanilla and many modded furnaces.
  <br> Like how the [Fabric Transfer API][fabric-transfer-api] adds storages to vanilla and many modded container blocks, this library do the same for the blocks that burn fuel, inserting and extracting parts of a fuel's worth of burning.

+ Blacklisting furnaces that would be otherwise compatible out-of-the-box, mostly for troubleshooting purposes.

+ Data pack features to register furnaces that aren't out-of-the-box compatible.

### Blacklisting

This library provides the `burning:blacklist` block tag to deny the out-of-the-box compatibility for blocks (and, thus, relative block entities) under it.

To do so, create a data pack as follows:

```tree
<datapack_name>.zip
├── data
│   └── <datapack_name>
│       └── burning
│           └── tags
│               └── blocks (before 1.21) or block (since 1.21)
│                   └── blacklist.json
├── pack.mcmeta
└── pack.png (optional)
```

Where the `blacklist.json` file will look something like:

```json
{
    "replace": false,
    "values": [
        "minecraft:furnace",
        "mod_name:block_name",
        ...
    ]
}
```

### Data-pack Compatibility

This library provides the `burning:dynamic_storage` dynamic registry to allow compatibility with other furnaces using only data packs. Of course, implementing an addon mod to do so is more effective and powerful, but creating a data pack is faster, so here we go.

To do so, create a data pack as follows:

```tree
<datapack_name>.zip
├── data
│   └── <datapack_name>
│       └── burning
│           └── dynamic_storage
│               └── strange_mod:strange_furnace_type.json
│               └── another_mod:another_furnace_type.json
│               └── ...
├── pack.mcmeta
└── pack.png (optional)
```

(Obviously, instead of `strange_mod:strange_furnace_type` and the other one, you should put the id of the furnace block entity.)

Where a file like `strange_mod:strange_furnace_type` will look something like:

```json
{
    // The name of that block entity's type
    // Yes, the same name of the json file
    "type": "strange_mod:strange_furnace_type",
    // The name of the field of that block entity that is functionally equivalent to `litTimeRemaining`
    // This is, that holds the remaining fuel burn duration
    "lit_time": "burnTime",
    // The name of the field of that block entity that is functionally equivalent to `litTotalTime`
    // That is, that tracks the original fuel burn duration
    "lit_duration": "fuelTime"
}
```
To know the two fields names, you have to read the "Strange Mod" source code or ask one of its human maintainers.

## Getting Started

To get started on developing a mod using this library, add the following to your `build.gradle` file:

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

## Project Overview

Do refer to the javadoc for a more comprehensive documentation.

In general, see [Fabric Transfer API][fabric-transfer-api] (or on [GitHub](https://github.com/FabricMC/fabric-api/tree/1.21.10/fabric-transfer-api-v1 "Fabric Transfer API (v1) | github.com")).

+ [**`FuelVariant`**](../src/main/java/niv/burning/api/FuelVariant.java "niv.burning.api.FuelVariant.java")
  <br> An immutable wrapper around a fuel item with access to its burn duration.
  <br> The base, almost primitive, type of resource that this mod deals with.

### Block APIs

This mod doesn't (no longer, at least) implement a novel Block API but rather builds upon the concept pioneered with by the [Fabric Transfer API][fabric-transfer-api]. 

+ [**`BurningStorage`**](../src/main/java/niv/burning/api/BurningStorage.java "niv.burning.api.BurningStorage.java")
  <br> A static class providing the `BlockApiLookup` instance for sided access to all registered `Storage<FuelVariant>`.

### Base Implementations

Most `base` implementations of [Fabric Transfer API][fabric-transfer-api] are fully compatible with the `FuelVariant` variant. Use them.

+ [**`SimpleBurningStorage`**](../src/main/java/niv/burning/api/base/SimpleBurningStorage.java "niv.burning.api.base.SimpleBurningStorage.java")
  <br> A simple implementations of a `SingleVariantStorage<FuelVariant>`. Useful for mod developers that doesn't need much customization beyond "something that can store some burning".
  <br> For example, [Heater][heater] extends a `SimpleBurningStorage` for the heater block entity internal burning storage.

+ [**`BurningStorageBlockEntity`**](../src/main/java/niv/burning/api/base/BurningStorageBlockEntity.java "niv.burning.api.base.BurningStorageBlockEntity.java")
  <br> Utility interface for automatic registration. This library register a fallback for every block entity implementing this interface.
  <br> This library also implements this interface for the `AbstractFurnaceBlockEntity` abstract class.

[fabric-transfer-api]: https://wiki.fabricmc.net/tutorial:transfer-api "Fluid, Item and Energy Transfer | wiki.fabricmc.net"

[heater]: https://modrinth.com/mod/heater "Heater | modrinth.com"
