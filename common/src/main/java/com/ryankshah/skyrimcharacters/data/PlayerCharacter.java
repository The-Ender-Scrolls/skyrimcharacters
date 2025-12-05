package com.ryankshah.skyrimcharacters.data;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function9;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ryankshah.skyrimcharacters.network.UpdatePlayerCharacter;
import com.ryankshah.skyrimcharacters.platform.Services;
import commonnetwork.api.Dispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class PlayerCharacter
{
    public static MapCodec<PlayerCharacter> CODEC = RecordCodecBuilder.mapCodec(characterInstance -> characterInstance.group(
            Codec.BOOL.fieldOf("characterCreated").forGetter(PlayerCharacter::isCharacterCreated),
            Race.RACE_CODEC.fieldOf("race").forGetter(PlayerCharacter::getRace),
            Codec.BOOL.fieldOf("isMale").forGetter(PlayerCharacter::isMale),
            Codec.FLOAT.fieldOf("skinTone").forGetter(PlayerCharacter::getSkinTone),
            Codec.FLOAT.fieldOf("hairStyle").forGetter(PlayerCharacter::getHairStyle),
            Codec.FLOAT.fieldOf("hairColor").forGetter(PlayerCharacter::getHairColor),
            Codec.FLOAT.fieldOf("eyeColor").forGetter(PlayerCharacter::getEyeColor),
            Codec.FLOAT.fieldOf("noseShape").forGetter(PlayerCharacter::getNoseShape),
            Codec.FLOAT.fieldOf("mouthShape").forGetter(PlayerCharacter::getMouthShape),
            Codec.FLOAT.fieldOf("browShape").forGetter(PlayerCharacter::getBrowShape)
    ).apply(characterInstance, PlayerCharacter::new));

    public static StreamCodec<FriendlyByteBuf, PlayerCharacter> STREAM_CODEC = composite10(
            ByteBufCodecs.BOOL,
            PlayerCharacter::isCharacterCreated,
            Race.STREAM_CODEC,
            PlayerCharacter::getRace,
            ByteBufCodecs.BOOL,
            PlayerCharacter::isMale,
            ByteBufCodecs.FLOAT,
            PlayerCharacter::getSkinTone,
            ByteBufCodecs.FLOAT,
            PlayerCharacter::getHairStyle,
            ByteBufCodecs.FLOAT,
            PlayerCharacter::getHairColor,
            ByteBufCodecs.FLOAT,
            PlayerCharacter::getEyeColor,
            ByteBufCodecs.FLOAT,
            PlayerCharacter::getNoseShape,
            ByteBufCodecs.FLOAT,
            PlayerCharacter::getMouthShape,
            ByteBufCodecs.FLOAT,
            PlayerCharacter::getBrowShape,
            PlayerCharacter::new
    );

    protected boolean characterCreated;
    protected Race race;
    protected boolean isMale;
    protected float skinTone;
    protected float hairStyle;
    protected float hairColor;
    protected float eyeColor;
    protected float noseShape;
    protected float mouthShape;
    protected float browShape;

    public PlayerCharacter(boolean characterCreated, Race race, boolean isMale, float skinTone,
                           float hairStyle, float hairColor, float eyeColor,
                           float noseShape, float mouthShape, float browShape) {
        this.characterCreated = characterCreated;
        this.race = race;
        this.isMale = isMale;
        this.skinTone = skinTone;
        this.hairStyle = hairStyle;
        this.hairColor = hairColor;
        this.eyeColor = eyeColor;
        this.noseShape = noseShape;
        this.mouthShape = mouthShape;
        this.browShape = browShape;
    }

    public PlayerCharacter() {
        this(false, Race.NORD, true, 0.5f, 0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f);
    }

    public boolean isCharacterCreated() {
        return characterCreated;
    }

    public void setCharacterCreated(boolean characterCreated) {
        this.characterCreated = characterCreated;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }

    public float getSkinTone() {
        return skinTone;
    }

    public void setSkinTone(float skinTone) {
        this.skinTone = skinTone;
    }

    public float getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(float hairStyle) {
        this.hairStyle = hairStyle;
    }

    public float getHairColor() {
        return hairColor;
    }

    public void setHairColor(float hairColor) {
        this.hairColor = hairColor;
    }

    public float getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(float eyeColor) {
        this.eyeColor = eyeColor;
    }

    public float getNoseShape() {
        return noseShape;
    }

    public void setNoseShape(float noseShape) {
        this.noseShape = noseShape;
    }

    public float getMouthShape() {
        return mouthShape;
    }

    public void setMouthShape(float mouthShape) {
        this.mouthShape = mouthShape;
    }

    public float getBrowShape() {
        return browShape;
    }

    public void setBrowShape(float browShape) {
        this.browShape = browShape;
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

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite10(final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1, final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2, final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3, final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4, final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5, final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6, final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7, final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8, final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9, final StreamCodec<? super B, T10> codec10, final Function<C, T10> getter10, final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> factory) {
        return new StreamCodec<B, C>() {
            public C decode(B p_381156_) {
                T1 t1 = codec1.decode(p_381156_);
                T2 t2 = codec2.decode(p_381156_);
                T3 t3 = codec3.decode(p_381156_);
                T4 t4 = codec4.decode(p_381156_);
                T5 t5 = codec5.decode(p_381156_);
                T6 t6 = codec6.decode(p_381156_);
                T7 t7 = codec7.decode(p_381156_);
                T8 t8 = codec8.decode(p_381156_);
                T9 t9 = codec9.decode(p_381156_);
                T10 t10 = codec10.decode(p_381156_);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
            }

            public void encode(B p_380991_, C p_381087_) {
                codec1.encode(p_380991_, getter1.apply(p_381087_));
                codec2.encode(p_380991_, getter2.apply(p_381087_));
                codec3.encode(p_380991_, getter3.apply(p_381087_));
                codec4.encode(p_380991_, getter4.apply(p_381087_));
                codec5.encode(p_380991_, getter5.apply(p_381087_));
                codec6.encode(p_380991_, getter6.apply(p_381087_));
                codec7.encode(p_380991_, getter7.apply(p_381087_));
                codec8.encode(p_380991_, getter8.apply(p_381087_));
                codec9.encode(p_380991_, getter9.apply(p_381087_));
                codec10.encode(p_380991_, getter10.apply(p_381087_));
            }
        };
    }
}