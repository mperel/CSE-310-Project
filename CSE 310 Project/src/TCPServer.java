import java.io.*;
import java.net.*;

public class TCPServer implements Runnable {
	Socket socket;
	static int numPlayers = 0;
	static String[] board = new String[9];
	static int whosTurn = 0;
	static String playerOneUsername = "";
	
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
			
			while (true) {
				// read in line from socket
				clientSentence = inFromClient.readLine();
						
				if (clientSentence == null) break;
						
				if (clientSentence.equals("help")) outputSentence = "help\n";
				else if (clientSentence.contains("loginerror")) {
					outputSentence = "You are already logged in.\n";
				}
				else if (clientSentence.contains("login ")) {
					if (clientSentence.substring(6).equals(playerOneUsername))
						outputSentence = "Username already is use.\n";
					else {
						if (numPlayers < 2) {
							numPlayers++;
							if (numPlayers == 1) {
								outputSentence = "player1\n";
								for (int i = 0; i < 9; i++) board[i] = ".";
								playerOneUsername = clientSentence.substring(6);
							}
							else {
								outputSentence = "player2\n";
								whosTurn = 1;
							}
						}
						else outputSentence = "There are already two players connected.\n";
					}
				}
				else if (clientSentence.contains("place ")) {
					if (numPlayers == 2) {
						int value = Integer.parseInt(clientSentence.substring(7));
						int player = Integer.parseInt(clientSentence.substring(0, 1));
						if (player != whosTurn) {
							outputSentence = "It's not your turn.\n";
						}
						else if (value < 1 || value > 9) outputSentence = "Invalid cell.\n";
						else {
							if (!board[value - 1].equals(".")) outputSentence = "Cell already occupied.\n";
							else {
								if (player == 1) {
									board[value - 1] = "X";
									whosTurn = 2;
								}
								else {
									board[value - 1] = "O";
									whosTurn = 1;
								}
										
								boolean gameOver = false;
										
								if ((board[0].equals("X") && board[1].equals("X") && board[2].equals("X"))
										|| (board[3].equals("X") && board[4].equals("X") && board[5].equals("X"))
										|| (board[6].equals("X") && board[7].equals("X") && board[8].equals("X"))
										|| (board[0].equals("X") && board[3].equals("X") && board[6].equals("X"))
										|| (board[1].equals("X") && board[4].equals("X") && board[7].equals("X"))
										|| (board[2].equals("X") && board[5].equals("X") && board[8].equals("X"))
										|| (board[0].equals("X") && board[4].equals("X") && board[8].equals("X"))
										|| (board[2].equals("X") && board[4].equals("X") && board[6].equals("X"))) {
									outputSentence = "Player 1 is the winner.\n";
									gameOver = true;
								} else if ((board[0].equals("O") && board[1].equals("O") && board[2].equals("O"))
										|| (board[3].equals("O") && board[4].equals("O") && board[5].equals("O"))
										|| (board[6].equals("O") && board[7].equals("O") && board[8].equals("O"))
										|| (board[0].equals("O") && board[3].equals("O") && board[6].equals("O"))
										|| (board[1].equals("O") && board[4].equals("O") && board[7].equals("O"))
										|| (board[2].equals("O") && board[5].equals("O") && board[8].equals("O"))
										|| (board[0].equals("O") && board[4].equals("O") && board[8].equals("O"))
										|| (board[2].equals("O") && board[4].equals("O") && board[6].equals("O"))) {
									outputSentence = "Player 2 is the winner.\n";
									gameOver = true;
								} else {
									outputSentence = "It's a draw.\n";
									gameOver = true;
									
									// check for draw (full board)
									for (int i = 0; i < 9; i++) {
										if (board[i].equals(".")) gameOver = false;
									}
								}
								
								if (!gameOver) {
									outputSentence = "board";
									for (int i = 0; i < 9; i++) {
										outputSentence += board[i];
									}
									outputSentence += "\n";
								}
							}
						}
					} else {
						int player = Integer.parseInt(clientSentence.substring(0, 1));
						if (numPlayers == 0) {
							outputSentence = "You need to log in first.\n";
						} else if (player == 1 && numPlayers == 1) {
							outputSentence = "You must wait for another player.\n";
						} else if (player == 2 && numPlayers == 1) {
							outputSentence = "You need to log in first.\n";
						} else outputSentence = "You must wait for another player.\n";
					}
				}
				else if (clientSentence.equals("exit")) outputSentence = "exit\n";
				else outputSentence = "Invalid command.\n";
				
				// write out line to socket
				outToClient.writeBytes(outputSentence);
			}
		} catch (IOException e) {
			System.out.println(e);
	    }
	}
}