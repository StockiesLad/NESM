package com.esm.nightmare.esmsounds;

import com.esm.nightmare.NightmareMain;

import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = NightmareMain.ModID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ESMSounds {

	public static SoundEvent nuclear_creeper_warn;

	/**
	 * The actual event handler that registers the custom items.
	 *
	 * @param event The event this event handler handles
	 */
	//public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		//ResourceLocation ResLoc = new ResourceLocation(NightmareMain.ModID, "nuclear_creeper_warn");
		//nuclear_creeper_warn = new SoundEvent(ResLoc).setRegistryName(ResLoc);
		//final SoundEvent[] soundEvents = { nuclear_creeper_warn };
		//event.getRegistry().registerAll(soundEvents);
	//}

}
