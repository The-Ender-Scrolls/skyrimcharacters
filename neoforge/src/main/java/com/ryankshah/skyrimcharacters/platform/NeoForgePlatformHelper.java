package com.ryankshah.skyrimcharacters.platform;

import com.ryankshah.skyrimcharacters.SkyrimCharactersNeo;
import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import com.ryankshah.skyrimcharacters.platform.services.IPlatformHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public PlayerCharacter getPlayerCharacter(Player player) {
        return player == null ? new PlayerCharacter() : player.getData(SkyrimCharactersNeo.PLAYER_CHARACTER);
    }

    @Override
    public void setPlayerCharacterData(Player player, PlayerCharacter PlayerCharacterData) {
        player.setData(SkyrimCharactersNeo.PLAYER_CHARACTER, PlayerCharacterData);
    }
}