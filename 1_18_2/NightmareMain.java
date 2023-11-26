package com.esm.nightmare;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.esm.nightmare.Base.NightmareCreeper;
import com.esm.nightmare.Base.NightmareSkeleton;
import com.esm.nightmare.Base.NightmareSpider;
import com.esm.nightmare.Base.NightmareZombie;
import com.esm.nightmare.MobBehavs.CreeperBreachWalls;
import com.esm.nightmare.MobBehavs.ItemChecker;
import com.esm.nightmare.MobBehavs.MobBloodlust;
import com.esm.nightmare.MobBehavs.MobBuildBridge;
import com.esm.nightmare.MobBehavs.MobBuildUp;
import com.esm.nightmare.MobBehavs.MobDig;
import com.esm.nightmare.MobBehavs.MobDigDown;
import com.esm.nightmare.MobBehavs.MobDigUp;
import com.esm.nightmare.MobBehavs.MobMaxLife;
import com.esm.nightmare.MobBehavs.MobPlaceTNT;
import com.esm.nightmare.MobBehavs.MobStartFires;
import com.esm.nightmare.MobBehavs.MobTargetPlayer;
import com.esm.nightmare.MobBehavs.SpawnRideableMob;
import com.esm.nightmare.MobBehavs.SpiderShootWeb;
import com.esm.nightmare.esmsounds.ESMSounds;

import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("nightmareinminecraft")
public class NightmareMain {

	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String ModID = "nightmareinminecraft";

