package mysql2;
import java.io.*; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*; 
import java.net.*; 
  
public class Server  
{	
    // ������ ������ �������� ��������� �������
    static Vector<ClientHandler> ar = new Vector<>(); 
    
    // �� ��������� ������� �� ������ (������� �� ������� ���)
    static int i = 0; 
    public static void main(String[] args) throws IOException  
    { 
    	// ������� ������������ �� ��
    	DBConnection.Init();
    	
    	// ������ ������ � ������ �������� �� �� ����� � �� ������ � ����
    	ClientHandler.GetData();
    	
        // server socket ����� ����� �� ���� 1234
        ServerSocket ss = new ServerSocket(1234); 
          
        Socket s; 
        
        System.out.println(">> ������� � ������ � ������ �������.");
          
        // ��������� ����, �� �� �������� ���������, ����� �� �������������
        while (true)  
        { 
        	// �������� ������� �������
            s = ss.accept(); 
  
            System.out.println(">> ��� ������ *client "+i+") ����� � ������� (" + s+")"); 
              
            // ����� ������� - input � output
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
              
  
            // ������ ��� ����� � ClientHandler (��� ������ ���������, ����� �� ������)
            ClientHandler mtch = new ClientHandler(s,"client " + i, i, dis, dos); 
  
            // ������ ��� Thread �� ������� �����
            Thread t = new Thread(mtch); 
              
  
            // �������� ������� ��� ������� (����� �� ����� �� ��������� ������� � �������)
            ar.add(mtch); 
  
            // ���������� Thread-a
            t.start(); 
  
            // ��������� �� � �� ��������� ������
            i++; 
  
        } 
    }
} 
  

class ClientHandler implements Runnable  
{
    Scanner scn = new Scanner(System.in); 
    Socket s; 
    
    private String name; 
    private int id;
    final DataInputStream dis; 
    final DataOutputStream dos; 
    boolean isloggedin=false; 
    boolean valid=false;
    
    
    // ���� ����� �� �������� �� ������� � ��
 	static ArrayList<User> list = new ArrayList<User>();
 	
    // ����������� 
    public ClientHandler(Socket s, String name, int ID, DataInputStream dis, DataOutputStream dos) 
    { 
    	this.id = ID;
        this.dis = dis; 
        this.dos = dos; 
        this.name = name; 
        this.s = s; 
        this.isloggedin=false; 
    } 
    
    void setName(String newName)
    {
    	this.name = newName;
    }
    
    String getName()
    {
    	return this.name;
    }
    
    // �������� ��� ������� �� ���������� ���� �� ���
    public Boolean Check(String User, String Pass)
    {
    	Boolean isokey=false;
    	for(User i : list) // foreach �� ������� �� ������
		{
    		if(i.getName().equals(User) && i.getPassword().equals(Pass)) // ��� ��� ����������
    		{
    			this.name = i.getName(); // ������� �� ����� � ���� �� ��, ������ ���� � � client i
        		this.id = i.getID(); // ������� �� ID, ������ ����� ����������� ���� ���� � ��
        		this.isloggedin=true; // ������ �

        		isokey = true;
    		}
		}
    	return isokey;
    }
    
