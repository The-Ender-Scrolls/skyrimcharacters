package com.ryankshah.skyrimcharacters.network;

import com.ryankshah.skyrimcharacters.Constants;
import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import com.ryankshah.skyrimcharacters.platform.Services;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record UpdatePlayerCharacter(PlayerCharacter character)
{
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "updatePlayerCharacter");

    public static final StreamCodec<FriendlyByteBuf, UpdatePlayerCharacter> CODEC = StreamCodec.composite(
            PlayerCharacter.STREAM_CODEC,
            UpdatePlayerCharacter::character,
            UpdatePlayerCharacter::new
    );

    public UpdatePlayerCharacter(final FriendlyByteBuf buffer) {
        this(buffer.readLenientJsonWithCodec(PlayerCharacter.CODEC.codec()));
    }

    public static void handle(PacketContext<UpdatePlayerCharacter> context) {
        if(context.side() == Side.CLIENT)
            handleClient(context);
        else
            handleServer(context);
    }

    public static void handleServer(PacketContext<UpdatePlayerCharacter> context) {
        ServerPlayer player = context.sender();
        Services.PLATFORM.setPlayerCharacterData(player, context.message().character);
        final UpdatePlayerCharacter sendToClient = new UpdatePlayerCharacter(Services.PLATFORM.getPlayerCharacter(player));
        Dispatcher.sendToClient(sendToClient, player);
    }

    public static void handleClient(PacketContext<UpdatePlayerCharacter> context) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Services.PLATFORM.setPlayerCharacterData(minecraft.player, context.message().character);
        });
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type() {
        return new CustomPacketPayload.Type<>(TYPE);
    }
}