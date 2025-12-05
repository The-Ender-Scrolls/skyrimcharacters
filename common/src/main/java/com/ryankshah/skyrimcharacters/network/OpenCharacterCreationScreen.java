package com.ryankshah.skyrimcharacters.network;

import com.ryankshah.skyrimcharacters.Constants;
import com.ryankshah.skyrimcharacters.client.screen.CharacterCreationScreen;
import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import com.ryankshah.skyrimcharacters.platform.Services;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record OpenCharacterCreationScreen(boolean hasSetup)
{
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "opencharactercreationscreen");

    public static final StreamCodec<FriendlyByteBuf, OpenCharacterCreationScreen> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            OpenCharacterCreationScreen::hasSetup,
            OpenCharacterCreationScreen::new
    );

    public OpenCharacterCreationScreen(final FriendlyByteBuf buffer) {
        this(buffer.readBoolean());
    }

    public static void handle(PacketContext<OpenCharacterCreationScreen> context) {
        if(context.side() == Side.CLIENT)
            handleClient(context);
        else
            handleServer(context);
    }

    public static void handleServer(PacketContext<OpenCharacterCreationScreen> context) {
        ServerPlayer player = context.sender();
        PlayerCharacter character = Services.PLATFORM.getPlayerCharacter(player);

        // Check if character has already been created
        boolean characterCreated = character.isCharacterCreated();

        final OpenCharacterCreationScreen sendToClient = new OpenCharacterCreationScreen(characterCreated);
        Dispatcher.sendToClient(sendToClient, player);
    }

    public static void handleClient(PacketContext<OpenCharacterCreationScreen> context) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Player player = Minecraft.getInstance().player;

            // Only open character creation screen if character hasn't been created yet
            if (!context.message().hasSetup) {
                Minecraft.getInstance().setScreen(new CharacterCreationScreen());
            }
            // If hasSetup is true, character was already created, so don't open the screen
        });
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type() {
        return new CustomPacketPayload.Type<>(TYPE);
    }
}