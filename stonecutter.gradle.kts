plugins {
    id("dev.kikugie.stonecutter")
    //id("fabric-loom") version "1.13-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.14-SNAPSHOT" apply false

}

stonecutter active "1.21.10"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
}