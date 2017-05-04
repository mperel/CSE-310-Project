public class GameRoom {
	String[] board = new String[9];
	int numPlayers = 0;
	int whosTurn = 0;
	Player playerOne;
	Player playerTwo;
	String gameid;
	boolean firstTime;
	
	public GameRoom() {}
	
	public GameRoom(Player playerOne, String gameid) {
		this.playerOne = playerOne;
		numPlayers = 1;
		for (int i = 0; i < 9; i++) board[i] = ".";
		this.gameid = gameid;
		firstTime = false;
	}
	
	public void addPlayerTwo(Player playerTwo) {
		this.playerTwo = playerTwo;
		numPlayers = 2;
		whosTurn = 1;
		firstTime = true;
	}
}