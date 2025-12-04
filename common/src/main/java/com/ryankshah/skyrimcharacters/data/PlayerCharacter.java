package com.ryankshah.skyrimcharacters.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ryankshah.skyrimcharacters.network.UpdatePlayerCharacter;
import com.ryankshah.skyrimcharacters.platform.Services;
import commonnetwork.api.Dispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerCharacter
{
    public static MapCodec<PlayerCharacter> CODEC = RecordCodecBuilder.mapCodec(characterInstance -> characterInstance.group(
            Race.RACE_CODEC.fieldOf("race").forGetter(PlayerCharacter::getRace)
    ).apply(characterInstance, PlayerCharacter::new));

    public static StreamCodec<FriendlyByteBuf, PlayerCharacter> STREAM_CODEC = StreamCodec.composite(
            Race.STREAM_CODEC,
            PlayerCharacter::getRace,
            PlayerCharacter::new
    );

    protected Race race;

    public PlayerCharacter(Race race) {
        this.race = race;
    }

    public PlayerCharacter() {
        this(
                Race.NORD
        );
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public static PlayerCharacter get(Player player) {
        return Services.PLATFORM.getPlayerCharacter(player);
    }

    private void syncToSelf(Player owner) {
        syncTo(owner);
    }

    protected void syncTo(Player player) {
        Dispatcher.sendToClient(new UpdatePlayerCharacter(this), (ServerPlayer) player);
    }

//    protected void syncTo(PacketDistributor.PacketTarget target) {
//        target.send(new UpdatePlayerCharacter(this));
//    }

    public static void entityJoinLevel(Player player) {
        if (player.level().isClientSide())
            return;
        get(player).syncToSelf(player);
    }

    public static void playerJoinWorld(Player player) {
        if (player.level().isClientSide())
            return;
        get(player).syncToSelf(player);
    }

    public static void playerChangedDimension(Player player) {
        if (player.level().isClientSide())
            return;
        get(player).syncToSelf(player);
    }

    public static void playerStartTracking(Player player) {
        if (player.level().isClientSide())
            return;
        get(player).syncToSelf(player);
    }

    public static void playerDeath(Player player) {
        var newHandler = get(player);

        Services.PLATFORM.setPlayerCharacterData(player, Services.PLATFORM.getPlayerCharacter(player));
        Dispatcher.sendToClient(new UpdatePlayerCharacter(newHandler), (ServerPlayer) player);
    }

    public static void playerClone(boolean isWasDeath, Player player, Player oldPlayer) {
        if(!isWasDeath)
            return;
        PlayerCharacter oldHandler = PlayerCharacter.get(oldPlayer);
        Services.PLATFORM.setPlayerCharacterData(player, oldHandler);
        PlayerCharacter newHandler = PlayerCharacter.get(player);
        Dispatcher.sendToClient(new UpdatePlayerCharacter(newHandler), (ServerPlayer) player);
    }
}
