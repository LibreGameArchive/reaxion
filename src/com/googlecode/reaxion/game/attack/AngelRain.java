package com.googlecode.reaxion.game.attack;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.attackobject.AngelSword;
import com.googlecode.reaxion.game.state.BattleGameState;
import com.googlecode.reaxion.game.util.LoadingQueue;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

/**
 * Fires an energy bullet towards the target
 */
public class AngelRain extends Attack {
	
	private static final int height = 48;
	private static final int riseSpeed = 1;
	private static final int minRadius = 1;
	private static final int maxRadius = 30;
	private static final int delay = 8;
	private static final int maxSwords = 32;
	
	private AngelSword as;
	private boolean oldGravity;
	
	public AngelRain() {
		name = "Angel Rain";
		gaugeCost = 18;
	}
	
	public AngelRain(AttackData ad) {
		super(ad, 18);
		name = "Angel Rain";
	}
	
	@Override
	public void load() {
		LoadingQueue.push(new Model(AngelSword.filename));
	}
	
	@Override
	public void firstFrame(BattleGameState b) {
		character.moveLock = true;
		character.jumpLock = true;
		character.animationLock = true;
		
		oldGravity = character.gravitate;
		character.gravitate = false;
		character.setVelocity(new Vector3f(0, 0, 0));
		character.play("raiseUp", b.tpf);
	}
	
	@Override
	public void nextFrame(BattleGameState b) {
		// if risen to the right height and animation is through
		if (phase == 0 && character.play("raiseUp", b.tpf)) {
			character.play("jump", b.tpf);
			phase++;
		} else if (phase == 1) {
			if (character.model.getWorldTranslation().y >= height && character.play("jump", b.tpf)) {
				character.setVelocity(new Vector3f(0, 0, 0));
				character.play("raiseDown", b.tpf);
				phase++;
			} else {
				character.setVelocity(new Vector3f(0, riseSpeed, 0));
			}
		} else if (phase > 1 && phase < maxSwords+2 && frameCount % delay == 0) {
			
			// determine spawn point
			float r = FastMath.nextRandomFloat()*(maxRadius - minRadius) + minRadius;
			float a = FastMath.nextRandomFloat()*FastMath.PI*2;
			Vector3f translation = new Vector3f(r*FastMath.sin(a), 4, r*FastMath.cos(a));
			
			as = (AngelSword)LoadingQueue.quickLoad(new AngelSword(getUsers()), b);

			as.model.setLocalTranslation(character.model.getWorldTranslation().add(translation));
			
			b.getRootNode().updateRenderState();
			phase++;
			
		} else if (phase >= maxSwords+1) {
			character.play("jump", b.tpf);
			character.setVelocity(new Vector3f(0, -riseSpeed, 0));
			if (character.model.getWorldTranslation().y <= 0) {
				character.gravitate = oldGravity;
				finish();
			}	
		}
	}
	
	@Override
	public void interrupt(BattleGameState b, Model other) {
		// negate flinch, this attack cannot be interrupted
        character.hp -= other.damagePerFrame/2;
	}
	
	@Override
	public void finish() {
		super.finish();
		character.moveLock = false;
		character.jumpLock = false;
		character.animationLock = false;
	}
	
}
