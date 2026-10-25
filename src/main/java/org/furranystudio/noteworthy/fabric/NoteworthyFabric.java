/**
 * File: NoteworthyFabric.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-25
 */
package org.furranystudio.noteworthy.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.command.NoteworthyCommand;
import org.furranystudio.noteworthy.item.ModItems;
import org.furranystudio.noteworthy.registry.ModRegistry;
import org.furranystudio.noteworthy.song.Songs;

import java.util.function.Supplier;

public final class NoteworthyFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Noteworthy.registerContent();
        ModRegistry.registerAll(NoteworthyFabric::registerVanilla);
        FabricNetwork.register();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output ->
            output.insertAfter(Items.JUKEBOX, ModItems.JUKEBOX.get()));

        ServerLifecycleEvents.SERVER_STARTED.register(Songs::get);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> Songs.get(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> NoteworthyCommand.register(dispatcher));

        Noteworthy.commonSetup();
    }

    // Fabric has no RegisterEvent, registries are still open here
    @SuppressWarnings("unchecked")
    private static <T> void registerVanilla(ResourceKey<? extends Registry<T>> registryKey, Identifier id, Supplier<T> value) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        if (registry == null) {
            throw new IllegalStateException("Unknown registry " + registryKey.identifier() + " for " + id);
        }
        Registry.register(registry, id, value.get());
    }
}
