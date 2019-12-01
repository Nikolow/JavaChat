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
    // вектор където сторваме активните клиенти
    static Vector<ClientHandler> ar = new Vector<>(); 
    
    // за началното влизане на клиент (слагане на дефаулт име)
    static int i = 0; 
    public static void main(String[] args) throws IOException  
    { 
    	// начална иницализация на бд
    	DBConnection.Init();
    	
    	// правим обекти с всички намерени от дб данни и ги пълним в лист
    	ClientHandler.GetData();
    	
        // server socket който слуша на порт 1234
        ServerSocket ss = new ServerSocket(1234); 
          
        Socket s; 
        
        System.out.println(">> Сървъра е пуснат и очаква клиенти.");
          
        // постоянен лууп, за да засичаме клиентите, които се присъединяват
        while (true)  
        { 
        	// приемаме идващия рекуест
            s = ss.accept(); 
  
            System.out.println(">> Нов клиент *client "+i+") влезе в сървъра (" + s+")"); 
              
            // двата стрийма - input и output
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
              
  
            // правим нов обект в ClientHandler (там държим клиентите, които са влезли)
            ClientHandler mtch = new ClientHandler(s,"client " + i, i, dis, dos); 
  
            // правим нов Thread за горният обект
            Thread t = new Thread(mtch); 
              
  
            // добавяме клиента във вектора (който ни служи за активните клиенти в момента)
            ar.add(mtch); 
  
            // стартираме Thread-a
            t.start(); 
  
            // инкремент на и за следващия клиент
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
    
    
    // арей листа от обектите на юзърите в бд
 	static ArrayList<User> list = new ArrayList<User>();
 	
    // конструктор 
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
    
    // Проверка при влизане на потребител дали го има
    public Boolean Check(String User, String Pass)
    {
    	Boolean isokey=false;
    	for(User i : list) // foreach на листата от обекти
		{
    		if(i.getName().equals(User) && i.getPassword().equals(Pass)) // ако има съвпадение
    		{
    			this.name = i.getName(); // сменяме му името с това от бд, понеже сега е с client i
        		this.id = i.getID(); // сетваме му ID, понеже имаме интервенция след това с бд
        		this.isloggedin=true; // онлайн е

        		isokey = true;
    		}
		}
    	return isokey;
    }
    
    public static void GetData() // обхождаме таблицата и вкарваме данните в нови обекти, които влизат в листата
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
    
    void processLogin(String user, String password) // функцията с която казваме какво се случва с клиента при логин
    {
        if (user == null) 
        {
        	try
        	{
        		this.dos.writeUTF("Невалиден Формат!\nПравилният формат е: login#username#password\nНяма въведено Потребителско Име!");
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
        		this.dos.writeUTF("Невалиден Формат!\nПравилният формат е: login#username#password\nНяма въведена Парола!");
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
            		this.dos.writeUTF("Успешен вход в системата!\nИме:"+this.name);
            		this.dos.writeUTF("Последните 5 съобщения адресирани към Вас в низходящ ред:");
                	LastGotMessages(this.id, 5); // вадим му автоматично последните 5 съобщения
                	this.dos.writeUTF("Напишете съобщение до клиент по модела: message#client");
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
            		this.dos.writeUTF("Въведените данни ги няма в базата данни!\nОпитайте отново...");
            	}
            	catch (IOException e) 
                { 
                    e.printStackTrace(); 
                }
            }
        }
    }
    
    public void LastGotMessages(int ID, int Limit) // функцията за история на съобщенията
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
    
    public void Help() // функция за инфо на клиента
    {
    	try
    	{
    		this.dos.writeUTF("=================================================================================\n"
    				+ "Добре Дошли в приложението UniChat\n"
    				+ "За да влезете в профила си, моля да напишете: login#Username#Password\n"
    				+ "Username: Вашето потребителско име в системата.\n"
    				+ "Password: Вашата парола асоциирана към потребителското Ви име.\n\n"
    				+ "Полезни Команди:\n"
    				+ "help - Информационно съобщение\n"
    				+ "logout - Излизане от системата\n"
    				+ "hello#Peter Ivanov - Изпращане на съобщението 'hello' до потребител 'Peter Ivanov' (Формат: message#client)\n"
    				+ "list - Изкарване на листата с онлайн потребители в момента.\n"
    				+ "history - Изкарване на всички съобщения адресирани до Вас.\n"
    				+ "=================================================================================\n"
    				+ "Моля напишете login#username#password за да влезете в системата.\n");
    	}
    	catch (IOException e) 
        { 
            e.printStackTrace(); 
        }
    }
  
    @Override
    public void run() 
    { 
    	Date date = new Date(); // обект който държи текущото време
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	Connection conn = null;
		Statement stmt = null;
		DBConnection db = new DBConnection("ConnectionMessages", conn, stmt);

    	Help(); // при влизане му изкарваме съобщението

    	
        String received; 
        while (true)  
        { 
            try
            { 
                // получаваме стринга
                received = dis.readUTF(); 
                  
                // невалиден - караме го да логне
                if(!valid)
                {
                	// разбиваме стринга на части
                	StringTokenizer st_login = new StringTokenizer(received, "#"); 
                    String Login = st_login.nextToken();
                    String Username = null;
                    if(st_login.hasMoreTokens()) Username = st_login.nextToken();
                    String Password = null;
                    if(st_login.hasMoreTokens()) Password = st_login.nextToken();
                    
	                if(Login.equals("login")) // ако първата част е login
	                {
	                	processLogin(Username,Password); // правим логина и ако е ок продължава
	                }
	                else if(Login.equals("logout")) // ако първата аст е logout
	                {
	                	// гасим го
	                	this.isloggedin=false; 
	                    this.s.close(); 
	                    break; 
	                }
	                else
	                {
	                	this.dos.writeUTF("Внимание!!!\nВие не сте влезли! Напишете login#username#password за да влезете!");
	                }
                }
                else // логнат е!
                {
	                // разбиваме стринга пак на части
	                StringTokenizer st = new StringTokenizer(received, "#"); 
	                String MsgToSend = st.nextToken();
	                String recipient = "";
	                if(st.hasMoreTokens()) recipient = st.nextToken();
	                
	                if(MsgToSend.equals("login")) // ако 1вата част е логин
	                {
	                	this.dos.writeUTF("Вие сте вече в системата!\nЗа да изпратите съобщение, напишете: message#user\nАко желаете да излезте, напишете logout");
	                }
	                else if(MsgToSend.equals("list")) //ако 1вата част е лист
	                {
	                	this.dos.writeUTF("Листа с потребители в системата:");
	                	for (ClientHandler mc : Server.ar)  // обхождаме влезлите в сървъра
	                	{
	                		if(mc.isloggedin==true) // само ако са влезли в акаунтите си
	                		{
	                			if(mc.getName() == this.name) // понеже заявилият и той е онлайн му добавяме едно (Вие) зад името
	                			{
	                				this.dos.writeUTF(mc.getName()+" (Вие)");
	                			}
	                			else this.dos.writeUTF(mc.getName()); // всички останали
	                		}
	                	}
	                }
	                else if(MsgToSend.equals("history")) // ако 1вата част е история
	                {
	                	int h_number = 0;
	                	if(recipient != "") h_number = Integer.parseInt(recipient); // ако има 2ра част
	                	if(h_number > 0) // ако 2рата част по-голяма от 0
	                	{
	                		this.dos.writeUTF("Изкарване на последните "+h_number+" съобщения:");
	                		LastGotMessages(this.id, h_number); // влизаме да изкараме последните Х съобщения
	                	}
	                	else // ако не е, значи ще изкараме само последното
	                	{
	                		this.dos.writeUTF("Изкарване на последното съобщение:");
	                		LastGotMessages(this.id, 1); // същото ама за последното 1 съобщение
	                	}
	                }
	                else if(MsgToSend.equals("help")) // ако пак иска да види съобщенията в началото
	                {
	                	Help();
	                }
	                else if(MsgToSend.equals("logout")) // ако иска да излезе от системата
	                {
	                	this.dos.writeUTF("Излизане от системата...");
		                
	                	this.isloggedin=false; 
	                    this.s.close(); 
	                }
	                else // значи е съобщение
	                {
	                	// търсим получателя в листата(вектора) с влезли в сървъра клиенти
		                for (ClientHandler mc : Server.ar)  
		                {
		                    if (mc.name.equals(recipient))  // ако е намерен
		                    {
			                    	if(mc.isloggedin==true) // ако е логнат
			                    	{
			                    		// ако е ок да му го пратим по неговия output stream
			                    		mc.dos.writeUTF(this.name+" : "+MsgToSend); 
			                    		this.dos.writeUTF(">> "+MsgToSend+" е изпратено на потребител "+recipient+" ("+formatter.format(date)+")");
			                    		
			                    		// логваме съобщението
			                    		db.OpenConnection();
					            		String sql = "INSERT INTO Messages(Personid, Clientid, Message) VALUES ("+this.id+", "+mc.id+", '"+MsgToSend+"');";
					            		db.Execute(sql);
					            		db.CloseConnection();
			                    		
			                    		break; 
			                    	 }
			                    	 
		                    }
		                    else // ако не е намерен
	                    	 {
	                    		for(User i : list) // търси се дали има изобщо такъв потребител в листата с обекти (потребители)
	        					{
	        			    		if(i.getName().equals(recipient)) // ако е намерен в обекта
	        			    		{
	        			    			// логваме съобщението
	        			    			db.OpenConnection();
					            		String sql = "INSERT INTO Messages(Personid, Clientid, Message) VALUES ("+this.id+", "+i.getID()+", '"+MsgToSend+"');";
					            		db.Execute(sql);
					            		db.CloseConnection();
					            		this.dos.writeUTF("Получателят не е на линия.. Ще архивираме съобщението..");
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
            // затваряме двата стрийма
            this.dis.close(); 
            this.dos.close(); 
              
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
    
    
    
} 
