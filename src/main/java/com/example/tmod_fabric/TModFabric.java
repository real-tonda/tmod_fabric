package com.example.tmod_fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TModFabric implements ModInitializer {
    public static final String MOD_ID = "tmod_fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Example block and item
    public static final Block EXAMPLE_BLOCK = new Block(FabricBlockSettings.of(Material.METAL).strength(4.0f).sounds(BlockSoundGroup.METAL));
    public static final Item EXAMPLE_ITEM = new Item(new FabricItemSettings());

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TMod Fabric!");

        // Register blocks
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "example_block"), EXAMPLE_BLOCK);
        
        // Register items
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "example_block"), new BlockItem(EXAMPLE_BLOCK, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "example_item"), EXAMPLE_ITEM);

        // Add items to creative inventory
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(EXAMPLE_BLOCK);
        });
        
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(EXAMPLE_ITEM);
        });

        LOGGER.info("TMod Fabric initialized successfully!");
    }
}
