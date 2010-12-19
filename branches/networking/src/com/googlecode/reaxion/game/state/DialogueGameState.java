package com.googlecode.reaxion.game.state;

import java.util.List;
import java.util.logging.Logger;

import com.googlecode.reaxion.game.Reaxion;
import com.googlecode.reaxion.game.audio.BgmPlayer;
import com.googlecode.reaxion.game.mission.MissionManager;
import com.googlecode.reaxion.game.overlay.DialogueOverlay;
import com.googlecode.reaxion.game.util.Actor;
import com.jme.app.AbstractGame;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jmex.game.state.CameraGameState;
import com.jmex.game.state.GameStateManager;

/**
 * {@code DialogueGameState} provides an interface for text-based cutscenes.
 * 
 * @author Khoa
 */
public class DialogueGameState extends CameraGameState {

	public static final String NAME = "dialogueGameState";

	private static final Logger logger = Logger
			.getLogger(DialogueGameState.class.getName());

	public float tpf;

	private DialogueOverlay dialogueNode;

	protected InputHandler input;

	protected AbstractGame game = null;

	private String bg;

	private final int textSpeed = 2;
	private final int textWidth = 760;
	/**
	 * Current sub-time for current text block.
	 */
	private int count = 0;
	/**
	 * Index of current text block.
	 */
	private int current = 0;
	private List<String> reserveText;
	private String currentText = "";

	/**
	 * Contains the text block for each index.
	 */
	private String[] strings;
	/**
	 * Contains the header labels for each index.
	 */
	private String[] names;
	/**
	 * Contains the duration in frames for each index. {@code 0} denotes no
	 * limit, skipping is available. {@code -1} denotes no skipping until text
	 * finishes.
	 */
	private int[] durations;
	/**
	 * Contains all the {@code Actors} for this cutscene.
	 */
	private Actor[] actors;

	public DialogueGameState(String[] s, int[] d, Actor[] a, String b) {
		super(NAME);
		parseInput(s);
		durations = d;
		actors = a;
		bg = b;
		init();
	}

	private void init() {
		rootNode = new Node("RootNode");

		// Prepare dialogue node
		dialogueNode = new DialogueOverlay();
		rootNode.attachChild(dialogueNode);

		// Fix up the camera, will not be needed for final camera controls
		Vector3f loc = new Vector3f(0.0f, 2.5f, 10.0f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, -1.0f);
		cam.setFrame(loc, left, up, dir);
		cam.setFrustumPerspective(45f, (float) DisplaySystem.getDisplaySystem()
				.getWidth()
				/ DisplaySystem.getDisplaySystem().getHeight(), .01f, 1000);
		cam.update();

		// Initial InputHandler
		// input = new FirstPersonHandler(cam, 15.0f, 0.5f);
		input = new InputHandler();
		initKeyBindings();

		// Make the mouse visible
		MouseInput.get().setCursorVisible(true);

		// Finish up
		rootNode.updateRenderState();
		rootNode.updateWorldBound();
		rootNode.updateGeometricState(0.0f, true);

		// Set the background
		dialogueNode.setBg(bg);

		// Setup actors
		dialogueNode.setupActors(actors.length);
		updateActors();

		// Prepare the text
		loadText(current);
	}

	// duplicate the functionality of DebugGameState
	// Most of this can be commented out during finalization
	private void initKeyBindings() {
		KeyBindingManager.getKeyBindingManager().set("screen_shot",
				KeyInput.KEY_F1);
		KeyBindingManager.getKeyBindingManager().set("exit",
				KeyInput.KEY_ESCAPE);
		KeyBindingManager.getKeyBindingManager().set("mem_report",
				KeyInput.KEY_R);
		KeyBindingManager.getKeyBindingManager().set("toggle_mouse",
				KeyInput.KEY_M);
		KeyBindingManager.getKeyBindingManager().set("return",
				KeyInput.KEY_RETURN);
	}

