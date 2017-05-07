import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TCPServer implements Runnable {
	Socket socket;
	static ArrayList<Player> allPlayers = new ArrayList<Player>();		// list of all players (not
																		// necessarily logged in
	static ArrayList<GameRoom> allGames = new ArrayList<GameRoom>();	// list of all games, a game
																		// must have 2 players
	static int totalPlayers = 0;		// total number of players ever, needed for generating player ids
	static int totalGames = 0;		// total number of games ever, needed for generating game ids
	
	// socket constructor
	TCPServer(Socket socket) {
		this.socket = socket;
	}

	public static void main(String argv[]) throws Exception {
		// create welcoming socket at port 6789
		int port = 6789;
		if (argv.length >= 1 && argv[0] != null) port = Integer.parseInt(argv[0]);
		ServerSocket welcomeSocket = new ServerSocket(port);
		
		// wait for new connections, upon new connection create a new thread
		while (true) {
			Socket socket = welcomeSocket.accept();
			new Thread(new TCPServer(socket)).start();
	    }  
	}
	
	public void run() {
		try {
			String clientSentence;		// message received from client containing command
			String outputSentence;		// message we'll be sending to client
			boolean exited = false;		// allows me to exit gracefully (this is super ghetto)
			
			// create input stream attached to socket
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
			// create output stream attached to socket
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
			
			// create a player (client) and generate an id for him (000 + total number of players)
			Player newPlayer = new Player();
			if (totalPlayers < 10) newPlayer.id = "00" + Integer.toString(totalPlayers);
			else if (totalPlayers < 100) newPlayer.id = "0" + Integer.toString(totalPlayers);
			else newPlayer.id = Integer.toString(totalPlayers);
			outToClient.writeBytes(newPlayer.id + "\n");
			newPlayer.status = "Available";		// player is available by default
			newPlayer.out = outToClient;
			allPlayers.add(newPlayer);		// add the player to the list of players
			totalPlayers++;
			
			while (true) {
				// read in line from socket
				clientSentence = inFromClient.readLine();
						
				// if (clientSentence == null) break;
				outputSentence = "";
				
				/*
				 * The paragraph below causes Player 2's very first input after a game has been
				 * started with him to skip. This causes him to be put in a waiting (listening)
				 * state until Player 1 has made a move.
				 */
				String tempid = clientSentence.substring(0, 3);
				boolean firstTimeFound = false;
				for (int i = 0; i < allGames.size(); i++) {
					// find the player based on his id
					if (tempid.equals(allGames.get(i).playerTwo.id) && allGames.get(i).firstTime) {
						outputSentence = "firsttime" + allGames.get(i).playerOne.name + "\n";
						allGames.get(i).firstTime = false;		// no longer his first input
						firstTimeFound = true;
						// notifies player 1 that player 2 is ready
						allGames.get(i).playerOne.out.writeBytes("ready\n");
					}
				}
				
				if (firstTimeFound) {}		// if it's player 2's first input after game started, skip it
				else {
					// player issued the "help" command
					if (clientSentence.contains("help")) outputSentence = "help\n";
					// player tried to log in again after already being logged in
					else if (clientSentence.contains("loginerror")) {
						outputSentence = "You are already logged in.\n";
					}
					// player issued the "login" command
					else if (clientSentence.contains("login ")) {
						boolean alreadyInUse = false;
						outputSentence = "";
						
						// check if the username is already in use
						for (int i = 0; i < allPlayers.size(); i++) {
							if (clientSentence.substring(9).equals(allPlayers.get(i).name))
								alreadyInUse = true;
						}
						if (alreadyInUse) outputSentence = "Username already is use.\n";
						// username is free so save it to the player object
						else {
							for (int i = 0; i < allPlayers.size(); i++) {
								if (clientSentence.substring(0, 3).equals(allPlayers.get(i).id)) {
									allPlayers.get(i).name = clientSentence.substring(9);
									outputSentence = "Logged in as " + allPlayers.get(i).name + ".\n";
								}
							}
						}
					}
					// player issued the "who" command (only lists players who are logged in)
					else if (clientSentence.contains("who")) {
						// the format of the output is:
						// names[user] [status] [user] [status] ...
						outputSentence = "names";
						for (int i = 0; i < allPlayers.size(); i++) {
							if (!allPlayers.get(i).name.equals("")) {
								outputSentence = outputSentence + allPlayers.get(i).name + " "
										+ allPlayers.get(i).status + " ";
							}
						}
						// there are no players logged in
						if (outputSentence.equals("names")) outputSentence = "There are no current players.\n";
						else outputSentence += "\n";
					}
					// player issued the "games" command
					else if (clientSentence.contains("games")) {
						// the format of the output is:
						// games[gameid] [player1user] [player2user] [gameid] [player1user] [player2user] ...
						outputSentence = "games";
						for (int i = 0; i < allGames.size(); i++) {
							outputSentence = outputSentence + allGames.get(i).gameid + " "
									+ allGames.get(i).playerOne.name + " "
									+ allGames.get(i).playerTwo.name + " ";
						}
						// there are no games going on
						if (outputSentence.equals("games")) outputSentence = "There are no current games.\n";
						else outputSentence += "\n";
					}
					// player issued the "play" command
					else if (clientSentence.contains("play ")) {
						boolean playerAvailable = false;		// is the requested player available?
						boolean playerExists = false;		// does the requested player exist?
						boolean loggedIn = false;		// am I logged in?
						boolean alreadyInGame = false;		// is the requested player already in a game?
						boolean playYourself = false;		// am I requesting myself?
						
						outputSentence = "";
						String id = clientSentence.substring(0, 3);		// get player's id
					
						// check if I'm requesting myself
						for (int i = 0; i < allPlayers.size(); i++) {
							if (allPlayers.get(i).id.equals(id)) {
								if (!allPlayers.get(i).name.equals("")) loggedIn = true;
								if (allPlayers.get(i).name.equals(clientSentence.substring(8)))
									playYourself = true;
							}
						}
						// check if the requested player is already in a game
						for (int i = 0; i < allGames.size(); i++) {
							if (allGames.get(i).playerOne.id.equals(id)
									|| allGames.get(i).playerTwo.id.equals(id)) {
								alreadyInGame = true;
							}
						}
						// the requested player is logged in, available, and is not me
						if (loggedIn && !alreadyInGame && !playYourself) {
							for (int i = 0; i < allPlayers.size(); i++) {
								if (clientSentence.substring(8).equals(allPlayers.get(i).name))
									if (allPlayers.get(i).status.equals("Available")) {
										playerAvailable = true;		// player confirmed available
										playerExists = true;		// played confirmed to exist
										outputSentence = "Game with " + allPlayers.get(i).name
												+ " has begun.\n";
										id = clientSentence.substring(0, 3);
										Player p1 = new Player();
										Player p2 = allPlayers.get(i);
										// get a reference to myself as a player
										for (int j = 0; j < allPlayers.size(); j++) {
											if (allPlayers.get(j).id.equals(id)) p1 = allPlayers.get(j);
										}
										p1.status = "Busy";		// set both players to busy
										p2.status = "Busy";
										// generate the game id (000 + total number of games)
										String gameid = "";
										if (totalGames < 10) gameid = "00" + Integer.toString(totalGames);
										else if (totalGames < 100) gameid = "0" + Integer.toString(totalGames);
										else gameid = Integer.toString(totalGames);
										
										// create the game room with player 1
										GameRoom newGame = new GameRoom(p1, gameid);
										newGame.addPlayerTwo(p2);		// add player 2 to the game room
										totalGames++;
										allGames.add(newGame);		// add the game room to the list
										i = allPlayers.size();		// just to end of for loop
									// requested opponent exists, but is not available
									} else {
										playerExists = true;
										playerAvailable = false;
									}
							}
						}
						// appropriate response messages
						if (playYourself) outputSentence = "You can't play yourself.\n";
						else if (!loggedIn) outputSentence = "You must log in first.\n";
						else if (alreadyInGame) outputSentence = "You are already in a game.\n";
						else if (!playerExists) outputSentence = "Player does not exist.\n";
						else if (!playerAvailable) outputSentence = "Player is not currently available.\n";
					}
					// player issued the move ("place") command
					else if (clientSentence.contains("place ")) {
						outputSentence = "";
						boolean inGame = false;		// checks if the player is really in a game room
						String id = clientSentence.substring(0, 3);		// get player's id
						GameRoom currentGame = new GameRoom();		// create a new game room
						Player p1 = new Player();
						Player p2 = new Player();
						int playerNumber = 0;		// is it player 1 or 2 making the move?
						for (int i = 0; i < allGames.size(); i++) {
							// player 1 issued the command
							if (allGames.get(i).playerOne.id.equals(id)) {
								inGame = true;
								currentGame = allGames.get(i);		// get a reference to the game
								playerNumber = 1;
								p1 = allGames.get(i).playerOne;		// get a reference to the players
								p2 = allGames.get(i).playerTwo;
							// player 2 issued the command
							} else if (allGames.get(i).playerTwo.id.equals(id)) {
								inGame = true;
								currentGame = allGames.get(i);		// get a reference to the game
								playerNumber = 2;
								p1 = allGames.get(i).playerOne;		// get a reference to the players
								p2 = allGames.get(i).playerTwo;
							}
						}
						if (inGame) {		// the player is really in a game room
							int value = Integer.parseInt(clientSentence.substring(9));
							// check if it's the player's turn
							if (playerNumber != currentGame.whosTurn) {
								outputSentence = "It's not your turn.\n";
							}
							// check if the cell is valid (1 through 9)
							else if (value < 1 || value > 9) outputSentence = "Invalid cell.\n";
							// so far so good
							else {
								// check if cell is already occupied
								if (!currentGame.board[value - 1].equals("."))
									outputSentence = "Cell already occupied.\n";
								// keep going...
								else {
									// if player 1 is making the move, place an X
									if (currentGame.whosTurn == 1) {
										currentGame.board[value - 1] = "X";
										currentGame.whosTurn = 2;
									}
									// if player 2 is making the move, place an O
									else {
										currentGame.board[value - 1] = "O";
										currentGame.whosTurn = 1;
									}
											
									boolean gameOver = false;		// check if the game is over
											
									// check for winning state for player 1 or player 2
									if ((currentGame.board[0].equals("X") && currentGame.board[1].equals("X") && currentGame.board[2].equals("X"))
											|| (currentGame.board[3].equals("X") && currentGame.board[4].equals("X") && currentGame.board[5].equals("X"))
											|| (currentGame.board[6].equals("X") && currentGame.board[7].equals("X") && currentGame.board[8].equals("X"))
											|| (currentGame.board[0].equals("X") && currentGame.board[3].equals("X") && currentGame.board[6].equals("X"))
											|| (currentGame.board[1].equals("X") && currentGame.board[4].equals("X") && currentGame.board[7].equals("X"))
											|| (currentGame.board[2].equals("X") && currentGame.board[5].equals("X") && currentGame.board[8].equals("X"))
											|| (currentGame.board[0].equals("X") && currentGame.board[4].equals("X") && currentGame.board[8].equals("X"))
											|| (currentGame.board[2].equals("X") && currentGame.board[4].equals("X") && currentGame.board[6].equals("X"))) {
										outputSentence = "Player 2 is the winner.\n";
										gameOver = true;
									} else if ((currentGame.board[0].equals("O") && currentGame.board[1].equals("O") && currentGame.board[2].equals("O"))
											|| (currentGame.board[3].equals("O") && currentGame.board[4].equals("O") && currentGame.board[5].equals("O"))
											|| (currentGame.board[6].equals("O") && currentGame.board[7].equals("O") && currentGame.board[8].equals("O"))
											|| (currentGame.board[0].equals("O") && currentGame.board[3].equals("O") && currentGame.board[6].equals("O"))
											|| (currentGame.board[1].equals("O") && currentGame.board[4].equals("O") && currentGame.board[7].equals("O"))
											|| (currentGame.board[2].equals("O") && currentGame.board[5].equals("O") && currentGame.board[8].equals("O"))
											|| (currentGame.board[0].equals("O") && currentGame.board[4].equals("O") && currentGame.board[8].equals("O"))
											|| (currentGame.board[2].equals("O") && currentGame.board[4].equals("O") && currentGame.board[6].equals("O"))) {
										outputSentence = "Player 1 is the winner.\n";
										gameOver = true;
									// by default, the game is a draw
									} else {
										outputSentence = "It's a draw.\n";
										gameOver = true;
										
										// check for draw (every cell is filled)
										for (int i = 0; i < 9; i++) {
											if (currentGame.board[i].equals(".")) gameOver = false;
										}
									}
									
									// game is not over so just send the board state
									if (!gameOver) {
										// format of the response message is:
										// board[cell1][cell2][cell3]...[cell9]
										outputSentence = "board";
										for (int i = 0; i < 9; i++) {
											outputSentence += currentGame.board[i];
										}
										outputSentence += "\n";
										// prints the game over message to the waiting player as well
										if (currentGame.whosTurn == 1) p1.out.writeBytes(outputSentence);
										else p2.out.writeBytes(outputSentence);
									// game is over so print game over message and delete room
									} else {
										p1.status = "Available";		// set both players to available
										p2.status = "Available";
										String result = "";
										// take note of the winner (or draw)
										if (outputSentence.contains("Player 1")) result = "p1";
										else if (outputSentence.contains("Player 2")) result = "p2";
										else result = "dw";
										// format of the response message is:
										// board[cell1][cell2][cell3]...[cell9][result]
										outputSentence = "board";
										for (int i = 0; i < 9; i++) {
											outputSentence += currentGame.board[i];
										}
										// delete the finished game
										for (int i = 0; i < allGames.size(); i++) {
											if (allGames.get(i).gameid.equals(currentGame.gameid))
												allGames.remove(i);
										}
										outputSentence = outputSentence + result;
										// take note of the winner (or draw)
										if (result.equals("p1"))
											outputSentence = outputSentence + p1.name + "\n";
										else if (result.equals("p2"))
											outputSentence = outputSentence + p2.name + "\n";
										// prints the game over message to the waiting player as well 
										if (currentGame.whosTurn == 1) p1.out.writeBytes(outputSentence);
										else p2.out.writeBytes(outputSentence);
									}
								}
							}
						} else {
							// something went wrong
							outputSentence = "You must start a game first.\n";
						}
					}
					// player issued the "exit" command
					else if (clientSentence.contains("exit")) {
						outputSentence = "exit\n";
						tempid = clientSentence.substring(0, 3);		// retrieve the player's id
						Player pl1 = new Player();
						Player pl2 = new Player();
						for (int i = 0; i < allGames.size(); i++) {
							if (tempid.equals(allGames.get(i).playerOne.id)
									|| tempid.equals(allGames.get(i).playerTwo.id)) {
								pl1 = allGames.get(i).playerOne;		// get a reference to both players
								pl2 = allGames.get(i).playerTwo;
								allGames.remove(i);		// delete the game room
								pl1.status = "Available";		// set both players as available
								pl2.status = "Available";
								// inform your opponent you have exited
								if (tempid == pl1.id) {
									pl1.out.writeBytes(pl2.name + " has exited.\n");
									for (int j = 0; j < allPlayers.size(); j++) {
										// remove the player who left from the players list
										if (allPlayers.get(i).id.equals(tempid)) allPlayers.remove(i);
									}
								}
								else {
									pl2.out.writeBytes(pl1.name + " has exited.\n");
									for (int j = 0; j < allPlayers.size(); j++) {
										// remove the player who left from the players list
										if (allPlayers.get(i).id.equals(tempid)) allPlayers.remove(i);
									}
								}
							}
						}
						// remove the player who left from the players list (this might be redundant)
						for (int i = 0; i < allPlayers.size(); i++) {
							if (allPlayers.get(i).id.equals(tempid)) allPlayers.remove(i);
						}
						exited = true;
					}
					// player issued an invalid command
					else outputSentence = "Invalid command.\n";
				}
				
				// write out line to socket
				outToClient.writeBytes(outputSentence);
				
				if (exited) {
					while (true) {}
				}
			}
		} catch (IOException e) {
			System.out.println(e);
	    }
	}
}