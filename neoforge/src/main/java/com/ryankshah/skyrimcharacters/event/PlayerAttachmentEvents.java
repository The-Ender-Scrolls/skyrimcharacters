package com.ryankshah.skyrimcharacters.event;

import com.ryankshah.skyrimcharacters.Constants;
import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class PlayerAttachmentEvents
{
    @SubscribeEvent
    public static void entityJoinLevel(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof Player player) {
            PlayerCharacter.entityJoinLevel(player);
        }
    }

    @SubscribeEvent
    public static void joinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof Player player) {
            PlayerCharacter.playerJoinWorld(player);
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.getEntity() instanceof Player player) {
            PlayerCharacter.playerChangedDimension(player);
        }
    }

//    @SubscribeEvent
//    public static void track(PlayerEvent.StartTracking event) {
//        if(event.getEntity() instanceof Player player) {
//            Character.playerStartTracking(player);
//        }
//    }

    @SubscribeEvent
    public static void playerDeath(LivingDeathEvent event) {
        if(event.getEntity() instanceof Player player) {
            PlayerCharacter.playerDeath(player);
        }
    }

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        PlayerCharacter.playerClone(event.isWasDeath(), event.getEntity(), event.getOriginal());
    }
}