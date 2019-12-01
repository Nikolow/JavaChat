package mysql2;

import java.io.*; 
import java.net.*; 
import java.util.Scanner; 
  
public class Client  
{ 
    final static int ServerPort = 1234; // порта на който слушаме
  
    public static void main(String args[]) throws UnknownHostException, IOException  
    { 
        final Scanner scn = new Scanner(System.in); 
          
        // взимаме локалхоста (може да се смени с ип ако сървъра е на друга машина)
        InetAddress ip = InetAddress.getByName("localhost"); 
          
        // правим връзката
        Socket s = new Socket(ip, ServerPort); 
          
        // двата стрийма (input & output)
        final DataInputStream dis = new DataInputStream(s.getInputStream()); 
        final DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 


        // Thread-a за изпращане на съобщение
        Thread sendMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() 
            { 
                while (true) // постоянен лууп
                {
                	// четем съобщението
                    String msg = scn.nextLine(); 
                      
                    try 
                    {
                        dos.writeUTF(msg); 
                    } 
                    catch (IOException e) 
                    { 
                        e.printStackTrace(); 
                    } 
                } 
            } 
        }); 
          
        // Thread за четене не съобщение
        Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() 
            { 
                while (true) 
                { 
                    try 
                    {
                    	// четем съобщението
                        String msg = dis.readUTF(); 
                        System.out.println(msg); 
                    }
                    catch (IOException e) 
                    {
                        e.printStackTrace(); 
                    } 
                } 
            } 
        }); 
  
        // стартираме ги
        sendMessage.start(); 
        readMessage.start(); 
  
    } 
} 