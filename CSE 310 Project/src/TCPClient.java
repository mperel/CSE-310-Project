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
	
		String username = null;
		int playerNumber = 0;
		while (!sentence.equals("exit")) {
			System.out.print("Input: ");
			sentence = inFromUser.readLine();
			
			if (sentence.contains("login ") && username == null) {
				username = sentence.substring(6);
			} else if (sentence.contains("login ") && username != null) {
				sentence = "loginerror";
			}
			
			if (sentence.contains("place ")) {
				String temp;
				if (playerNumber == 1) temp = "1";
				else temp = "2";
				temp += sentence;
				sentence = temp;
			}
		
			// send line to server
			outToServer.writeBytes(sentence + '\n');
		
			// read line from server
			modifiedSentence = inFromServer.readLine();
		
			System.out.println();
			if (modifiedSentence.equals("help")) {
				System.out.println("Supported Commands:\n\n"
						+ "help - Prints a list of supported commands.\n"
						+ "login [USERID] - Logs in with the given user id.\n"
						+ "place [n] - Makes a move on cell n.\n"
						+ "exit - Exits the server.");
			} else if (modifiedSentence.contains("board")) {
				System.out.print(modifiedSentence.charAt(5) + " "
						+ modifiedSentence.charAt(6) + " "
						+ modifiedSentence.charAt(7) + "\n"
						+ modifiedSentence.charAt(8) + " "
						+ modifiedSentence.charAt(9) + " "
						+ modifiedSentence.charAt(10) + "\n"
						+ modifiedSentence.charAt(11) + " "
						+ modifiedSentence.charAt(12) + " "
						+ modifiedSentence.charAt(13) + "\n");
			} else if (modifiedSentence.equals("exit")) {
				sentence = "exit";
				System.out.println("Exited.");
			} else if (modifiedSentence.contains("player1")) {
				playerNumber = 1;
				System.out.println("Logged in as Player 1.");
				System.out.println("Please wait for another player.");
			} else if (modifiedSentence.contains("player2")) {
				playerNumber = 2;
				System.out.println("Logged in as Player 2.");
			} else if (modifiedSentence.contains("Username already is use")) {
				username = null;
				System.out.println(modifiedSentence);
			} else System.out.println(modifiedSentence);
			System.out.println();
		}
		clientSocket.close();
	} 
} 