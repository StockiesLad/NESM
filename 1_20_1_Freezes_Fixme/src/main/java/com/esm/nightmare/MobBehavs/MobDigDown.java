package com.esm.nightmare.MobBehavs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

//Causes zombies to dig down from overworld to attack the player
public class MobDigDown {
	
	
	//Is zombie currently digging down?
	public boolean isDoing;
	
	//X,Z square at which zombie keeps digging
	float DigDist = 16;

	public MobDigDown(Mob M) {

		// Monster as an entity
		Entity M_Entity = (Entity) M;

		// If monster not swinging, then
		if (!M.swinging) {

			//If monster is right above player or close
			BlockPos Zombie_Pos = M_Entity.blockPosition();
			
			//Get zombie target
			Entity Tgt = M.getTarget();
			
			//If valid target, then
			if(Tgt != null)
			{
				
				//Is this target is a server player, then
				if(Tgt instanceof ServerPlayer)
				{					
					//If this zombie is right over player, then
					if(isCloseEnough(M, Tgt))
					{
						
						//If target below monster, then, monster starts digging
						if(isTargetUnderMonster(M, Tgt))
						{
							
							//Damage block under this monster if it can be damaged
							BlockPos Under_BlockPos = Zombie_Pos.below();
							
							Level Lvl = Tgt.level();
							
							Block Dmg_Block = Lvl.getBlockState(Under_BlockPos).getBlock();

							new MobDamageBlock(M_Entity, Dmg_Block, Under_BlockPos);
							
							isDoing = true;
						}					
					}			
				}
				
			}		
		}
	}
	
	// Get distance from target - is this monster close enough to start digging up?
	boolean isCloseEnough(Mob M, Entity Tgt) {

		// Get x and z for monster and target
		double M_X = M.blockPosition().getX();
		double M_Z = M.blockPosition().getZ();
		double Tgt_X = Tgt.blockPosition().getX();
		double Tgt_Z = Tgt.blockPosition().getZ();

		return (Math.abs(M_X - Tgt_X) < DigDist) && (Math.abs(M_Z - Tgt_Z) < DigDist);

	}
	
	
	//Is the zombie's target under them?
	boolean isTargetUnderMonster(Mob M, Entity Tgt)
	{
		// Get x and z for monster and target
		double M_Y = M.blockPosition().getY();
		double TgtY = Tgt.blockPosition().getY();
		
		return TgtY < M_Y;
	}
}