package com.googlecode.reaxion.game.model.attackobject;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.character.Character;
import com.googlecode.reaxion.game.state.StageGameState;

public class DarkPit extends AttackObject {
	
	public static final String filename = "dark-pit";
	protected static final int span = 510;
	protected static final float dpf = 0;
	protected static final float maxdpf = .2f;
	
	private final int upTime = 40;
	private final int downTime = 470;
	
	private Model user;
	
	public DarkPit(Model m) {
    	super(filename, dpf, m);
    	lifespan = span;
    	user = m;
    }
	
	public DarkPit(Model[] m) {
    	super(filename, dpf, m);
    	lifespan = span;
    	user = m[m.length-1];
    }
	
	@Override
	public void hit(StageGameState b, Character other) {
		// give hp to user
		((Character)user).heal(b, damagePerFrame/6);
    }
	
	@ Override
    public void act(StageGameState b) {
		// can't touch other black holes
		Model[] collisions = getModelCollisions(b);
        for (Model c : collisions)
        	if (c instanceof DarkPit)
        		finish(b);
		
		if (lifeCount < upTime)
			model.setLocalScale(3*(float)(lifeCount+1)/upTime);
		else if (lifeCount > downTime) {
			model.setLocalScale(3*(float)(lifespan - lifeCount + 1)/(lifespan - downTime));
			damagePerFrame = 0;
		} else
			damagePerFrame = maxdpf;
    	super.act(b);
    }
	
}
