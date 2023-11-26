package com.esm.nightmare;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class GetNearestTarget {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public Entity Get(Entity Attacker) {

		// Current Minecraft server
		MinecraftServer Curr_Server = ServerLifecycleHooks.getCurrentServer();

		PlayerList Player_List = Curr_Server.getPlayerList();

		if (Curr_Server != null) {

			// Get random player index to have this mob attack
			int PlayerCount = Player_List.getPlayerCount();

			// If players in game, then
			if (PlayerCount > 0) {

				Entity Targeted_Entity = GetNearestEntity(Player_List, Attacker);

				return Targeted_Entity;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	Entity GetNearestEntity(PlayerList Player_List, Entity Attacker) {

		java.util.List<Entity> Nearby_Entities = GetNearbyEntities(Player_List, Attacker);
		int Entity_Count = Nearby_Entities.size();

		if (Entity_Count > 0) {
			
			float Winning_Dist = 999F;
			Entity Winning_Target = Nearby_Entities.get(0);

			// Get closest player in the list
			for (int i = 0; i < Entity_Count; i++) {

				// Get current player in the loop as a potential target
				Entity PotentialTgt = Nearby_Entities.get(i);

				if(Winning_Target != PotentialTgt)
				{				
					
					
					// Do not track players in creative mode...
					if (isEntitySurvivalModeOrMob(PotentialTgt)) {
						
						
						// Get distance from this creeper to the given player
						float Dist = Attacker.distanceTo(PotentialTgt);
	
						// If distance is winning distance, then, set this enttiy as the targeted player
						if (Dist < Winning_Dist) {
							
							Winning_Dist = Dist;
							Winning_Target = PotentialTgt;
							
							//LOGGER.info("Winning Entity = " + Winning_Target.getName() + " at dist " + Dist);
						}
					}
				}
			}
			return Winning_Target;
		} else {
			return null;
		}

	}

	// If targeting villagers is enabled, then, include them in the list.
	java.util.List<Entity> GetNearbyEntities(PlayerList Player_List, Entity Attacker) {

		java.util.List<Entity> Compiled_Entities = new ArrayList<Entity>();
		java.util.List<ServerPlayer> All_Players = Player_List.getPlayers();
		int PlayerCount = Player_List.getPlayerCount();

		// Get players
		for (int i = 0; i < PlayerCount; i++) {
			ServerPlayer Plyr = All_Players.get(i);
			if (!Plyr.isCreative() && Plyr.isAlive()) {
				Compiled_Entities.add((Entity) Plyr);
			}
		}

		// If should target villagers, then, include those as well.
		if (ESMConfig.isMonstersTargetVillagers.get()) {
			
			//LOGGER.info("1");
			
			TargetingConditions Tgt = TargetingConditions.forCombat().range(64.0D);
			java.util.List<Villager> All_Nearby_Entities = Attacker.getLevel().getNearbyEntities(
					net.minecraft.world.entity.npc.Villager.class, Tgt, (LivingEntity) Attacker,
					Attacker.getBoundingBox().inflate(16.0D, 4.0D, 16.0D));

			//LOGGER.info("Ent Size = " + All_Nearby_Entities.size());
			
			for (int i = 0; i < All_Nearby_Entities.size(); i++) {
				Villager Curr_Villager = All_Nearby_Entities.get(i);
				Entity Entity_Villager = (Entity) Curr_Villager;
				Compiled_Entities.add(Entity_Villager);
			}
		}
		
		//LOGGER.info("Compiled Ents = " + Compiled_Entities.size());

		return Compiled_Entities;

	}

	
	boolean isEntitySurvivalModeOrMob(Entity Entity_Class) {

		if (Entity_Class instanceof ServerPlayer) {
			ServerPlayer Server_Player = (ServerPlayer) Entity_Class;
			return Server_Player.isCreative();
		} else {
			return true;
		}

	}
}
