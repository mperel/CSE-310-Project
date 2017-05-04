import java.io.*;
import java.net.*;

class TCPClient {
	
	public static void main(String argv[]) throws Exception {
		String sentence = "";
		String modifiedSentence;
	
		// create input stream
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	
		// create client socket, connect to server
		Socket clientSocket = new Socket("localhost", 6789);
	
		// create output stream attached to socket
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	
		// create input stream attached to socket
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	
		modifiedSentence = "";
		while (modifiedSentence.equals("")) {
			modifiedSentence = inFromServer.readLine();
		}
		String id = modifiedSentence;
		
		String username = null;
		boolean gameBegun = false;
		boolean myTurn = true;
		while (!sentence.equals("exit")) {
			if (gameBegun && !myTurn) {
				System.out.println("Waiting for opponent to move...");
				modifiedSentence = "";
				while (!modifiedSentence.contains("board") && !modifiedSentence.contains("has exited")) {
					modifiedSentence = inFromServer.readLine();
				}
				if (modifiedSentence.contains("has exited")) {
					System.out.println("\nThe other player has left.\n");
					gameBegun = false;
				} else {
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
				myTurn = true;
				if (modifiedSentence.contains("p1")) {
					System.out.println(modifiedSentence.substring(16) + " is the winner.\n");
					gameBegun = false;
				}
				else if (modifiedSentence.contains("p2")) {
					System.out.println(modifiedSentence.substring(16) + " is the winner.\n");
					gameBegun = false;
				}
				else if (modifiedSentence.contains("dw")) {
					System.out.println("It's a draw.\n");
					gameBegun = false;
				}
			}
			
			System.out.print("Input: ");
			sentence = inFromUser.readLine();
			sentence = id + sentence;
			
			if (sentence.contains("login ") && username == null) {
				username = sentence.substring(6);
			} else if (sentence.contains("login ") && username != null) {
				sentence = "loginerror";
			}
		
			// send line to server
			outToServer.writeBytes(sentence + '\n');
		
			// read line from server
			modifiedSentence = inFromServer.readLine();
		
			System.out.println();
			if (modifiedSentence.equals("help")) {
				System.out.println("Supported Commands:\n\n"
						+ "help - Prints a list of supported commands.\n"
						+ "login [USERID] - Logs in with the specified [USERID].\n"
						+ "place [n] - Makes a move on cell [n].\n"
						+ "games - Prints a list of ongoing games.\n"
						+ "who - Prints a list of available players.\n"
						+ "play [USERID] - Starts a new game with player [USERID].\n"
						+ "exit - Exits the server.");
				myTurn = true;
			} else if (modifiedSentence.contains("board")) {
				gameBegun = true;
				System.out.print(modifiedSentence.charAt(5) + " "
						+ modifiedSentence.charAt(6) + " "
						+ modifiedSentence.charAt(7) + "\n"
						+ modifiedSentence.charAt(8) + " "
						+ modifiedSentence.charAt(9) + " "
						+ modifiedSentence.charAt(10) + "\n"
						+ modifiedSentence.charAt(11) + " "
						+ modifiedSentence.charAt(12) + " "
						+ modifiedSentence.charAt(13) + "\n");
				myTurn = false;
				if (modifiedSentence.contains("p1")) {
					System.out.println("\n" + modifiedSentence.substring(16) + " is the winner.");
					myTurn = true;
					gameBegun = false;
				}
				else if (modifiedSentence.contains("p2")) {
					System.out.println("\n" + modifiedSentence.substring(16) + " is the winner.");
					myTurn = true;
					gameBegun = false;
				}
				else if (modifiedSentence.contains("dw")) {
					System.out.println("\nIt's a draw.");
					myTurn = true;
					gameBegun = false;
				}
			} else if (modifiedSentence.equals("exit")) {
				sentence = "exit";
				System.out.println("Exited.");
			} else if (modifiedSentence.contains("Username already is use")) {
				username = null;
				System.out.println(modifiedSentence);
				myTurn = true;
			} else if (modifiedSentence.contains("names")) {
				modifiedSentence = modifiedSentence.substring(5);
				String[] tokens = modifiedSentence.split("\\s+");
				System.out.println("Username\tStatus");
			    for (int i = 0; i < tokens.length; i += 2) {
			        System.out.print(tokens[i] + "\t\t" + tokens[i + 1] + "\n");
			    }
			} else if (modifiedSentence.contains("There are no current games")) {
				System.out.println(modifiedSentence);
			} else if (modifiedSentence.contains("games")) {
				modifiedSentence = modifiedSentence.substring(5);
				String[] tokens = modifiedSentence.split("\\s+");
				System.out.println("Game ID\t\tPlayer One\tPlayer Two");
			    for (int i = 0; i < tokens.length; i += 3) {
			        System.out.print(tokens[i] + "\t\t" + tokens[i + 1] + "\t\t" + tokens[i + 2] + "\n");
			    }
			} else if (modifiedSentence.contains("Game with ")) {
				System.out.println(modifiedSentence);
				gameBegun = true;
				System.out.println("Waiting for input from opponent...");
				modifiedSentence = "";
				while (!modifiedSentence.contains("ready")) {
					modifiedSentence = inFromServer.readLine();
				}
			} else if (modifiedSentence.contains("firsttime")) {
				gameBegun = true;
				System.out.println("Game with " + modifiedSentence.substring(9) + " has begun.");
				myTurn = false;
			} else {
				System.out.println(modifiedSentence);
			}
			System.out.println();
		}
		clientSocket.close();
	} 
} 