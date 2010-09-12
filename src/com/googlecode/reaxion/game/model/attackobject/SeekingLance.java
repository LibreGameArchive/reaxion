package com.googlecode.reaxion.game.model.attackobject;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.character.Character;
import com.googlecode.reaxion.game.state.BattleGameState;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

public class SeekingLance extends AttackObject {
	
	public static final String filename = "lance";
	protected static final int span = 300;
	protected static final float dpf = 4;
	
	public SeekingLance(Model m) {
    	super(filename, dpf, m);
    	flinch = true;
    	lifespan = span;
    }
	
	public SeekingLance(Model[] m) {
    	super(filename, dpf, m);
    	flinch = true;
    	lifespan = span;
    }
	
	@Override
	public void hit(BattleGameState b, Character other) {
		b.removeModel(this);
    }
	
	@ Override
    public void act(BattleGameState b) {
    	super.act(b);
    }
	
}
