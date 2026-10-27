/**
 * File: NoteworthyForge.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-27
 */
package org.furranystudio.noteworthy.forge;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import org.furranystudio.noteworthy.Noteworthy;
import org.furranystudio.noteworthy.command.NoteworthyCommand;
import org.furranystudio.noteworthy.item.ModItems;
import org.furranystudio.noteworthy.registry.ModRegistry;
import org.furranystudio.noteworthy.song.Songs;

@Mod(Noteworthy.MODID)
public class NoteworthyForge {

    public NoteworthyForge(FMLJavaModLoadingContext context) {
        Noteworthy.registerContent();
        ForgeNetwork.register();
        RegisterEvent.getBus(context.getModBusGroup()).addListener(event -> ModRegistry.registerAll(event::register));
        BuildCreativeModeTabContentsEvent.BUS.addListener(event -> {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.getEntries().putAfter(new ItemStack(Items.JUKEBOX), new ItemStack(ModItems.JUKEBOX.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
                event.getEntries().putAfter(new ItemStack(Items.JUKEBOX), new ItemStack(ModItems.JUKEBOX.get()),
                    CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            }
        });

        ServerStartedEvent.BUS.addListener(event -> Songs.get(event.getServer()));
        OnDatapackSyncEvent.BUS.addListener(event -> {
            if (event.getPlayer() == null) {
                Songs.get(event.getPlayerList().getServer());
            }
        });

        RegisterCommandsEvent.BUS.addListener(event -> NoteworthyCommand.register(event.getDispatcher()));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NoteworthyForgeClient.init();
        }

        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(event -> Noteworthy.commonSetup());
    }
}
