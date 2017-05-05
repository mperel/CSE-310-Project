import java.io.*;
import java.net.*;

class TCPClient {
	
	public static void main(String argv[]) throws Exception {
		String sentence = "";		// message to be sent to server
		String modifiedSentence;		// message received from server
	
		// create input stream
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	
		// create client socket, connect to server
		Socket clientSocket = new Socket("localhost", 6789);
	
		// create output stream attached to socket
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	
		// create input stream attached to socket
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	
		// wait for server to generate an id for the client
		modifiedSentence = "";
		while (modifiedSentence.equals("")) {
			modifiedSentence = inFromServer.readLine();
		}
		String id = modifiedSentence;
		
		String username = null;		// player's username (null until logged in)
		boolean gameBegun = false;		// is the player in a game?
		boolean myTurn = true;		// is it the player's turn?
		while (!sentence.equals("exit")) {		// exit upon "exit" command
			if (gameBegun && !myTurn) {		// if it's not my turn, wait for opponent to make a move
				System.out.println("Waiting for opponent to move...");
				
				modifiedSentence = "";
				// loop until opponent makes a move or exits
				while (!modifiedSentence.contains("board") && !modifiedSentence.contains("has exited")) {
					modifiedSentence = inFromServer.readLine();
				}
				
				// opponent exited
				if (modifiedSentence.contains("has exited")) {
					System.out.println("\nThe other player has left.\n");
					gameBegun = false;
				// opponent made a move
				} else {
					// print the board (3x3 grid)
					System.out.print("\n" + modifiedSentence.charAt(5) + " "
							+ modifiedSentence.charAt(6) + " "
							+ modifiedSentence.charAt(7) + "\n"
							+ modifiedSentence.charAt(8) + " "
							+ modifiedSentence.charAt(9) + " "
							+ modifiedSentence.charAt(10) + "\n"
							+ modifiedSentence.charAt(11) + " "
							+ modifiedSentence.charAt(12) + " "
							+ modifiedSentence.charAt(13) + "\n\n");
				}
				
				myTurn = true;		// now it's my turn to make a move
				
				// player 1 has won
				if (modifiedSentence.contains("p1")) {
					System.out.println(modifiedSentence.substring(16) + " is the winner.\n");
					gameBegun = false;
				}
				// player 2 has won
				else if (modifiedSentence.contains("p2")) {
					System.out.println(modifiedSentence.substring(16) + " is the winner.\n");
					gameBegun = false;
				}
				// game ended in a draw
				else if (modifiedSentence.contains("dw")) {
					System.out.println("It's a draw.\n");
					gameBegun = false;
				}
			}
			
			// prompt for player command
			System.out.print("Input: ");
			sentence = inFromUser.readLine();
			sentence = id + sentence;		// adds the player's unique id to the beginning of every
											// command, this allows the server to know who is sending
											// the command and can react appropriately
			
			// if the player is trying to log in, set the username
			if (sentence.contains("login ") && username == null) {
				username = sentence.substring(6);
			// player is already logged in, but is trying again anyway :(
			} else if (sentence.contains("login ") && username != null) {
				sentence = "loginerror";
			}
		
			// send command to server
			outToServer.writeBytes(sentence + '\n');
		
			// read response from server
			modifiedSentence = inFromServer.readLine();
		
			System.out.println();
			// Each response from the server is (usually) unique. The client prints information
			// depending on what the response was. The client does not always just print the
			// response, it has to interpret it.
			// print the help menu
			if (modifiedSentence.equals("help")) {
				System.out.println("Supported Commands:\n\n"
						+ "help - Prints a list of supported commands.\n"
						+ "login [USERID] - Logs in with the specified [USERID].\n"
						+ "place [n] - Makes a move on cell [n].\n"
						+ "games - Prints a list of ongoing games.\n"
						+ "who - Prints a list of available players.\n"
						+ "play [USERID] - Starts a new game with player [USERID].\n"
						+ "exit - Exits the server.");
				myTurn = true;		// it's still my turn because I didn't make a move
			// print the board
			} else if (modifiedSentence.contains("board")) {
				gameBegun = true;		// this is only here for player 2 when game begins
				System.out.print(modifiedSentence.charAt(5) + " "
						+ modifiedSentence.charAt(6) + " "
						+ modifiedSentence.charAt(7) + "\n"
						+ modifiedSentence.charAt(8) + " "
						+ modifiedSentence.charAt(9) + " "
						+ modifiedSentence.charAt(10) + "\n"
						+ modifiedSentence.charAt(11) + " "
						+ modifiedSentence.charAt(12) + " "
						+ modifiedSentence.charAt(13) + "\n");
				myTurn = false;		// I made a move so it's not my turn anymore
				// player 1 has won
				if (modifiedSentence.contains("p1")) {
					System.out.println("\n" + modifiedSentence.substring(16) + " is the winner.");
					myTurn = true;		// game is over so it's my turn again by default
					gameBegun = false;
				}
				// player 2 has won
				else if (modifiedSentence.contains("p2")) {
					System.out.println("\n" + modifiedSentence.substring(16) + " is the winner.");
					myTurn = true;		// game is over so it's my turn again by default
					gameBegun = false;
				}
				// game ended in a draw
				else if (modifiedSentence.contains("dw")) {
					System.out.println("\nIt's a draw.");
					myTurn = true;
					gameBegun = false;
				}
			// time to exit the client
			} else if (modifiedSentence.equals("exit")) {
				sentence = "exit";
				System.out.println("Exited.");
			// player tried to log in with a username that already exists
			} else if (modifiedSentence.contains("Username already is use")) {
				username = null;
				System.out.println(modifiedSentence);
				myTurn = true;
			// player issued the "who" command so print all the users in a nice table
			} else if (modifiedSentence.contains("names")) {
				modifiedSentence = modifiedSentence.substring(5);
				String[] tokens = modifiedSentence.split("\\s+");
				System.out.println("Username\tStatus");
			    for (int i = 0; i < tokens.length; i += 2) {
			        System.out.print(tokens[i] + "\t\t" + tokens[i + 1] + "\n");
			    }
			// player issued the "games" command, but there are no games
			} else if (modifiedSentence.contains("There are no current games")) {
				System.out.println(modifiedSentence);
			// player issued the "games" command so print all the games in a nice table
			} else if (modifiedSentence.contains("games")) {
				modifiedSentence = modifiedSentence.substring(5);
				String[] tokens = modifiedSentence.split("\\s+");
				System.out.println("Game ID\t\tPlayer One\tPlayer Two");
			    for (int i = 0; i < tokens.length; i += 3) {
			        System.out.print(tokens[i] + "\t\t" + tokens[i + 1] + "\t\t" + tokens[i + 2] + "\n");
			    }
			// player started a game with someone who we wait for opponent's response
			} else if (modifiedSentence.contains("Game with ")) {
				System.out.println(modifiedSentence);
				gameBegun = true;
				System.out.println("Waiting for input from opponent...");
				modifiedSentence = "";
				while (!modifiedSentence.contains("ready")) {
					modifiedSentence = inFromServer.readLine();
				}
			// this is needed for player 2
			} else if (modifiedSentence.contains("firsttime")) {
				gameBegun = true;
				System.out.println("Game with " + modifiedSentence.substring(9) + " has begun.");
				myTurn = false;
			// print the server response by default
			} else {
				System.out.println(modifiedSentence);
			}
			System.out.println();
		}
		
		// close the socket (disconnect client)
		clientSocket.close();
	} 
} 