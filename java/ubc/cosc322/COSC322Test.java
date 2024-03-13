//get the idea from: https://github.com/OKThomas1/cosc322-project
package ubc.cosc322;

import java.util.Map;
import sfs2x.client.entities.Room;
import java.util.ArrayList;
import java.util.List;

import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.HumanPlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Test extends GamePlayer{
	private int chessType;
    private GameClient gameClient = null; 
    private BaseGameGUI gamegui = null;
	
    private String userName = null;
    private String passwd = null;

	private AI aiplayer = null;
 
	
    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {	
		String arg0 = "test" + Math.random();
		String arg1 = "test" + Math.random();			 
		if(args.length > 0){
			arg0 = args[0];
			arg1 = args[1];
		}
    	COSC322Test player2 = new COSC322Test(arg0, arg1);
    	
    	if(player2.getGameGUI() == null) {
    		player2.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                	player2.Go();
                }
            });
    	}
		

    }
	
    /**
     * Any name and passwd 
     * @param userName
      * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
    	this.userName = userName;
    	this.passwd = passwd;
    	
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
    	//this.gamegui = new BaseGameGUI(this);

		this.gamegui = new BaseGameGUI(this);
    }
 


    @Override
    public void onLogin() {
    	System.out.println("Congratualations!!! "
    			+ "I am called because the server indicated that the login is successfully");
    	System.out.println("The next step is to find a room and join it: "
    			+ "the gameClient instance created in my constructor knows how!");
		userName = gameClient.getUserName();
		List<Room> rooms = this.gameClient.getRoomList();
    	for(Room room:rooms) {
    		System.out.println(room);
    	}
		if(gamegui != null) {
			gamegui.setRoomInformation(gameClient.getRoomList());
		}
    }

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    	//This method will be called by the GameClient when it receives a game-related message
    	//from the server.
	
    	//For a detailed description of the message types and format, 
    	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
	
		System.out.println("Receiving message of type " + messageType);
if(messageType.equals(GameMessage.GAME_STATE_BOARD)){
  @SuppressWarnings("unchecked")
ArrayList<Integer> board = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
  getGameGUI().setGameState(board); 
  chessType = 1; 	// Black
  //chessType = 2; 	// White
  this.aiplayer = new AI(board, chessType);  	//activate the AI player. 
  //this.aiplayer = new AI(board, 2);
}

if(chessType == 1 && messageType.equals(GameMessage.GAME_ACTION_START)) {	//make the first move when we are black.
	ArrayList<Integer> QCurr = new ArrayList<Integer>(); 
	ArrayList<Integer> QNew = new ArrayList<Integer>();
	ArrayList<Integer> Arrow = new ArrayList<Integer>();
	QCurr.add(10); 
	QCurr.add(7);
	QNew.add(2);
	QNew.add(4); 
	Arrow.add(1);
	Arrow.add(5);
	Map<String, Object> createMove = this.aiplayer.createMove(QCurr, QNew, Arrow); 
	System.out.println(createMove);
	gameClient.sendMoveMessage(createMove);
	this.aiplayer.updateGameState(createMove, true);
	getGameGUI().updateGameState(createMove);
	  }

if(messageType.equals(GameMessage.GAME_ACTION_MOVE)){	
  getGameGUI().updateGameState(msgDetails);
  this.aiplayer.updateGameState(msgDetails, false);
  Map<String, Object> nextMove = this.aiplayer.calculateNextMove();	//make the next best move. 
  if(nextMove == null){	//if cannot make the next move, we lose automatically. 
    System.out.println("SURRENDER");
    return false;
  }
  System.out.println(nextMove.toString());
  gameClient.sendMoveMessage(nextMove);
  this.aiplayer.updateGameState(nextMove, true);
  getGameGUI().updateGameState(nextMove);
}
   	return true;   	
    }
    
    
    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		// TODO Auto-generated method stub
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		// TODO Auto-generated method stub
		return this.gamegui;
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
    	gameClient = new GameClient(userName, passwd, this);			
	}

 
}//end of class