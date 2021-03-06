package com.googlecode.reaxion.game.state;

import com.googlecode.reaxion.game.input.bindings.CharacterSelectionOverlayBindings;
import com.googlecode.reaxion.game.input.bindings.HubGameStateBindings;
import com.googlecode.reaxion.game.input.bindings.MenuBindings;
import com.googlecode.reaxion.game.mission.MissionManager;
import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.character.Monica;
import com.googlecode.reaxion.game.model.prop.Pointer;
import com.googlecode.reaxion.game.overlay.CharacterSelectionOverlay;
import com.googlecode.reaxion.game.overlay.InfoOverlay;
import com.googlecode.reaxion.game.overlay.MenuOverlay;
import com.googlecode.reaxion.game.overlay.MissionOverlay;
import com.googlecode.reaxion.game.overlay.Overlay;
import com.googlecode.reaxion.game.overlay.SaveOverlay;
import com.googlecode.reaxion.game.overlay.StageSelectionOverlay;
import com.googlecode.reaxion.game.overlay.StatsOverlay;
import com.googlecode.reaxion.game.overlay.TerminalOverlay;
import com.googlecode.reaxion.game.util.Battle;
import com.googlecode.reaxion.game.util.LoadingQueue;
import com.googlecode.reaxion.game.util.SaveManager;
import com.jme.input.KeyBindingManager;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jmex.angelfont.BitmapFont;
import com.jmex.game.state.GameStateManager;

/**
 * {@code HubGameState} extends {@code StageGameState} with functionality
 * dedicated to the hub system, such as portals and hub controllers.
 * 
 * @author Khoa
 */
public class HubGameState extends StageGameState {
	
	public static final String NAME = "hubGameState";
	
	private Model terminal;
	private Pointer pointer;
	
	private Node holderNode;
	
	private TerminalOverlay terminalOverlay;
	private CharacterSelectionOverlay characterOverlay;
	private StageSelectionOverlay stageOverlay;
	private MissionOverlay missionOverlay;
	private MenuOverlay currentMenu;
	private StatsOverlay statsOverlay;
	private SaveOverlay saveOverlay;
	
	private final float activationDistance = 10f;
	
	private boolean menuShowing;
	
	private String action = "";
	private String saveName = "SaveState";
    
    public HubGameState() {
    	super();
    }
    
    public HubGameState(Battle b) {
    	super(b);
    	statsOverlay = new StatsOverlay(player.info, partner.info);
    	createTerminal(b.getStage().getTerminalPosition());
    }
    
    @Override
    protected void init() {
    	super.init();
    	
    	setName(NAME);
    	
    	holderNode = new Node("holderNode");
    	rootNode.attachChild(holderNode);
    	
    	startsBGM = true;
    	endsBGM = true;
    	
    	terminalOverlay = new TerminalOverlay();
    	characterOverlay = new CharacterSelectionOverlay(false);
    	stageOverlay = new StageSelectionOverlay(false);
    	missionOverlay = new MissionOverlay();
    	saveOverlay = new SaveOverlay(this);
    	
    	changeCurrentMenu(terminalOverlay);
    	
    	rootNode.updateRenderState();
    }
    
