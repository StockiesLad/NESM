package com.esm.nightmare;

import java.util.Random;

public class RNG {

	
	//Generate a random number between Min (inclusive) and Max (inclusive)
	public RNG()
	{		

	}	
		
	public int GetInt(int Min, int Max) {
		//Get random number for property weights
		Random New_RNG = new Random();
		return New_RNG.nextInt(Min, Max);
	}
	
	//Get random double-precision decimal value
	public double GetDouble(double Min, double Max)
	{
		Random New_RNG = new Random();
		return New_RNG.nextDouble(Min, Max);
	}
	
}
