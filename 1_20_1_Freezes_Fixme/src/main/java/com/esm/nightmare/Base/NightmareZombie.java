package com.esm.nightmare.Base;


import com.esm.nightmare.ESMConfig;
import com.esm.nightmare.GiveRandomArmor;
import com.esm.nightmare.GiveRandomPickaxe;
import com.esm.nightmare.RNG;
import com.esm.nightmare.MobBehavs.SpawnRideableMob;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;

//net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration

//Spawn a nightmare zombie with chance of it having a creeper as a jockey. Also has a chance of being a lightning zombie e.g. extremely fast,
//extra health.
public class NightmareZombie {
	  
    
	//Spawn a nightmare zombie with chance of it having a creeper as a jockey. Also has a chance of being a lightning zombie e.g. extremely fast,
	//extra health.
	public NightmareZombie(Entity Entity_Class) 
	{    	
		//Get RNG value for this spawn
    	int ZombWeightValue = new RNG().GetInt(0, 100);
    	    	
    	//Cast to zombie
    	Monster M = (Monster)Entity_Class;    		
    	
    	//Should zombies be allowed to hold special items?
    	if(ESMConfig.GiveZombiesPickaxe.get())
    	{
    		int PickaxeWeightVal = new RNG().GetInt(0,100);
	    	//Give this zombie a random pickaxe based on probability
	    	if(PickaxeWeightVal < ESMConfig.ChanceOfPickaxe.get())
	    	{
	    		new GiveRandomPickaxe(M);
	    	}
    	}
    	
    	//Should zombies be given special armor?
    	if(ESMConfig.GiveZombiesArmor.get())
    	{
    		//Give random armor
    		new GiveRandomArmor(Entity_Class);
    	}
	}
	

	// Set number of ticks for creeper
	void SetTicks(Monster M, int Amt) {
		M.getPersistentData().putInt("placecooloff", Amt);
		M.getPersistentData().putInt("digcooloff", Amt);
	}
	
}