    public static void GetData() // ��������� ��������� � �������� ������� � ���� ������, ����� ������ � �������
    {
    	Connection conn = null;
		Statement stmt = null;
		DBConnection db = new DBConnection("ConnectionUsers", conn, stmt);
    	
    	db.OpenConnection();
		String sql = "SELECT * FROM `students`.`persons`;";
		ResultSet rs = db.Select(sql);
		
		try 
		{
			while (rs.next()) 
			{
				list.add(new User (rs.getInt("Personid"), rs.getString("Username"), rs.getString("Password"))); 
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		db.CloseConnection();
    }
    
    void processLogin(String user, String password) // ��������� � ����� ������� ����� �� ������ � ������� ��� �����
    {
        if (user == null) 
        {
        	try
        	{
        		this.dos.writeUTF("��������� ������!\n���������� ������ �: login#username#password\n���� �������� ������������� ���!");
        	}
        	catch (IOException e) 
            { 
                e.printStackTrace(); 
            }
        }
        else if (password == null) 
        {
        	try
        	{
        		this.dos.writeUTF("��������� ������!\n���������� ������ �: login#username#password\n���� �������� ������!");
        	}
        	catch (IOException e) 
            { 
                e.printStackTrace(); 
            }
        }
        else 
        {
            if (Check(user, password)) 
            {
                valid = true;
                try
            	{
            		this.dos.writeUTF("������� ���� � ���������!\n���:"+this.name);
            		this.dos.writeUTF("���������� 5 ��������� ���������� ��� ��� � �������� ���:");
                	LastGotMessages(this.id, 5); // ����� �� ����������� ���������� 5 ���������
                	this.dos.writeUTF("�������� ��������� �� ������ �� ������: message#client");
            	}
            	catch (IOException e) 
                { 
                    e.printStackTrace(); 
                }
            }
            else 
            {
                valid = false;
                try
            	{
            		this.dos.writeUTF("���������� ����� �� ���� � ������ �����!\n�������� ������...");
            	}
            	catch (IOException e) 
                { 
                    e.printStackTrace(); 
                }
            }
        }
    }
    
    public void LastGotMessages(int ID, int Limit) // ��������� �� ������� �� �����������
    {
    	Connection conn = null;
		Statement stmt = null;
		DBConnection db = new DBConnection("ConnectionLastMessages", conn, stmt);
    	
    	db.OpenConnection();
		String sql = "SELECT * FROM `messages` WHERE Clientid="+ID+" ORDER BY `messages`.`Messageid` DESC LIMIT "+Limit+";";
		ResultSet rs = db.Select(sql);
		
		try 
		{
			while (rs.next()) 
			{
				  String msg = rs.getString("Message");
				  int personid = rs.getInt("Personid");
				  String time = rs.getString("time");
				  
				  String Name = "";
				    for(User i : list) 
					{
			    		if(i.getID()==(personid))
			    		{
			    			Name = i.getName();
			    		}
					}
				  
				  
				    try {
						this.dos.writeUTF("("+time+") "+Name + ": " + msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		db.CloseConnection();
    }
    
    public void Help() // ������� �� ���� �� �������
    {
    	try
    	{
    		this.dos.writeUTF("=================================================================================\n"
    				+ "����� ����� � ������������ UniChat\n"
    				+ "�� �� ������� � ������� ��, ���� �� ��������: login#Username#Password\n"
    				+ "Username: ������ ������������� ��� � ���������.\n"
    				+ "Password: ������ ������ ���������� ��� ��������������� �� ���.\n\n"
    				+ "������� �������:\n"
    				+ "help - ������������� ���������\n"
    				+ "logout - �������� �� ���������\n"
    				+ "hello#Peter Ivanov - ��������� �� ����������� 'hello' �� ���������� 'Peter Ivanov' (������: message#client)\n"
    				+ "list - ��������� �� ������� � ������ ����������� � �������.\n"
    				+ "history - ��������� �� ������ ��������� ���������� �� ���.\n"
    				+ "=================================================================================\n"
    				+ "���� �������� login#username#password �� �� ������� � ���������.\n");
    	}
    	catch (IOException e) 
        { 
            e.printStackTrace(); 
        }
    }
  
    @Override
    public void run() 
    { 
    	Date date = new Date(); // ����� ����� ����� �������� �����
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	Connection conn = null;
		Statement stmt = null;
		DBConnection db = new DBConnection("ConnectionMessages", conn, stmt);

    	Help(); // ��� ������� �� ��������� �����������

    	
        String received; 
        while (true)  
        { 
            try
            { 
                // ���������� �������
                received = dis.readUTF(); 
                  
                // ��������� - ������ �� �� �����
                if(!valid)
                {
                	// ��������� ������� �� �����
                	StringTokenizer st_login = new StringTokenizer(received, "#"); 
                    String Login = st_login.nextToken();
                    String Username = null;
                    if(st_login.hasMoreTokens()) Username = st_login.nextToken();
                    String Password = null;
                    if(st_login.hasMoreTokens()) Password = st_login.nextToken();
                    
	                if(Login.equals("login")) // ��� ������� ���� � login
	                {
	                	processLogin(Username,Password); // ������ ������ � ��� � �� ����������
	                }
	                else if(Login.equals("logout")) // ��� ������� ��� � logout
	                {
	                	// ����� ��
	                	this.isloggedin=false; 
	                    this.s.close(); 
	                    break; 
	                }
	                else
	                {
	                	this.dos.writeUTF("��������!!!\n��� �� ��� ������! �������� login#username#password �� �� �������!");
	                }
                }
                else // ������ �!
                {
	                // ��������� ������� ��� �� �����
	                StringTokenizer st = new StringTokenizer(received, "#"); 
	                String MsgToSend = st.nextToken();
	                String recipient = "";
	                if(st.hasMoreTokens()) recipient = st.nextToken();
	                
	                if(MsgToSend.equals("login")) // ��� 1���� ���� � �����
	                {
	                	this.dos.writeUTF("��� ��� ���� � ���������!\n�� �� ��������� ���������, ��������: message#user\n��� ������� �� �������, �������� logout");
	                }
	                else if(MsgToSend.equals("list")) //��� 1���� ���� � ����
	                {
	                	this.dos.writeUTF("����� � ����������� � ���������:");
	                	for (ClientHandler mc : Server.ar)  // ��������� �������� � �������
	                	{
	                		if(mc.isloggedin==true) // ���� ��� �� ������ � ��������� ��
	                		{
	                			if(mc.getName() == this.name) // ������ ��������� � ��� � ������ �� �������� ���� (���) ��� �����
	                			{
	                				this.dos.writeUTF(mc.getName()+" (���)");
	                			}
	                			else this.dos.writeUTF(mc.getName()); // ������ ��������
	                		}
	                	}
	                }
	                else if(MsgToSend.equals("history")) // ��� 1���� ���� � �������
	                {
	                	int h_number = 0;
	                	if(recipient != "") h_number = Integer.parseInt(recipient); // ��� ��� 2�� ����
	                	if(h_number > 0) // ��� 2���� ���� ��-������ �� 0
	                	{
	                		this.dos.writeUTF("��������� �� ���������� "+h_number+" ���������:");
	                		LastGotMessages(this.id, h_number); // ������� �� �������� ���������� � ���������
	                	}
	                	else // ��� �� �, ����� �� �������� ���� ����������
	                	{
	                		this.dos.writeUTF("��������� �� ���������� ���������:");
	                		LastGotMessages(this.id, 1); // ������ ��� �� ���������� 1 ���������
	                	}
	                }
	                else if(MsgToSend.equals("help")) // ��� ��� ���� �� ���� ����������� � ��������
	                {
	                	Help();
	                }
	                else if(MsgToSend.equals("logout")) // ��� ���� �� ������ �� ���������
	                {
	                	this.dos.writeUTF("�������� �� ���������...");
		                
	                	this.isloggedin=false; 
	                    this.s.close(); 
	                }
	                else // ����� � ���������
	                {
	                	// ������ ���������� � �������(�������) � ������ � ������� �������
		                for (ClientHandler mc : Server.ar)  
		                {
		                    if (mc.name.equals(recipient))  // ��� � �������
		                    {
			                    	if(mc.isloggedin==true) // ��� � ������
			                    	{
			                    		// ��� � �� �� �� �� ������ �� ������� output stream
			                    		mc.dos.writeUTF(this.name+" : "+MsgToSend); 
			                    		this.dos.writeUTF(">> "+MsgToSend+" � ��������� �� ���������� "+recipient+" ("+formatter.format(date)+")");
			                    		
			                    		// ������� �����������
			                    		db.OpenConnection();
					            		String sql = "INSERT INTO Messages(Personid, Clientid, Message) VALUES ("+this.id+", "+mc.id+", '"+MsgToSend+"');";
					            		db.Execute(sql);
					            		db.CloseConnection();
			                    		
			                    		break; 
			                    	 }
			                    	 
		                    }
		                    else // ��� �� � �������
	                    	 {
	                    		for(User i : list) // ����� �� ���� ��� ������ ����� ���������� � ������� � ������ (�����������)
	        					{
	        			    		if(i.getName().equals(recipient)) // ��� � ������� � ������
	        			    		{
	        			    			// ������� �����������
	        			    			db.OpenConnection();
					            		String sql = "INSERT INTO Messages(Personid, Clientid, Message) VALUES ("+this.id+", "+i.getID()+", '"+MsgToSend+"');";
					            		db.Execute(sql);
					            		db.CloseConnection();
					            		this.dos.writeUTF("����������� �� � �� �����.. �� ���������� �����������..");
					            		break;
	        			    		}
	        					}
	                    	
	                    	  }
		                }
	                }
                }
            } 
            catch (IOException e) 
            { 
                e.printStackTrace(); 
            } 
        } 
        try
        { 
            // ��������� ����� �������
            this.dis.close(); 
            this.dos.close(); 
              
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
    
    
    
} 
