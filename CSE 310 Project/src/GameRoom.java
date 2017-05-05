public class GameRoom {
	String[] board = new String[9];		// game board (3x3 grid)
	int numPlayers = 0;		// number of players (0, 1, or 2)
	int whosTurn = 0;		// active player (1 for Player 1, 2 for Player 2)
	Player playerOne;
	Player playerTwo;
	String gameid;		// 3-digit id of game room
	boolean firstTime;		// first move or not
	
	/**
	 * Default constructor.
	 */
	public GameRoom() {}
	
	/**
	 * Game Room constructor with Player 1 arguments.
	 */
	public GameRoom(Player playerOne, String gameid) {
		this.playerOne = playerOne;
		numPlayers = 1;
		for (int i = 0; i < 9; i++) board[i] = ".";
		this.gameid = gameid;
		firstTime = false;
	}
	
	/**
	 * Adds a second player to the game room.
	 * @param playerTwo
	 */
	public void addPlayerTwo(Player playerTwo) {
		this.playerTwo = playerTwo;
		numPlayers = 2;
		whosTurn = 1;
		firstTime = true;
	}
}