    @Override
	public void stateUpdate(float tpf) {
		super.stateUpdate(tpf);
		
		boolean terminalAccessed = false;
		
		Vector3f playerLoc = player.getXZTranslation();
		Vector3f terminalLoc = terminal.getXZTranslation();
		boolean withinRange = playerLoc.distance(terminalLoc) <= activationDistance;
		pointer.show(withinRange);
		
		//  check queue actions
		if (action == "stage") {
			Battle.setDefaultStage(((StageSelectionOverlay) currentMenu).getSelectedStageClass());
			
			MissionManager.endHubGameState();
			MissionManager.startHubGameState();
			
		} else if (action == "char") {
			String[] characters = ((CharacterSelectionOverlay) currentMenu).getSelectedChars(true);
			
			Battle.setDefaultPlayers(characters[0], characters[1]);
			
			MissionManager.endHubGameState();
			MissionManager.startHubGameState();
		}
		
		KeyBindingManager manager = KeyBindingManager.getKeyBindingManager();
		
		if (manager.isValidCommand(HubGameStateBindings.ACCESS_TERMINAL.toString(), false) && !menuShowing) {
			terminalAccessed = true;
			
			if (withinRange) {
				toggleMenu(true);
				changeCurrentMenu(terminalOverlay);
				holderNode.attachChild((Overlay) currentMenu);
			}
		}
		
		if (manager.isValidCommand(MenuBindings.SELECT_FINAL.toString(), false) && !terminalAccessed) {
			if (currentMenu instanceof MissionOverlay) {
				missionOverlay.startSelectedMission();
			} else if (currentMenu instanceof TerminalOverlay) {
				holderNode.detachChild(currentMenu);
				
				switch (((TerminalOverlay) currentMenu).getCurrentIndex()) {
				case 0:
					changeCurrentMenu(characterOverlay);
					break;
				case 1:
					changeCurrentMenu(stageOverlay);
					break;
				case 2:
					changeCurrentMenu(missionOverlay);
					break;
				case 3:
					changeCurrentMenu(statsOverlay);
					break;
				case 4:
					switchToBurstGrid();
					break;
				case 5:
					changeCurrentMenu(saveOverlay);
				case 6:
					//SaveManager.loadGame("SaveState");
				}
				
				((Overlay) currentMenu).updateRenderState();
				holderNode.attachChild((Overlay) currentMenu);
				toggleMenu(true);
			} else if (currentMenu instanceof StageSelectionOverlay) {
				loading.show(true);
				action = "stage";
				return;
				
			} else if (currentMenu instanceof CharacterSelectionOverlay) {
				// check selection
				String[] characters = ((CharacterSelectionOverlay) currentMenu).getSelectedChars(true);
				if (characters == null)
					return;
				
				loading.show(true);
				action = "char";
				return;
				
			} else if (currentMenu instanceof SaveOverlay) {
				// assign new item
				SaveManager.saveGame(this, ((SaveOverlay)currentMenu).savesList.getSelectedEntry(true));
				info.alert("Game saved sucessfully!", BitmapFont.Align.Right, 60, 1);
				
				// close terminal
				toggleMenu(false);
				holderNode.detachChild((Overlay) currentMenu);

				return;				
			}
			
		}
		
		if (manager.isValidCommand(HubGameStateBindings.CLOSE_TERMINAL.toString(), false) && menuShowing) {
			
			if (playerLoc.distance(terminalLoc) <= activationDistance) {
				toggleMenu(false);
				holderNode.detachChild((Overlay) currentMenu);
			}
			
			// refresh attacks and abilities
			if (currentMenu instanceof StatsOverlay) {
				Battle b = Battle.getCurrentBattle();
				b.loadAttributes();
				assignTeam(b.getP1(), b.getP1Attacks(), b.getP2(), b.getP2Attacks());  
			}
		}
		
		if (manager.isValidCommand(MenuBindings.UP.toString(), false)) {
			if (menuShowing)
				currentMenu.updateDisplay(MenuBindings.UP);
		}
		
		if (manager.isValidCommand(MenuBindings.DOWN.toString(), false)) {
			if (menuShowing)
				currentMenu.updateDisplay(MenuBindings.DOWN);
		}
		
		if (manager.isValidCommand(MenuBindings.LEFT.toString(), false)) {
			if (menuShowing)
				currentMenu.updateDisplay(MenuBindings.LEFT);
		}
		
		if (manager.isValidCommand(MenuBindings.RIGHT.toString(), false)) {
			if (menuShowing)
				currentMenu.updateDisplay(MenuBindings.RIGHT);
		}
		
		if (manager.isValidCommand(MenuBindings.SELECT_ITEM.toString(), false)) {
			if (menuShowing)
				currentMenu.updateDisplay(MenuBindings.SELECT_ITEM);
		}
		
		if (manager.isValidCommand(CharacterSelectionOverlayBindings.UNDO_CHOICE.toString(),
				false)) {
			currentMenu.updateDisplay(CharacterSelectionOverlayBindings.UNDO_CHOICE);
		}
		
		if (menuShowing && currentMenu instanceof CharacterSelectionOverlay) {
			if (manager.isValidCommand(CharacterSelectionOverlayBindings.CHOOSE_1.toString(), false))
				currentMenu.updateDisplay(CharacterSelectionOverlayBindings.CHOOSE_1);
			if (manager.isValidCommand(CharacterSelectionOverlayBindings.CHOOSE_2.toString(), false))
				currentMenu.updateDisplay(CharacterSelectionOverlayBindings.CHOOSE_2);
		
		} else if (menuShowing  & currentMenu instanceof StatsOverlay) {
		if (manager.isValidCommand(MenuBindings.CHOOSE_1.toString(), false))
			currentMenu.updateDisplay(MenuBindings.CHOOSE_1);
		if (manager.isValidCommand(MenuBindings.CHOOSE_2.toString(), false))
			currentMenu.updateDisplay(MenuBindings.CHOOSE_2);
		if (manager.isValidCommand(MenuBindings.CHOOSE_3.toString(), false))
			currentMenu.updateDisplay(MenuBindings.CHOOSE_3);
		if (manager.isValidCommand(MenuBindings.CHOOSE_4.toString(), false))
			currentMenu.updateDisplay(MenuBindings.CHOOSE_4);
		if (manager.isValidCommand(MenuBindings.CHOOSE_5.toString(), false))
			currentMenu.updateDisplay(MenuBindings.CHOOSE_5);
		if (manager.isValidCommand(MenuBindings.CHOOSE_6.toString(), false))
			currentMenu.updateDisplay(MenuBindings.CHOOSE_6);
		}
		
	}

    private void toggleMenu(boolean showing) {				
		frozen = showing;
    	menuShowing = showing;
    	
    	if (menuShowing) 
    		hideOverlays();
    	else
    		showOverlays();
    }
    
    private void changeCurrentMenu(MenuOverlay m) { 
    	if (m instanceof StageSelectionOverlay)
    		((StageSelectionOverlay) m).setSelectedStage(getStage().name);
    	
    	currentMenu = m;
    }
    
    private void switchToBurstGrid() {
    	// The following check is temporary
    	if (player instanceof Monica) {
    		MissionManager.endHubGameState();
    		BurstGridGameState bggs = new BurstGridGameState(player.info);
    		bggs.setActive(true);
    		GameStateManager.getInstance().attachChild(bggs);
    	} else {
    		info.alert("You can only access the burst grid with Monica.", BitmapFont.Align.Right, 100, 1);
    	}
    }
    
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		
		if (active) {
			player.renew();
			partner.renew();
		}
	}

	@Override
    protected void act() {
    	Vector3f p = player.model.getLocalTranslation();
    	Vector3f t = terminal.model.getLocalTranslation();
    	terminal.roll = FastMath.atan2(p.x-t.x, p.z-t.z);
    	terminal.rotate();
    }
    
    private void createTerminal(Vector3f pos) {
    	terminal = LoadingQueue.quickLoad(new Model("dashboard"), this);
    	terminal.trackable = true;
    	terminal.model.setLocalTranslation(pos);
    	pointer = (Pointer)LoadingQueue.quickLoad(new Pointer(terminal), this);
    	rootNode.updateRenderState();
    }
    
    public String getSaveName(){
    	return saveName;
    }

}