import java.io.*;
import java.net.*;

class TCPServer {

	public static void main(String argv[]) throws Exception {
		String clientSentence;
		String outputSentence;
		String[] board = new String[9];
		for (int i = 0; i < 9; i++) board[i] = "-";
		
		// create welcoming socket at port 6789
		ServerSocket welcomeSocket = new ServerSocket(6789);

		// wait on welcoming socket for contact by client
		Socket connectionSocket = welcomeSocket.accept();

		// create input stream attached to socket
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

		// create output stream attached to socket
		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		
		while (true) {
			// read in line from socket
			clientSentence = inFromClient.readLine();
			
			if (clientSentence == null) break;
			
			if (clientSentence.equals("help")) outputSentence = "help\n";
			else if (clientSentence.contains("login")) {
				outputSentence = "Logged in as " + clientSentence.substring(6) + ".\n";
			}
			else if (clientSentence.contains("place")) {
				int value = Integer.parseInt(clientSentence.substring(6));
				if (value < 1 || value > 9) outputSentence = "Invalid cell.\n";
				else {
					if (!board[value - 1].equals("-")) outputSentence = "Cell already occupied.\n";
					else {
						board[value - 1] = "X";
						
						boolean gameOver = false;
						
						if ((board[0].equals("X") && board[1].equals("X") && board[2].equals("X"))
								|| (board[3].equals("X") && board[4].equals("X") && board[5].equals("X"))
								|| (board[6].equals("X") && board[7].equals("X") && board[8].equals("X"))
								|| (board[0].equals("X") && board[3].equals("X") && board[6].equals("X"))
								|| (board[1].equals("X") && board[4].equals("X") && board[7].equals("X"))
								|| (board[2].equals("X") && board[5].equals("X") && board[8].equals("X"))
								|| (board[0].equals("X") && board[4].equals("X") && board[8].equals("X"))
								|| (board[2].equals("X") && board[4].equals("X") && board[6].equals("X"))) {
							outputSentence = "X is the winner.\n";
							gameOver = true;
						} else if ((board[0].equals("O") && board[1].equals("O") && board[2].equals("O"))
								|| (board[3].equals("O") && board[4].equals("O") && board[5].equals("O"))
								|| (board[6].equals("O") && board[7].equals("O") && board[8].equals("O"))
								|| (board[0].equals("O") && board[3].equals("O") && board[6].equals("O"))
								|| (board[1].equals("O") && board[4].equals("O") && board[7].equals("O"))
								|| (board[2].equals("O") && board[5].equals("O") && board[8].equals("O"))
								|| (board[0].equals("O") && board[4].equals("O") && board[8].equals("O"))
								|| (board[2].equals("O") && board[4].equals("O") && board[6].equals("O"))) {
							outputSentence = "O is the winner.\n";
							gameOver = true;
						} else {
							outputSentence = "It's a draw.\n";
							gameOver = true;
							
							// check for draw (full board)
							for (int i = 0; i < 9; i++) {
								if (board[i].equals("-")) gameOver = false;
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
			}
			else if (clientSentence.equals("exit")) outputSentence = "exit\n";
			else outputSentence = "Invalid command.\n";

			// write out line to socket
			outToClient.writeBytes(outputSentence);
		}  
	} 
}