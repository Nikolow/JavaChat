package mysql2;

public class User 
{
	private String Name;
	private String Password;
	private int ID;
	
	public User(int id, String name, String password)
	{
		this.ID = id;
		this.Name = name;
		this.Password = password;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
}