	public NightmareMain() {

		// net.minecraft.world.entity.animal
		// net.minecraft.world.entity.monster
		// net.minecraft.world.entity.GlowSquid

		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
//		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ESMConfig.SPEC, "esm-config.toml");

		// Register event bus to play ESM sounds
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		// ForgeModSounds.REGISTRY.register(modEventBus);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {

	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onEntitySpawn(SpecialSpawn e) {

		// Get expanded entity properties
		Entity Entity_Class = e.getEntity();

		//Sieges can recur every X days. If on Siege day, then
		if (isSiegeDay(Entity_Class)) {

			// If is monster, then, target player
			if (Entity_Class instanceof Mob) {

				// If is not client object, then
				if (!Entity_Class.level.isClientSide) {

					new MobTargetPlayer(Entity_Class);
					
					// Cast as monster
					Mob M = (Mob) Entity_Class;

					// If this is a zombie, then
					if (Entity_Class.getType() == EntityType.ZOMBIE
							|| Entity_Class.getType() == EntityType.ZOMBIE_VILLAGER
							|| Entity_Class.getType() == EntityType.HUSK) {

						// Create new nightmare zombie
						new NightmareZombie(Entity_Class);
					}

					// If this is a creeper, then
					if (Entity_Class.getType() == EntityType.CREEPER) {

						if (ESMConfig.isChargedCreeperAllowed.get()) {
							// If entity is a creeper, then
							new NightmareCreeper(Entity_Class, ESMConfig.ChargedCreeperChance.get());
						}
					}

					// If this is a skeleton then
					if (Entity_Class.getType() == EntityType.SKELETON) {
						new NightmareSkeleton(Entity_Class);
					}

					else if (Entity_Class.getType() == EntityType.SPIDER
							|| Entity_Class.getType() == EntityType.CAVE_SPIDER) {
						new NightmareSpider(Entity_Class);
					}

					if (ESMConfig.Entity_Duplication.get()) {

						int MaxDupes = ESMConfig.MaxDuplicationClones.get();
						int MinDupes = ESMConfig.MinDuplicationClones.get();
						if (MinDupes > MaxDupes) {
							MinDupes = MaxDupes;
						}

						// If mob was not summoned after it was spawned and is not loading from save,
						// then...
						if (e.getSpawnReason() != MobSpawnType.MOB_SUMMONED
								&& e.getSpawnReason() != MobSpawnType.CHUNK_GENERATION) {
							int TotalDupes = new RNG().GetInt(MinDupes, MaxDupes);
							if (ShouldDuplicate()) {
								new DuplicateMob(M, TotalDupes);
							}
						}

						// If entity came from a spawner, run duplication again to create even more
						// monsters
						if (e.getSpawnReason() == MobSpawnType.SPAWNER) {

							int MaxDungeonDupes = ESMConfig.MaxDungeonDuplicationClones.get();
							int MinDungeonDupes = ESMConfig.MinDungeonDuplicationClones.get();
							if (MinDungeonDupes > MaxDungeonDupes) {
								MinDungeonDupes = MaxDungeonDupes;
							}

							int TotalDupes = new RNG().GetInt(MinDungeonDupes, MaxDungeonDupes);

							new DuplicateMob(M, TotalDupes);
						}
					}

					if (ESMConfig.isSpecialJockeyMobsAllowed.get()) {
						// Check if this entity should ride a mob (with RNG)
						double RNG_Val = new RNG().GetDouble(0.0, 100.0);
						double Jockey_Chance = ESMConfig.SpecialJockeyGenerationChance.get();
						if (RNG_Val < Jockey_Chance) {
							// This entity will spawn riding a special jockey animal
							new SpawnRideableMob(Entity_Class);
						}

					}

					// Get RNG for blood-lusting entities
					if (ESMConfig.AngryEntities.get()) {
						float Bloodlust_RNG = new RNG().GetInt(0, 100);
						if (Bloodlust_RNG < ESMConfig.AngryEntityChance.get()) {

							new MobBloodlust(Entity_Class);
						}
					}

				}
			}
		}
	}

	// Based on RNG, should this entity duplicate?
	boolean ShouldDuplicate() {
		int RNG_Val = new RNG().GetInt(0, 100);
		return RNG_Val < ESMConfig.DuplicationChance.get();
	}

	// Is the current day a day that is having a siege, based on siege recurrence?
	boolean isSiegeDay(Entity Entity_Class) {
		long Uptime = Entity_Class.getLevel().getDayTime();
		long DayCount = (Uptime / 24000);
		return ((DayCount % ESMConfig.InvadeEveryXDays.get()) == 0);
	}

	// Entity tick
	@SubscribeEvent
	public void EntityTick(LivingUpdateEvent e) {
		
		// Get entity being ticked
		Entity Entity_Class = e.getEntity();

		// If type is monster, then route to player
		if (Entity_Class instanceof Mob) {

			Mob Mob_Class = (Mob) Entity_Class;

			if (!Mob_Class.hasCustomName()) {
				LivingMobTick(e, Entity_Class);
			}else {
				if(Entity_Class instanceof Creeper)
				{
					Creeper C = (Creeper)Entity_Class;
					NightmareCreeper.CreeperTick(C);
				}
			}
		}
	}

	// Do tick only for a living mob
	void LivingMobTick(LivingUpdateEvent e, Entity Entity_Class) {
		
		// Current time
		long TimeStamp = System.currentTimeMillis();

		// If is not client object, then
		if (!Entity_Class.level.isClientSide) {

			new MobTargetPlayer(Entity_Class);

			// If this is a creeper, then, perform breaching behavior
			if (Entity_Class.getType() == EntityType.CREEPER) {

				Creeper C = (Creeper)Entity_Class;
				if (ESMConfig.isCreeperBreachingAllowed.get()) {
					// Breaching creeper AI
					new CreeperBreachWalls((Creeper) Entity_Class);
				}
	
				NightmareCreeper.CreeperTick(C);
			}

			// Monster instance
			Mob Entity_Mob = (Mob) Entity_Class;

			// If this is a zombie, then
			if (Entity_Class instanceof Zombie) {

				// Is game rule "Mob Griefing" Allowed?
				boolean isMobGriefingAllowed = Entity_Class.getServer().getGameRules()
						.getBoolean(GameRules.RULE_MOBGRIEFING);

				
				// Are zombies allowed to destroy blocks?
				if (isMobGriefingAllowed || ESMConfig.AllowZombieGriefing.get()) {

					//For setting of if we only want entities to grief if they're holding a pickaxe
					boolean isAllowedToGriefWithItemHeld = true;
					
					if(ESMConfig.EntitiesNeedPickaxesToBreakBlocks.get())
					{
						isAllowedToGriefWithItemHeld = ItemChecker.EntityHasPickaxe(Entity_Class);
					}
					
					
					// Get current cycle for mob. Cycles between: Digging, Digging up, Digging down.
					// Supposed to prevent irritating 1x1 death traps...

					boolean isAtTimeInterval = Clocker.IsAtTimeInterval(Entity_Class, ESMConfig.EntityDigDelay.get());

					if (isAtTimeInterval) {

						int Curr_Dig_Cycle = Clocker.GetIndexFromTime(3);

						switch (Curr_Dig_Cycle) {
						case 0: {
							MobDig Zombies_Dig = new MobDig(Entity_Class);
						}
						case 1: {
							MobDigUp Zombies_Dig_Up = new MobDigUp(Entity_Mob);
						}
						case 2: {
							MobDigDown Zombies_Dig_Down = new MobDigDown(Entity_Mob);
						}
						}
					}

					if (Clocker.IsAtTimeInterval(Entity_Class, ESMConfig.EntityBuildDelay.get())) {
						MobBuildUp Zombies_Build_Up = new MobBuildUp(Entity_Mob);
						MobBuildBridge Zombie_Build_Bridge = new MobBuildBridge(Entity_Mob);
					}

					if (ESMConfig.ZombiesLayTNT.get()) {
						// Place TNT
						new MobPlaceTNT(Entity_Class);
					}

					// Is fire-lighting allowed?
					if (ESMConfig.ZombiesLightFires.get()) {
						// If so, check if should light wood on fire?
						new MobStartFires(Entity_Mob);
					}
				}

			}

			// Behaviors for spiders
			if (Entity_Class.getType() == EntityType.SPIDER || Entity_Class.getType() == EntityType.CAVE_SPIDER) {

				if (ESMConfig.SpidersShootWebs.get()) {
					// Shoot webs
					new SpiderShootWeb(Entity_Mob);
				}
			}

			// Can entities hop on the ocean?
			if (ESMConfig.isEntitiesBounceOnWater.get()) {

				// LOGGER.info("1");
				// Augment entity speeds on ocean
				new AugmentOceanSpeed(Entity_Mob);
			}

		}
	}

	// Fires when player tries to sleep
	// e - Event
	@SubscribeEvent
	public void cancelSleep(PlayerSleepInBedEvent e) {

		if (!ESMConfig.AllowSleeping.get()) {
			// Set player spawn pos
			Player Player_Entity = e.getPlayer();
			new SetPlayerSpawn(Player_Entity, e.getPos());

			// Deny sleeping in bed
			e.setResult(BedSleepingProblem.NOT_SAFE);

		}
	}

	// Convert comma-separated list to entities
	public static EntityType[] GetEntitiesFromSerialized(String SerialList) {
		String[] Entity_String_List = SerialList.split(",");
		EntityType[] RetVal = new EntityType[Entity_String_List.length];
		for (int i = 0; i < Entity_String_List.length; i++) {
			String Curr_Entity = Entity_String_List[i];
			EntityType Entity_Type_Class = com.esm.nightmare.NightmareMain.ToEntity(Curr_Entity);
			RetVal[i] = Entity_Type_Class;
		}

		return RetVal;
	}

	// Finds an entity class by name
	public static EntityType ToEntity(String Name) {
		Optional<EntityType<?>> Entity_Type = net.minecraft.world.entity.EntityType.byString(Name);
		if (Entity_Type.isPresent()) {
			return Entity_Type.get();
		} else {
			return null;
		}

//		for (int i = 0; i < Mob_Subclasses.length; i++) {
//			Class Generic_Subclass = Mob_Subclasses[i];
//			
//			LOGGER.info("COMPARE " + Generic_Subclass.getName().toLowerCase() + " TO PARAM NAME " + Name);
//			if (Generic_Subclass.getName().toLowerCase() == Name) {
//				Entity Entity_Class = Entity.class.cast(Generic_Subclass);
//				return Entity_Class;
//			}
//		}

		// Entity not found by name

	}

	public static String EntitiesToSerialList(EntityType[] Entity_List) {

		String Entity_List_Str = "";
		for (int i = 0; i < Entity_List.length; i++) {
			EntityType Curr_Entity = Entity_List[i];
			if (Curr_Entity == null) {
				Entity_List_Str += "NULL,";
			} else {
				Entity_List_Str += Curr_Entity.getDescription().getString().toLowerCase() + ",";
			}

		}

		return Entity_List_Str;

	}

//	private void enqueueIMC(final InterModEnqueueEvent event) {
//		// some example code to dispatch IMC to another mod
//		InterModComms.sendTo("examplemod", "helloworld", () -> {
//			LOGGER.info("Epic Seige Mod Loaded!");
//			return "Hello world";
//		});
//
//	}

	private void processIMC(final InterModProcessEvent event) {
		// some example code to receive and process InterModComms from other mods
		LOGGER.info("Got IMC {}",
				event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
	}

//	// You can use SubscribeEvent and let the Event Bus discover methods to call
//	@SubscribeEvent
//	public void onServerStarting(ServerStartingEvent event) {
//
//	}

	// You can use EventBusSubscriber to automatically subscribe events on the
	// contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {

	}
}
