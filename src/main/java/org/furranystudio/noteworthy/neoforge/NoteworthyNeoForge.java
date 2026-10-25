/**
 * File: NoteworthyNeoForge.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-25
 */
package org.furranystudio.noteworthy.neoforge;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.command.NoteworthyCommand;
import org.furranystudio.noteworthy.item.ModItems;
import org.furranystudio.noteworthy.registry.ModRegistry;
import org.furranystudio.noteworthy.song.Songs;

@Mod(Noteworthy.MODID)
public class NoteworthyNeoForge {

    public NoteworthyNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Noteworthy.registerContent();
        NeoForgeNetwork.initSender();
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> NeoForgeNetwork.register(event));
        modEventBus.addListener((RegisterEvent event) -> ModRegistry.registerAll(event::register));
        modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.insertAfter(new ItemStack(Items.JUKEBOX), new ItemStack(ModItems.JUKEBOX.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        });

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> Songs.get(event.getServer()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) {
                Songs.get(event.getPlayerList().getServer());
            }
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> NoteworthyCommand.register(event.getDispatcher()));

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NoteworthyNeoForgeClient.init(modEventBus);
        }

        modEventBus.addListener((FMLCommonSetupEvent event) -> Noteworthy.commonSetup());
    }
}
