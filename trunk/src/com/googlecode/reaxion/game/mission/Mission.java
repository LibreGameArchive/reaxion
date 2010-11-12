package com.googlecode.reaxion.game.mission;

import java.util.ArrayList;

import com.googlecode.reaxion.game.state.StageGameState;
import com.jmex.game.state.GameState;

/**
 * Represents an in-game mission. All specific missions should extend this class.
 *
 * @author Brian Clanton
 */

public abstract class Mission implements Comparable<Mission> {
	
	private String title;
	private int missionID;
	private int difficultyRating;
	private boolean required;
	private String imageURL;
	
	private ArrayList<GameState> states;
	
	private int stateCount;
	
	public Mission() {
		title = "???";
		missionID = 0;
		difficultyRating = 0;
		required = false;
		states = new ArrayList<GameState> ();
		stateCount = 0;
	}
	
	public Mission(String title, int missionID, int difficultyRating, boolean required, String imageURL) {
		this.title = title;
		this.missionID = missionID;
		this.difficultyRating = difficultyRating;
		this.required = required;
		this.imageURL = imageURL;
		states = new ArrayList<GameState> ();
		stateCount = states.size();
	}
	
	public int compareTo(Mission m) {
		return m.getMissionID() - missionID;
	}

	/**
	 * Set up all states for this mission. Override to add content.
	 */
	public void init() {
		
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMissionID() {
		return missionID;
	}

	public void setMissionID(int missionID) {
		this.missionID = missionID;
	}

	public int getDifficultyRating() {
		return difficultyRating;
	}

	public void setDifficultyRating(int difficultyRating) {
		this.difficultyRating = difficultyRating;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public void setStates(ArrayList<GameState> states) {
		this.states = states;
		stateCount = states.size();
	}
	
	public void addState(GameState g) {
		states.add(g);
		stateCount++;
	}
	
	public GameState getStateAt(int index) {
		return states.get(index);
	}
	
	public void activateStateAt(int index) {
		states.get(index).setActive(true);
		if(states.get(index) instanceof StageGameState)
			((StageGameState) states.get(index)).startBGM();
	}
	
	public void deactivateStateAt(int index) {
		states.get(index).setActive(false);
	}

	public int getStateCount() {
		return stateCount;
	}
}