	@Override
	public void stateUpdate(float _tpf) {
		tpf = _tpf;

		// Update the InputHandler
		if (input != null) {
			input.update(tpf);

			/** If exit is a valid command (via key Esc), exit game */
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit",
					false)) {
				if (game != null) {
					game.finish();
				} else {
					Reaxion.terminate();
				}
			}
		}

		// Update the geometric state of the rootNode
		rootNode.updateGeometricState(tpf, true);

		if (input != null) {
			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
					"return", false)) {
				// check the duration limit
				if (count >= durations[current]) {

					// if text is done
					if (reserveText == null) {
						current++;
						if (current < strings.length)
							loadText(current);
						else
							returnToCharSelectState();

					} else if (durations[current] >= 0) {
						// if allowed, skip the scrolling
						while (reserveText != null)
							add();
					}
				}
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
					"screen_shot", false)) {
				DisplaySystem.getDisplaySystem().getRenderer().takeScreenShot(
						"SimpleGameScreenShot");
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
					"mem_report", false)) {
				long totMem = Runtime.getRuntime().totalMemory();
				long freeMem = Runtime.getRuntime().freeMemory();
				long maxMem = Runtime.getRuntime().maxMemory();

				logger.info("|*|*|  Memory Stats  |*|*|");
				logger.info("Total memory: " + (totMem >> 10) + " kb");
				logger.info("Free memory: " + (freeMem >> 10) + " kb");
				logger.info("Max memory: " + (maxMem >> 10) + " kb");
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
					"toggle_mouse", false)) {
				MouseInput.get().setCursorVisible(
						!MouseInput.get().isCursorVisible());
				logger.info("Cursor Visibility set to "
						+ MouseInput.get().isCursorVisible());
			}
		}

		// Update the dialogue node
		if (count % textSpeed == 0) {
			add();
			dialogueNode.setText(currentText);
		}
		updateActors();
		dialogueNode.showArrow(current < durations.length
				&& (durations[current] >= 0 || reserveText == null)
				&& count >= durations[current]);
		// System.out.println(count+" "+durations[current]);
		count++;
	}

	/**
	 * Parses input array for name labels and text, separated by "::".
	 * 
	 * @param str
	 */
	private void parseInput(String[] s) {
		names = new String[s.length];
		strings = new String[s.length];

		for (int i = 0; i < s.length; i++) {
			String[] p = s[i].split("::");
			if (p.length > 1)
				names[i] = p[0].trim();
			strings[i] = p[p.length - 1].trim();
		}
	}

	/**
	 * Updates {@code Actor}s' positions and portraits.
	 */
	private void updateActors() {
		for (int i = 0; i < actors.length; i++) {
			String p = actors[i].getPortrait(current, count);
			if (p != null)
				dialogueNode.loadPortrait(i, p);
			dialogueNode.movePortrait(i, actors[i].getPosition(current, count,
					dialogueNode.getPoint(i)));

			// System.out.println(current+" "+count+" : "+p+" / "+actors[i].getPosition(current,
			// count, dialogueNode.getPoint(i)));
		}
	}

	int curChar;

	/**
	 * Load the text block denoted by {@code ind} and changes the {@code
	 * dialogueNode} accordingly.
	 * 
	 * @param ind
	 *            index to change to
	 */
	private void loadText(int ind) {
		// reserveText = Arrays.asList(strings[ind].split(" "));
		// reserveText = strings[ind];
		currentText = "";
		dialogueNode.setName(names[ind]);
		dialogueNode.setText(currentText);
		count = 0;

		curChar = 0;
	}

	/**
	 * Add to the current text block, iterating over the current index's string.
	 * Characters are added one at a time or in conjunction with spaces/line
	 * breaks. If the current word is complete, the next word is deployed. When
	 * all words have been added, the reserve is emptied.
	 */
	/*
	 * private void add() { // if there is text left to add if (reserveText !=
	 * null) {
	 * 
	 * String first = reserveText.get(0);
	 * 
	 * // add the next letter String nextChar = first.substring(0, 1);
	 * currentText += nextChar;
	 * 
	 * first.
	 * 
	 * // subtract from the current reserved word if (first.length() > 1) {
	 * first = first.substring(1); } else { // shift to the next word if
	 * (reserveText.length > 1) { reserveText = Arrays.copyOfRange(reserveText,
	 * 1, reserveText.length); if
	 * (dialogueNode.testText(currentText+reserveText[0]) > textWidth)
	 * currentText += "\n"; else currentText += " "; } else { // empty the
	 * reserve reserveText = null; } } } }
	 */

	private void add() {
		if (current < strings.length)
			if (curChar < strings[current].length())
				currentText += strings[current].charAt(curChar++);

	}

	private void returnToCharSelectState() {
		if (MissionManager.hasCurrentMission())
			MissionManager.startNext();
		else {
			GameStateManager.getInstance().getChild(
					CharacterSelectionState.NAME).setActive(true);
			setActive(false);
			BgmPlayer.stopAndReset();
			GameStateManager.getInstance().detachChild(this);
		}
	}

	@Override
	public void cleanup() {
	}
}