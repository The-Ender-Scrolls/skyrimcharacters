package com.ryankshah.skyrimcharacters;

import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import com.ryankshah.skyrimcharacters.network.OpenCharacterCreationScreen;
import commonnetwork.api.Dispatcher;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(modid = Constants.MOD_ID)
public class SkyrimCharactersNeo
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final Supplier<AttachmentType<PlayerCharacter>> PLAYER_CHARACTER = ATTACHMENT_TYPES.register(
            "playercharacter", () -> AttachmentType.builder(() -> new PlayerCharacter()).serialize(PlayerCharacter.CODEC).copyOnDeath().build());

    public SkyrimCharactersNeo(IEventBus eventBus) {
        CommonClass.init();
    }

    @SubscribeEvent
    public static void showCreationScreen(PlayerEvent.PlayerLoggedInEvent event) {
        Dispatcher.sendToServer(new OpenCharacterCreationScreen(false));
    }
}