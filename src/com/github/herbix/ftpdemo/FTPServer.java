package com.github.herbix.ftpdemo;

//import java.io.InputStream;
//import java.io.OutputStream;
import java.net.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FTPServer {
	public static boolean connectClient = true;
	public static void main(String[] args) throws Throwable {
		ServerSocket server = new ServerSocket(8888);
		while(true)
		{
			Socket client = server.accept();
			Thread t = new mulipleServer(client);
			t.start();	
		}
		
	}

}

class mulipleServer extends Thread{
	static String EOS = "\r\n";
	Thread t;
	public boolean connectClient = false;
	public boolean openPORT = false;
	public boolean openPASV = false;
	InetAddress clientAddr;
	int clientPort;
	Socket client;
	InputStream in;
	OutputStream out;
		public static boolean isEmail(String email) {
		Pattern emailPattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		Matcher matcher = emailPattern.matcher(email);
		if(matcher.find()){
		return true;
		}
		return false;
	}
	mulipleServer (Socket client)throws Throwable
	{
		this.client = client;
		in = this.client.getInputStream();
		out = this.client.getOutputStream();
		
	}
	public void run(){
		try{
		boolean isLogin = false;
		
		out.write(("220 ftp.ssast.org FTP server ready"+EOS).getBytes());
		while(!isLogin)
		{
			//get the command
		byte[] buffer = new byte[4096];
		in.read(buffer);
		String command = new String(buffer);
		command = command.trim();
		//command = command.substring(0,14);
		System.out.println(command);
		System.out.println("ab");
		//USER annoymous
		if(command.equals("233"))
		{
			String response = "331 Guest login ok, send your complete e-mail address as password.";
			out.write((response+EOS).getBytes());
			//get the email Address
			while(true)
			{
				in.read(buffer);
				command = new String(buffer);
				command = command.trim();
				String[] email = command.split(" ");
				System.out.println(command);
				System.out.println(email.length);
				if(email.length == 2 && email[0].equals("PASS")&&isEmail(email[1]))
				{
					isLogin = true;
					break;
				}//login in
				response = "please input your email address:PASS XX@XX.com";
				out.write((response+EOS).getBytes());
			}
		}
		else
			{
			String response = "please login in at first";
			out.write((response+EOS).getBytes());
			}
		}
		responseMeg("login success");
		//after login in
		boolean openDataPort = false;
		while(true)
		{
			String[] command = getCommand();
			if(command.length < 1)
			{
				responseMeg("please input correct command");
				continue;
			}
			switch(command[0])
			{
			case "PORT":
				if(command.length == 2)
				{
					getClientIPPort(command[1]);
					openDataPort = true;

					 //start a new thread to transfrom the data
					t = new dataPort(clientAddr, clientPort);
					responseMeg("200 Port Model "+command[1]);
					t.start();
					t.join();//shanchu
				}
				else
				{
					responseMeg("invalid para");
				}
				break;
			case"PASV":
				
				break;
			case"RETR":
				if(!openDataPort)
				{
					responseMeg("PORT or PASV at first");
				}
				else
				{
					t.join();
					responseMeg("t join");
				}
				break;
			case"STOR":
				if(!openDataPort)
				{
					responseMeg("PORT or PASV at first");
				}
				else
				{
					
				}
				break;
			case"ABOR":
			case"QUIT":
				responseMeg("client close");
				client.close();
				return;
			case"SYST":
				responseMeg("215 UNIX Type: L8");
				break;
			case"TYPE":
				if(command.length == 2 && command[1].equals("I"))
				{
					responseMeg("200 Type set to I.");	
				}
				else
				{
					responseMeg("we only support TYPE I");
				}
				break;
				default:
					responseMeg("invalid input");
					break;
			}
		}
		
		//transform file
		

		//client.close();
	}
		catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

public void getClientIPPort(String para) throws UnknownHostException
{
	try{
	String[] temp = para.split(",| ");
	if(temp.length == 6)
	{
		int i = 0;
		for(; i < 6;i++)
		{
			if(Integer.parseInt(temp[i]) <0 || Integer.parseInt(temp[i]) > 255)
			{
				break;
			}
		}
		if(i != 6)
		{
			responseMeg("number is out of 0-255");
		}
		String ip = temp[0]+"."+temp[1]+"."+temp[2]+"."+temp[3];
		clientAddr = InetAddress.getByName(ip);
		clientPort = Integer.parseInt(temp[4])*256 + Integer.parseInt(temp[5]);
		return;
	}
	responseMeg("the para is wrong");
	}
	catch(UnknownHostException e){
		System.out.println("UnknownHostException: " + e.getMessage());
	}
}
public String[] getCommand()
{
	try{
	String temp;
	byte[] buffer = new byte[4096];
	in.read(buffer);
	temp = new String(buffer);
	temp = temp.trim();
	String[] command = temp.split(" ");
	return command;
	}
	catch(IOException e){
		String[] temp = null;
		System.out.println("IOException: " + e.getMessage());
		return temp;
	}
}

public void responseMeg(String response)
{
	try{
		out.write((response+EOS).getBytes());
	}
	catch(IOException e){
		System.out.println("IOException: " + e.getMessage());
	}
}

class dataPort extends Thread{
	InetAddress clientAddr;
	int clientPort;
	dataPort (InetAddress clientAddr,int clientPort)throws Throwable
	{
		this.clientAddr = clientAddr;
		this.clientPort = clientPort;
		
	}
	public void run()
	{
		try {
			System.out.print(clientAddr);
			System.out.print(clientPort);
			File localFile = new File("te.txt");
			FileInputStream fis = new FileInputStream(localFile);
			Socket socket = new Socket(clientAddr,clientPort);
			connectClient = true;
			OutputStream out = socket.getOutputStream();
			byte[] buffer = new byte[4096];
			int count;
			while((count = fis.read(buffer))!=-1){	
				//System.out.println(bytes);
				out.write(buffer,0,count);			
			}
			fis.close();
			out.close();
			socket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			connectClient = false;
		}
	}
}

class passiveModel extends Thread{
	Socket client;
	InputStream in;
	OutputStream out;
	passiveModel (Socket client)throws Throwable
	{
		this.client = client;
		in = this.client.getInputStream();
		out = this.client.getOutputStream();
		
	}
	public void run()
	{
		
	}
}

}


