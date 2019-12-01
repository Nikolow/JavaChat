package mysql2;

import java.sql.*;

public class DBConnection 
{
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; //драйвера
	static final String DB_URL = "jdbc:mysql://localhost/students"; // базата данни и таблицата
	
	// дб юзър и парола
	static final String USER = "root";
	static final String PASS = "password";
	
	private String name;
	private Connection conn;
	private Statement stmt;
	
	public DBConnection(String Name, Connection Conn, Statement Stmt)
	{
		this.setName(Name);
		this.setConn(Conn);
		this.setStmt(Stmt);
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	
	

	public void setConn(Connection Conn) 
	{
		this.conn = Conn;
	}
	
	public Connection getConn() 
	{
		return conn;
	}
	
	
	public void setStmt(Statement Stmt) 
	{
		this.stmt = Stmt;
	}
	
	public Statement getStmt() 
	{
		return stmt;
	}

	
	
	public void OpenConnection() // отваряне на кънекция 
	{
		try 
		{
			this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
			this.stmt = this.conn.createStatement();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void CloseConnection() // затваряне на кънекцията
	{
		try 
		{
			if(this.stmt!=null)
			{
				this.stmt.close();
			}
			if(this.conn != null)
			{
				this.conn.close();
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void Execute(String sql) // изпълняване на SQL код
	{
		try 
		{
			this.stmt.executeUpdate(sql);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public ResultSet Select(String sql) // resultset (от селект)
	{
			try 
			{
				return this.stmt.executeQuery(sql);
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			return null;
	}
	
	public static void Init()
	{
		// начална иницализация
		Connection conn = null;
		Statement stmt = null;
		DBConnection db = new DBConnection("Init", conn, stmt);
		
		db.OpenConnection();
		
		System.out.println("Иницализация на таблиците в базата данни....... Готово");
		
		//System.out.println("CREATE TABLE IF NOT EXISTS - Messages...");
		String sql = "CREATE TABLE IF NOT EXISTS `messages` (`Messageid` int(11) NOT NULL,`Personid` int(11) NOT NULL,`Clientid` int(11) NOT NULL,`Message` varchar(255) DEFAULT NULL,`time` timestamp NOT NULL DEFAULT current_timestamp()) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		db.Execute(sql);
		
		
		//System.out.println("CREATE TABLE IF NOT EXISTS - Persons...");
		String sql2 = "CREATE TABLE IF NOT EXISTS `persons` (`Personid` int(11) NOT NULL,`Username` varchar(255) DEFAULT NULL,`Password` varchar(255) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		db.Execute(sql2);
		
		db.CloseConnection();
	}
}
