# Documentation

## Features Overview

+ Out-of-the-box compatibility with vanilla and many modded furnaces.
  <br> Like the [Fabric Transfer API][fabric-transfer-api] adds storages to vanilla and many modded container blocks, this library do the same for the percentage of remaining fuel in furnace blocks.

+ Blacklisting of the aforementioned compatibility, mostly for troubleshooting purposes.

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

## Project Overview

Do refer to the javadoc for a more comprehensive documentation.

+ [**`Burning`**](../src/main/java/niv/burning/api/Burning.java "niv.burning.api.Burning.java")
  <br> The core object of this mod, as well as its namesake.
  <br> It's a immutable wrapper around a percentage of a remaining fuel item, e.g., half a coal worth of burning.
  <br> It works as the base, almost primitive, unit of measure of this library.

### Block APIs

+ [**`BurningStorage`**](../src/main/java/niv/burning/api/BurningStorage.java "niv.burning.api.BurningStorage.java")
  <br> Storage API for the heat produced by fuel that furnaces burn. See [Fabric Transfer API][fabric-transfer-api] (or on [GitHub](https://github.com/FabricMC/fabric-api/tree/1.21.10/fabric-transfer-api-v1 "Fabric Transfer API (v1) | github.com")) and/or TechReborn's [Energy API](https://github.com/TechReborn/Energy/tree/master "TechReborn/Energy | github.com" ) to better understand why I implemented it the way it is.

+ [**`BurningPropagator`**](../src/main/java/niv/burning/api/BurningPropagator.java "niv.burning.api.BurningPropagator.java")
  <br> Block API for the creation of "propagation graphs" that can link burning storages together.

### Backports Bridges

+ [**`BurningContext`**](../src/main/java/niv/burning/api/BurningContext.java "niv.burning.api.BurningContext.java")
  <br> Introduced to bridge between pre 1.21.2 `AbstractFurnaceBlockEntity`'s fuel map and post 1.21.2 `FuelValues` objects.
  <br> It isn't planned yet, but I may grow annoyed of this and drop it in future versions.

### Base Implementations

The [**`api.base`**](../src/main/java/niv/burning/api/base/ "niv.burning.api.base") package contains some base implementations of the previous interfaces, both for ease of use and of actual use in this mod.

+ [**`SimpleBurningStorage`**](../src/main/java/niv/burning/api/base/SimpleBurningStorage.java "niv.burning.api.base.SimpleBurningStorage.java")
  <br> A simple implementations of a `BurningStorage`. Useful for mod developers that doesn't need much customization beyond "something that can store some burning".
  <br> For example, [Heater][heater] extends a `SimpleBurningStorage` for the heater block entity internal burning storage.

+ [**`SimpleBurningContext`**](../src/main/java/niv/burning/api/base/SimpleBurningContext.java "niv.burning.api.base.SimpleBurningContext.java")
  <br> A map-backed immutable implementation of a `BurningContext` with a lazy singleton instance identical to the pre 1.21.1 `AbstractFurnaceBlockEntity`'s fuel map.

+ [**`DelegatingBurningStorage`**](../src/main/java/niv/burning/api/base/DelegatingBurningStorage.java "niv.burning.api.base.DelegatingBurningStorage.java")
  <br> Delegates all methods call to another burning storage.
  <br> For example, [Heater][heater]'s registers one of these for the thermostat blocks.

+ [**`FurnaceBurningStorage`**](../src/main/java/niv/burning/api/base/FurnaceBurningStorage.java "niv.burning.api.base.FurnaceBurningStorage.java")
  <br> Used internally to mixin with `AbstractFurnaceBlockEntity` class.
  <br> Vanilla furnaces, when queried for a burning storage, return instances of this class.

+ [**`InfiniteBurningStorage`**](../src/main/java/niv/burning/api/base/InfiniteBurningStorage.java "niv.burning.api.base.InfiniteBurningStorage.java")
  <br> An always full, extraction only, instance of a burning storage. Useful to implement creative generators.

+ [**`BurningStorageBlockEntity`**](../src/main/java/niv/burning/api/base/BurningStorageBlockEntity.java "niv.burning.api.base.BurningStorageBlockEntity.java")
  <br> Utility interface for automatic registration. This library register a fallback for every block entity implementing this interface.
  <br> This library also implements this interface for the `AbstractFurnaceBlockEntity` abstract class.

[fabric-transfer-api]: https://wiki.fabricmc.net/tutorial:transfer-api "Fluid, Item and Energy Transfer | wiki.fabricmc.net"

[heater]: https://modrinth.com/mod/heater "Heater | modrinth.com"
