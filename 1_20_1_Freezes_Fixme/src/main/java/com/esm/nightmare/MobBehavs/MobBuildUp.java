package com.esm.nightmare.MobBehavs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.lang.Math;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.esm.nightmare.SnapToBlockCenter;

public class MobBuildUp {

	private static final Logger LOGGER = LogManager.getLogger();
	
	// Distance at which entities start building up
	int BuildUpDist = 4;

	// Block for zombie to place when building up
	Block Block_Type = Blocks.COBBLESTONE;

	// Is zombie building up?
	public boolean isDoing;

	// Behavior for this zombie building up to a player if they build a base in the
	// sky. Zombies husks and zombie villagers should use this behavior
	public MobBuildUp(Mob M) {

		// Get this monster's target
		Entity Tgt = M.getTarget();

		// If zombie has a target, then
		if (Tgt != null) {

			// If the zombie's target is a player, then
			if (Tgt instanceof ServerPlayer) {

				//LOGGER.info("4");
				// Cast to server player
				ServerPlayer Server_Player = (ServerPlayer) Tgt;

				// If this player is not in creative mode, then
				if (!Server_Player.isCreative()) {
					
					//LOGGER.info("5");
					// If this monster is close enoughj to this entity, then
					if (isCloseEnough(M, Tgt)) {
						
						//LOGGER.info("6");

						// Monster as entity class
						Entity Entity_Class = (Entity) M;

						// If monster is below the player, then
						if (isBelowPlayer(M, Tgt) && isPlayerHighEnough(M, Tgt)) {
							
							//LOGGER.info("7");
													
							// Get monster's block position
							BlockPos M_BlockPos = M.blockPosition();

							// Get block position above monster (usually at head level)
							BlockPos M_BlockPos_Above = M_BlockPos.above();

							// Get block position above head position
							BlockPos M_BlockPos_AboveHead = M_BlockPos_Above.above();

							// Only build up if position above is air
							//ServerLevel Server_Level = Server_Player.getLevel(); //1.19.3
							ServerLevel Server_Level = Server_Player.serverLevel(); //1.20.3
							
							// Get block above head state
							BlockState Block_State = Server_Level.getBlockState(M_BlockPos_AboveHead);

							// If can build above, then
							if (Block_State.isAir()) {
								
								// Get build direction
								Vec3 Norm_Build_Dir = GetBuildDir(M, Tgt);
								Vec3i Norm_Build_Dir_I = new Vec3i((int)Norm_Build_Dir.x,(int)Norm_Build_Dir.y,(int)Norm_Build_Dir.z);

								// Get new block position
								//BlockPos New_Pos = M_BlockPos.offset(Norm_Build_Dir.x, Norm_Build_Dir.y,
								//		Norm_Build_Dir.z);		//1.19.3
								
								BlockPos New_Pos = M_BlockPos.offset(Norm_Build_Dir_I);		//1.20.1

								// Block shift in that direction and place block there
								new MobPlaceBlock(Entity_Class, Block_Type, New_Pos);

								// Put zombie 1 block up
								M.setPos(M.blockPosition().getX(), M.blockPosition().getY() + 1,
										M.blockPosition().getZ());

								// Snap to center of block
								new SnapToBlockCenter(Entity_Class);

								// Make zombie swing
								M.swing(M.getUsedItemHand());
								isDoing = true;
							}

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

		return (Math.abs(M_X - Tgt_X) < BuildUpDist) && (Math.abs(M_Z - Tgt_Z) < BuildUpDist);

	}
	
	
	//Player must be over 1 block above monster
	boolean isPlayerHighEnough(Mob M, Entity Tgt)
	{
		int Dist = Math.abs(M.blockPosition().getY() - Tgt.blockPosition().getY());
		return Dist > 1;
	}

	// Is this monster below the player?
	boolean isBelowPlayer(Mob M, Entity Tgt) {
		// Get monster block position
		BlockPos M_BlockPos = M.blockPosition();

		// Get target block position
		BlockPos T_BlockPos = Tgt.blockPosition();

		// if target block position is above zombie, then, build up
		return M_BlockPos.getY() < T_BlockPos.getY();
	}

	// Get build direction for zombie blocks
	// M - This zombie
	// Tgt - The zombie's current target
	Vec3 GetBuildDir(Mob M, Entity Tgt) {

		// Get target block position
		BlockPos Tgt_Block_Pos = Tgt.blockPosition();

		// Tgt X,Y,Z
		float Tgt_X = Tgt_Block_Pos.getX();
		float Tgt_Y = Tgt_Block_Pos.getY();
		float Tgt_Z = Tgt_Block_Pos.getZ();

		// Get zombie block position
		BlockPos M_Block_Pos = M.blockPosition();

		// Zombie X,Y,Z
		float Z_X = M_Block_Pos.getX();
		float Z_Y = M_Block_Pos.getY();
		float Z_Z = M_Block_Pos.getZ();

		// Get normalized direction
		float Diff_X = Tgt_X - Z_X;
		float Diff_Y = Tgt_Y - Z_Y;
		float Diff_Z = Tgt_Z - Z_Z;
		Vec3 BlockDir = new Vec3(Diff_X, Diff_Y, Diff_Z);
		BlockDir = BlockDir.normalize();

		return BlockDir;
	}

}
