package com.ryankshah.skyrimcharacters;

import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SkyrimCharactersFabric implements ModInitializer
{
    public static AttachmentType<PlayerCharacter> CHARACTER_DATA =
            AttachmentRegistryImpl.<PlayerCharacter>builder()
                    .initializer(PlayerCharacter::new)
                    .persistent(PlayerCharacter.CODEC.codec())
                    .buildAndRegister(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "playercharacter"));

    @Override
    public void onInitialize() {
        CommonClass.init();
        initAttachments();
    }

    public static void initAttachments() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerCharacter.playerJoinWorld(handler.player);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if(entity instanceof Player player) {
                PlayerCharacter.playerDeath(player);
            }
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            PlayerCharacter.playerChangedDimension(player);
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerCharacter.playerClone(alive, newPlayer, oldPlayer);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PlayerCharacter.playerClone(alive, newPlayer, oldPlayer);
        });
//        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
//            PlayerCharacter.playerStartTracking(player);
//        });

    }
}
