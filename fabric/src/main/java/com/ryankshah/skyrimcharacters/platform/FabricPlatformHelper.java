package com.ryankshah.skyrimcharacters.platform;

import com.ryankshah.skyrimcharacters.SkyrimCharactersFabric;
import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import com.ryankshah.skyrimcharacters.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public PlayerCharacter getPlayerCharacter(Player player) {
        return player == null ? new PlayerCharacter() : ((AttachmentTarget)player).getAttachedOrCreate(SkyrimCharactersFabric.CHARACTER_DATA, PlayerCharacter::new);
    }

    @Override
    public void setPlayerCharacterData(Player player, PlayerCharacter characterData) {
        ((AttachmentTarget)player).setAttached(SkyrimCharactersFabric.CHARACTER_DATA, characterData);
    }
}
