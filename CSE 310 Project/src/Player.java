import java.io.DataOutputStream;

public class Player {
	String name = "";		// username
	String id = "";		// player id
	String status = "";		// available or busy
	DataOutputStream out;		// unique output stream
	
	/**
	 * Default constructor.
	 */
	public Player() {}
	
	/**
	 * Player constructor with arguments.
	 * @param name
	 * @param id
	 * @param status
	 * @param out
	 */
	public Player(String name, String id, String status, DataOutputStream out) {
		this.name = name;
		this.id = id;
		this.status = status;
		this.out = out;
	}
}
