package com.esm.nightmare.MobBehavs;

import net.minecraft.world.entity.monster.Creeper;

//Makes creepers blow up!
public class CreeperExplode {

	//Causes the given creeper to explode
	public CreeperExplode(Creeper C)
	{
		C.ignite();
	}
}
