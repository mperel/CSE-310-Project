import java.io.DataOutputStream;

public class Player {
	String name = "";
	String id = "";
	String status = "";
	DataOutputStream out;
	
	public Player() {}
	
	public Player(String name, String id, String status, DataOutputStream out) {
		this.name = name;
		this.id = id;
		this.status = status;
		this.out = out;
	}
}
