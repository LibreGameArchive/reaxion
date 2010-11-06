package com.googlecode.reaxion.game.burstgrid;

import java.util.ArrayList;

import com.googlecode.reaxion.game.ability.Ability;

/** 
 * A simple AbilityNode. Simply stores an Ability in a node.
 * 
 * @author Cy Neita
 */

public class AbilityNode extends BurstNode
{
	public Ability ab;
	
	public AbilityNode(Ability a, int id){
		super(id);
		ab = a;
	}
	
	public AbilityNode(String s, int id){
		super(id);
		/**
		 * Not yet implemented, but findAbility will return an Ability based on then name of the Ability
		 */
		//ab = findAbility(s);
	}
	
	public void print(){
		System.out.print(id + " Ability ");
	}
}