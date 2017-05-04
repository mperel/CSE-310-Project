import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TCPServer implements Runnable {
	Socket socket;
	static ArrayList<Player> allPlayers = new ArrayList<Player>();
	static ArrayList<GameRoom> allGames = new ArrayList<GameRoom>();
	static int totalPlayers = 0;
	static int totalGames = 0;
	
	TCPServer(Socket socket) {
		this.socket = socket;
	}

	public static void main(String argv[]) throws Exception {
		// create welcoming socket at port 6789
		ServerSocket welcomeSocket = new ServerSocket(6789);
		
		while (true) {
			Socket socket = welcomeSocket.accept();
			new Thread(new TCPServer(socket)).start();
	    }  
	}
	
	public void run() {
		try {
			String clientSentence;
			String outputSentence;
			
			// create input stream attached to socket
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
			// create output stream attached to socket
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
			
			Player newPlayer = new Player();
			if (totalPlayers < 10) newPlayer.id = "00" + Integer.toString(totalPlayers);
			else if (totalPlayers < 100) newPlayer.id = "0" + Integer.toString(totalPlayers);
			else newPlayer.id = Integer.toString(totalPlayers);
			outToClient.writeBytes(newPlayer.id + "\n");
			newPlayer.status = "Available";
			newPlayer.out = outToClient;
			allPlayers.add(newPlayer);
			totalPlayers++;
			
			while (true) {
				// read in line from socket
				clientSentence = inFromClient.readLine();
						
				// if (clientSentence == null) break;
				outputSentence = "";
				
				String tempid = clientSentence.substring(0, 3);
				boolean firstTimeFound = false;
				for (int i = 0; i < allGames.size(); i++) {
					if (tempid.equals(allGames.get(i).playerTwo.id) && allGames.get(i).firstTime) {
						outputSentence = "firsttime" + allGames.get(i).playerOne.name + "\n";
						allGames.get(i).firstTime = false;
						firstTimeFound = true;
						allGames.get(i).playerOne.out.writeBytes("ready\n");
					}
				}
				
				if (firstTimeFound) {}
				else {
					if (clientSentence.contains("help")) outputSentence = "help\n";
					else if (clientSentence.contains("loginerror")) {
						outputSentence = "You are already logged in.\n";
					}
					else if (clientSentence.contains("login ")) {
						boolean alreadyInUse = false;
						outputSentence = "";
						for (int i = 0; i < allPlayers.size(); i++) {
							if (clientSentence.substring(9).equals(allPlayers.get(i).name))
								alreadyInUse = true;
						}
						if (alreadyInUse) outputSentence = "Username already is use.\n";
						else {
							for (int i = 0; i < allPlayers.size(); i++) {
								if (clientSentence.substring(0, 3).equals(allPlayers.get(i).id)) {
									allPlayers.get(i).name = clientSentence.substring(9);
									outputSentence = "Logged in as " + allPlayers.get(i).name + ".\n";
								}
							}
						}
					}
					else if (clientSentence.contains("who")) {
						outputSentence = "names";
						for (int i = 0; i < allPlayers.size(); i++) {
							if (!allPlayers.get(i).name.equals("")) {
								outputSentence = outputSentence + allPlayers.get(i).name + " "
										+ allPlayers.get(i).status + " ";
							}
						}
						if (outputSentence.equals("names")) outputSentence = "There are no current players.\n";
						else outputSentence += "\n";
					}
					else if (clientSentence.contains("games")) {
						outputSentence = "games";
						for (int i = 0; i < allGames.size(); i++) {
							outputSentence = outputSentence + allGames.get(i).gameid + " "
									+ allGames.get(i).playerOne.name + " "
									+ allGames.get(i).playerTwo.name + " ";
						}
						if (outputSentence.equals("games")) outputSentence = "There are no current games.\n";
						else outputSentence += "\n";
					}
					else if (clientSentence.contains("play ")) {
						boolean playerAvailable = false;
						boolean playerExists = false;
						boolean loggedIn = false;
						boolean alreadyInGame = false;
						boolean playYourself = false;
						outputSentence = "";
						String id = clientSentence.substring(0, 3);
						for (int i = 0; i < allPlayers.size(); i++) {
							if (allPlayers.get(i).id.equals(id)) {
								if (!allPlayers.get(i).name.equals("")) loggedIn = true;
								if (allPlayers.get(i).name.equals(clientSentence.substring(8)))
									playYourself = true;
							}
						}
						for (int i = 0; i < allGames.size(); i++) {
							if (allGames.get(i).playerOne.id.equals(id) || allGames.get(i).playerTwo.id.equals(id)) {
								alreadyInGame = true;
							}
						}
						if (loggedIn && !alreadyInGame && !playYourself) {
							for (int i = 0; i < allPlayers.size(); i++) {
								if (clientSentence.substring(8).equals(allPlayers.get(i).name))
									if (allPlayers.get(i).status.equals("Available")) {
										playerAvailable = true;
										playerExists = true;
										outputSentence = "Game with " + allPlayers.get(i).name + " has begun.\n";
										id = clientSentence.substring(0, 3);
										Player p1 = new Player();
										Player p2 = allPlayers.get(i);
										for (int j = 0; j < allPlayers.size(); j++) {
											if (allPlayers.get(j).id.equals(id)) p1 = allPlayers.get(j);
										}
										p1.status = "Busy";
										p2.status = "Busy";
										String gameid = "";
										if (totalGames < 10) gameid = "00" + Integer.toString(totalGames);
										else if (totalGames < 100) gameid = "0" + Integer.toString(totalGames);
										else gameid = Integer.toString(totalGames);
										GameRoom newGame = new GameRoom(p1, gameid);
										newGame.addPlayerTwo(p2);
										totalGames++;
										allGames.add(newGame);
										i = allPlayers.size();
									} else {
										playerExists = true;
										playerAvailable = false;
									}
							}
						}
						if (playYourself) outputSentence = "You can't play yourself.\n";
						else if (!loggedIn) outputSentence = "You must log in first.\n";
						else if (alreadyInGame) outputSentence = "You are already in a game.\n";
						else if (!playerExists) outputSentence = "Player does not exist.\n";
						else if (!playerAvailable) outputSentence = "Player is not currently available.\n";
					}
					else if (clientSentence.contains("place ")) {
						outputSentence = "";
						boolean inGame = false;
						String id = clientSentence.substring(0, 3);
						GameRoom currentGame = new GameRoom();
						Player p1 = new Player();
						Player p2 = new Player();
						int playerNumber = 0;
						for (int i = 0; i < allGames.size(); i++) {
							if (allGames.get(i).playerOne.id.equals(id)) {
								inGame = true;
								currentGame = allGames.get(i);
								playerNumber = 1;
								p1 = allGames.get(i).playerOne;
								p2 = allGames.get(i).playerTwo;
							} else if (allGames.get(i).playerTwo.id.equals(id)) {
								inGame = true;
								currentGame = allGames.get(i);
								playerNumber = 2;
								p1 = allGames.get(i).playerOne;
								p2 = allGames.get(i).playerTwo;
							}
						}
						if (inGame) {
							int value = Integer.parseInt(clientSentence.substring(9));
							if (playerNumber != currentGame.whosTurn) {
								outputSentence = "It's not your turn.\n";
							}
							else if (value < 1 || value > 9) outputSentence = "Invalid cell.\n";
							else {
								if (!currentGame.board[value - 1].equals("."))
									outputSentence = "Cell already occupied.\n";
								else {
									if (currentGame.whosTurn == 1) {
										currentGame.board[value - 1] = "X";
										currentGame.whosTurn = 2;
									}
									else {
										currentGame.board[value - 1] = "O";
										currentGame.whosTurn = 1;
									}
											
									boolean gameOver = false;
											
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
									} else {
										outputSentence = "It's a draw.\n";
										gameOver = true;
										
										// check for draw (full board)
										for (int i = 0; i < 9; i++) {
											if (currentGame.board[i].equals(".")) gameOver = false;
										}
									}
									
									if (!gameOver) {
										outputSentence = "board";
										for (int i = 0; i < 9; i++) {
											outputSentence += currentGame.board[i];
										}
										outputSentence += "\n";
										if (currentGame.whosTurn == 1) p1.out.writeBytes(outputSentence);
										else p2.out.writeBytes(outputSentence);
									} else {
										p1.status = "Available";
										p2.status = "Available";
										String result = "";
										if (outputSentence.contains("Player 1")) result = "p1";
										else if (outputSentence.contains("Player 2")) result = "p2";
										else result = "dw";
										outputSentence = "board";
										for (int i = 0; i < 9; i++) {
											outputSentence += currentGame.board[i];
										}
										for (int i = 0; i < allGames.size(); i++) {
											if (allGames.get(i).gameid.equals(currentGame.gameid))
												allGames.remove(i);
										}
										outputSentence = outputSentence + result;
										if (result.equals("p1"))
											outputSentence = outputSentence + p1.name + "\n";
										else if (result.equals("p2"))
											outputSentence = outputSentence + p2.name + "\n";
										if (currentGame.whosTurn == 1) p1.out.writeBytes(outputSentence);
										else p2.out.writeBytes(outputSentence);
									}
								}
							}
						} else {
							outputSentence = "You must start a game first.\n";
						}
					}
					else if (clientSentence.contains("exit")) {
						outputSentence = "exit\n";
						tempid = clientSentence.substring(0, 3);
						Player pl1 = new Player();
						Player pl2 = new Player();
						for (int i = 0; i < allGames.size(); i++) {
							if (tempid.equals(allGames.get(i).playerOne.id)
									|| tempid.equals(allGames.get(i).playerTwo.id)) {
								pl1 = allGames.get(i).playerOne;
								pl2 = allGames.get(i).playerTwo;
								allGames.remove(i);
								pl1.status = "Available";
								pl2.status = "Available";
								if (tempid == pl1.id) {
									pl1.out.writeBytes(pl2.name + " has exited.\n");
									for (int j = 0; j < allPlayers.size(); j++) {
										if (allPlayers.get(i).id.equals(tempid)) allPlayers.remove(i);
									}
								}
								else {
									pl2.out.writeBytes(pl1.name + " has exited.\n");
									for (int j = 0; j < allPlayers.size(); j++) {
										if (allPlayers.get(i).id.equals(tempid)) allPlayers.remove(i);
									}
								}
							}
						}
						for (int i = 0; i < allPlayers.size(); i++) {
							if (allPlayers.get(i).id.equals(tempid)) allPlayers.remove(i);
						}
					}
					else outputSentence = "Invalid command.\n";
				}
				
				// write out line to socket
				outToClient.writeBytes(outputSentence);
			}
		} catch (IOException e) {
			System.out.println(e);
	    }
	}
}