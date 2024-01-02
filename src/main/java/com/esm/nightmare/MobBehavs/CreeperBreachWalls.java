package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.ESMConfig;
import com.esm.nightmare.GetBlockingBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

//Handles logic for breaching creepers who can't get to player
public class CreeperBreachWalls {


	// Debug - Cause this creeper to explode immediately.
	public boolean Debug_ExplodeImmediately = false;
	
	// Have this creeper perform breaching against wall if they cannot get to the
	// player.
	public CreeperBreachWalls(Creeper C) {

		if (Debug_ExplodeImmediately) {
			new CreeperExplode(C);
		} else {

			// Get this creeper's current target
			Entity Creeper_Target = C.getTarget();

			// If Creeper has a target, then
			if (Creeper_Target != null) {

				// This creeper's entity class
				Entity Creeper_Entity = (Entity) C;

				if (Creeper_Target instanceof ServerPlayer) {

					ServerPlayer Plyr = (ServerPlayer) Creeper_Target;

					if (!Plyr.isCreative()) {
						

						// Get distance from creeper to its target
						float Dist_To_Tgt = C.distanceTo(Creeper_Target);

						// To breach, distance must be < 64
						boolean isCloseEnough = Dist_To_Tgt < ESMConfig.CreeperBreachingDistance.get();

						// Get creeper ticks
						int Ticks = GetTicks(C);

						// Is this creeper at a wall?
						if (isAtWall(Creeper_Entity) || isWithinStrikingDistance(C, Plyr)) {

							// If creeper has nowhere to go and is close enough
							if (isCloseEnough) {
								
								//Face player
								C.lookAt(C, 1.0F, 1.0F);
								
								// Tick for explosion
								IncTicks(C);

								// If over amount to blow creeper up, then
								if (Ticks > ESMConfig.CreeperObstructedExplodeTicks.get()) {

									// Cause creeper to explode
									new CreeperExplode(C);
								}
							} else {
								ResetTicks(C);
							}
						} else {
							ResetTicks(C);
						}
												
						//If creeper directly above player and is within distance then
						if(isHoveringAbove(C, Plyr))
						{
							// Cause creeper to explode
							new CreeperExplode(C);
						}
						
						
					} else {
						ResetTicks(C);
					}

				}
			}
		}
	}

	// Increment number of ticks
	void IncTicks(Creeper C) {
		int CurrTicks = GetTicks(C);
		CurrTicks++;
		SetTicks(C, CurrTicks);
	}

	// Set number of ticks for creeper
	void SetTicks(Creeper C, int Amt) {
		C.getPersistentData().putInt("nightmare_ticks", Amt);
	}

	// Reset this creeper's ticks for being against wall
	void ResetTicks(Creeper C) {
		SetTicks(C, 0);
	}

	// Get current amount of ticks this creeper has been standing still.
	int GetTicks(Creeper C) {
		int RetVal = C.getPersistentData().getInt("nightmare_ticks");
		return RetVal;
	}

	// Is this creeper at a wall?
	boolean isAtWall(Entity Entity_Class) {
		GetBlockingBlock Blocking_Block = new GetBlockingBlock(Entity_Class);
		return Blocking_Block.isFootBlocked && Blocking_Block.isBlocking;
	}
	
	//Is this creeper hovering above the player?
	boolean isHoveringAbove(Creeper C, ServerPlayer Player_Target) {
		
		BlockPos Creeper_Pos = C.blockPosition();
		BlockPos Player_Pos = Player_Target.blockPosition();
		
		//X difference and Z difference
		float x_diff = Math.abs(Creeper_Pos.getX() - Player_Pos.getX());
		float z_diff = Math.abs(Creeper_Pos.getZ() - Player_Pos.getZ());
		
		//If within distance square and player is below creeper, then
		return ((x_diff < ESMConfig.CreeperAboveExplodeDistance.get() && z_diff < ESMConfig.CreeperAboveExplodeDistance.get()) && Player_Pos.getY() < Creeper_Pos.getY());
		
	}
	
	//Is this creeper within a certain distance where it can explode and harm the player?
	boolean isWithinStrikingDistance(Creeper C, ServerPlayer Player_Target)
	{
		float Creeper_Dist = C.distanceTo(Player_Target);
		return Creeper_Dist < ESMConfig.CreeperStrikingDistance.get();
	}
}
