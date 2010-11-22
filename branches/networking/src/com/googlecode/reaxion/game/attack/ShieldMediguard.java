package com.googlecode.reaxion.game.attack;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.attackobject.MediShield;
import com.googlecode.reaxion.game.state.StageGameState;
import com.googlecode.reaxion.game.util.LoadingQueue;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

/**
 * Raises a barrier that protects from attacks and heals the user
 */
public class ShieldMediguard extends Attack {
	
	private static final int duration = 140;
	
	private MediShield medishield;
	
	public ShieldMediguard() {
		name = "Mediguard";
		gaugeCost = 8;
	}
	
	public ShieldMediguard(AttackData ad) {
		super(ad, 8);
		name = "Mediguard";
		validateGround();
	}
	
	public static void load() {
		LoadingQueue.push(new Model(MediShield.filename));
	}
	
	@Override
	public void firstFrame(StageGameState b) {
		character.moveLock = true;
		character.jumpLock = true;
		character.animationLock = true;
		
		character.play("guard");
		character.setVelocity(new Vector3f(0, 0, 0));
		
		// calculate transformations
		Vector3f rotation = character.rotationVector;
		float angle = FastMath.atan2(rotation.x, rotation.z);
		Vector3f translation = new Vector3f(1.5f*FastMath.sin(angle), 0, 1.5f*FastMath.cos(angle));
		
		medishield = (MediShield)LoadingQueue.quickLoad(new MediShield(getUsers()), b);
		
		medishield.rotate(rotation);
		medishield.model.setLocalTranslation(character.model.getWorldTranslation().add(translation));
		
		b.getRootNode().updateRenderState();
	}
	
	@Override
	public void nextFrame(StageGameState b) {
		if (frameCount >= duration) {
			finish();
		}
	}
	
	@Override
	public void interrupt(StageGameState b, Model other) {
		// check if the interrupting object passed through the barrier
        Model[] collisions = other.getLinearModelCollisions(b, other.getVelocity().normalize().mult(-1.5f), .02f);
        for (Model c : collisions) {
        	if (c == medishield) {
        		// cancel the damage and no flinch
        		//((AttackObject) other).hit(b, character);
            	return;
        	}
        }
	}
	
	@Override
	public void finish() {
		super.finish();
		if (medishield != null)
			medishield.cancel();
		character.moveLock = false;
		character.jumpLock = false;
		character.animationLock = false;
	}
	
}