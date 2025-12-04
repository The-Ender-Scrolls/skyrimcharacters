package com.ryankshah.skyrimcharacters.network;

import commonnetwork.api.Network;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class Networking
{
    public static void load() {
        Network.registerPacket(OpenCharacterCreationScreen.type(), OpenCharacterCreationScreen.class, OpenCharacterCreationScreen.CODEC, OpenCharacterCreationScreen::handle);
        Network.registerPacket(UpdatePlayerCharacter.type(), UpdatePlayerCharacter.class, UpdatePlayerCharacter.CODEC, UpdatePlayerCharacter::handle);
    }
}