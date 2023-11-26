package com.esm.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

//Enhancement that makes mobs much quicker when in the ocean, to destroy the viability of cheaping it out in the ocean.
public class AugmentOceanSpeed {

	//private static final UUID SPEED_MODIFIER_SOUL_SPEED_UUID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");

	// Normal entity movement speed in water
	float Movement_Speed = 9F;

	// Creeper movement speed in water
	float Creeper_Movement_Speed = 12F;

	// If this entity is swimming in the ocean, augment their speed.
	public AugmentOceanSpeed(Mob M) {
		// Get position under feet
		BlockPos Pos = M.blockPosition();
		BlockPos Feet_Pos = Pos.below();

		// If spider has target, then
		LivingEntity Tgt = M.getTarget();

		if (Tgt instanceof ServerPlayer) {
			
			
			ServerPlayer Plyr = (ServerPlayer) Tgt;
			
			// Get block at this position
			Level Curr_Level = M.getLevel();

			// Get block at feet
			Block Curr_Block = Curr_Level.getBlockState(Pos).getBlock();
			Block Feet_Block = Curr_Level.getBlockState(Feet_Pos).getBlock();

			// If feet block is water, then
			if (Feet_Block == Blocks.WATER || Curr_Block == Blocks.WATER) {
				M.lookAt(Plyr, 1F, 1F);
				tryAddOceanSpeed(M, Plyr);				
			} else {
				M.getAttribute(Attributes.MOVEMENT_SPEED).removeModifiers();
			}
		}
	}

	protected void tryAddOceanSpeed(Mob M, ServerPlayer Tgt) {
		LivingEntity Living_Entity = (LivingEntity) M;

		if (M.getType() == EntityType.CREEPER) {
			M.lookAt(Tgt, 0.25F, 0.25F);
			Living_Entity.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get())
					.setBaseValue(Creeper_Movement_Speed);
			Living_Entity.getAttribute(Attributes.MOVEMENT_SPEED)
					.addPermanentModifier(new AttributeModifier("Ocean Movement", 2.0F, Operation.MULTIPLY_BASE));
		} else {

			if (M.getType() == EntityType.SPIDER || M.getType() == EntityType.CAVE_SPIDER) {
				M.lookAt(Tgt, 0.25F, 0.25F);
				Living_Entity.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get())
						.setBaseValue(Movement_Speed);
				Living_Entity.getAttribute(Attributes.MOVEMENT_SPEED)
						.addPermanentModifier(new AttributeModifier("Ocean Movement", 2.0F, Operation.MULTIPLY_BASE));
			}
		} //

	}
}
