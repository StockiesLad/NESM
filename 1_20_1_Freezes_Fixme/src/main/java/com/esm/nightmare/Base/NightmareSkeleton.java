package com.esm.nightmare.Base;

import com.esm.nightmare.ESMConfig;
import com.esm.nightmare.GiveRandomArmor;
import com.esm.nightmare.RNG;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;

public class NightmareSkeleton {

	//Creates a nightmare skeleton
	//Entity - The skeleton enttiy
	public NightmareSkeleton(Entity Entity_Class)
	{
		//Give skeleton random armor
		if(ESMConfig.GiveSkeletonsArmor.get())
		{
			new GiveRandomArmor(Entity_Class);
		}
		
		//Get random number between 0 and 100 for chance of super skeleton
		if(ESMConfig.AllowSuperSkeletons.get())
		{
			int SkeleWeightValue = new RNG().GetInt(0, 100);
			double Chance_SuperSkeleton = ESMConfig.ChanceOfSuperSkeleton.get();
			if(SkeleWeightValue < Chance_SuperSkeleton)
			{
				Monster M = (Monster)Entity_Class;
				
				new SuperSkeleton(M);
			}
		}
	}
	
}
