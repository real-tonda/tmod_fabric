package com.example.tmod_fabric;

import net.fabricmc.api.ClientModInitializer;

public class TModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client-side initialization code here
        TModFabric.LOGGER.info("TMod Fabric Client initialized!");
    }
}
