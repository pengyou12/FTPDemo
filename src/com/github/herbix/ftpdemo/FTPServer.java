package com.github.herbix.ftpdemo;

import java.net.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FTPServer {
	public static boolean connectClient = true;
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Throwable {
		ServerSocket server = new ServerSocket(21);
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
	public static String fileName;
	public static boolean openDataPort = false;
	public static boolean readFile = false;
	public static boolean isGet = false;
	Thread t;
	public static boolean connectClient = false;
	public int model = 0;//1 means port modle, 2 means passive model
	InetAddress clientAddr;
	int clientPort;
	Socket client;
	InputStream in;
	OutputStream out;
	mulipleServer (Socket client)throws Throwable
	{
		this.client = client;
		in = this.client.getInputStream();
		out = this.client.getOutputStream();
		
	}
	public void run(){
		try{
//		boolean isLogin = false;
		
		out.write(("220 Anonymous FTP server ready."+EOS).getBytes());
		while(true)
		{
			//get the command
		byte[] buffer = new byte[4096];
		in.read(buffer);
		String command = new String(buffer);
		command = command.trim();
		System.out.println(command);
		//USER annoymous
		if(command.equals("USER anonymous"))
		{
			String response = "331 Guest login ok, send your complete e-mail address as password.";
			out.write((response+EOS).getBytes());
			//get the email Address

			in.read(buffer);
			command = new String(buffer);
			command = command.trim();
			String[] email = command.split(" ");
			System.out.println(command);
			System.out.println(email.length);
			if(email.length == 2 && email[0].equals("PASS")&&isEmail(email[1]))
			{
//				isLogin = true;
				break;
			}//login in
			response = "530 invalid email address!";
			out.write((response+EOS).getBytes());
		}
		else
			{
			String response = "500 cannot understand the command, we only support USER anonymous to login in at current!";
			out.write((response+EOS).getBytes());
			}
		}
		responseMeg("230-"
				+ "230-Welcome to"
				+ "230- School of Software\r\n"
				+ "230- FTP Archives at ftp.ssast.org\r\n"
				+ "230-\r\n"
				+ "230-This site is provided as a public service by School of\r\n"
				+ "230-Software. Use in violation of any applicable laws is strictly\r\n"
				+ "230-prohibited. We make no guarantees, explicit or implicit, about the\r\n"
				+ "230-contents of this site. Use at your own risk.\r\n"
				+ "230-\r\n"
				+ "230 Guest login ok, access restrictions apply.");
		//after login in
		while(true)
		{
			String[] command = getCommand();
			if(command == null || command.length < 1)
			{
				responseMeg("500 please input correct command");
				continue;
			}
			switch(command[0])
			{
			case "PORT":
				if(command.length == 2)
				{
					getClientIPPort(command[1]);
					command[1] = command[1].trim();
					openDataPort = true;
					fileName = command[1];
					 //start a new thread to transfrom the data
					t = new portModel(clientAddr, clientPort);
					command[1] = command[1].trim();
					responseMeg("200 Port Model "+command[1]);
					model = 1;
				}
				else
				{
					responseMeg("500 invalid para");
				}
				break;
			case"PASV":
				if(command.length != 1)
				{
					responseMeg("500 invalid para");
					break;
				}
				openDataPort = true;
				model = 2;
				t = new passiveModel();
				t.start();
				String ip= client.getLocalAddress().toString();
				ip = ip.substring(1);
				ip = ip.trim();
				ip = ip.replace('.',',');
				int a = clientPort / 256;
				int b = clientPort % 256;
			
				String response = (ip+","+a+"," +b);
				responseMeg("227 ="+response);
				break;
			case"RETR":
				if(!openDataPort)
				{
					responseMeg("425 user PORT or PASV to set TCP connection first!");
				}
				else if(connectClient)
				{
					responseMeg("426 fail to connect to client!");
				}
				else if(command.length == 2)
				{
					fileName = command[1];
					isGet = false;
					if(model == 1)
					{
						t.start();
						t.join();
					}
					else if(model == 2)
					{
						t.join();
					}
				}
				else
				{
					responseMeg("500 filename error");
				}
				break;
			case"STOR":
				if(!openDataPort)
				{
					responseMeg("425 PORT or PASV at first");
				}
				else
				{
					fileName = command[1];

					isGet = true;
					if(model == 1)
					{
						t.start();
						t.join();
					}
					else if(model == 2)
					{
						t.join();
					}
				}
				break;
			case"ABOR":
			case"QUIT":
				responseMeg("221 Goodbye!");
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
					responseMeg("500 we only support TYPE I");
				}
				break;
				default:
					responseMeg("500 invalid input");
					break;
			}
		}
		
	}
		catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

	public static boolean isEmail(String email) {
		Pattern emailPattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		Matcher matcher = emailPattern.matcher(email);
		if(matcher.find()){
		return true;
		}
		return false;
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

class portModel extends Thread{
	InetAddress clientAddr;
	int clientPort;
	portModel (InetAddress clientAddr,int clientPort)throws Throwable
	{
		this.clientAddr = clientAddr;
		this.clientPort = clientPort;
		
	}
	public void run()
	{
		if(isGet)
		{
			get();
		}
		else
		{
			put();
		}
	}
	public void get()
	{
		try {
			Socket socket = new Socket(clientAddr,clientPort);
			InputStream in = socket.getInputStream();
			byte[] buffer = new byte[4096];
			int count;
			responseMeg("150 download start");
			if(socket.isConnected())
			{
				File localFile = new File(fileName);
				FileOutputStream is = new FileOutputStream(localFile);
				while((count = in.read(buffer))!=-1){	
				//System.out.println(bytes);
				is.write(buffer,0,count);			
				}
				responseMeg("226 update complete.");
				is.close();
			}
			in.close();
			socket.close();
			openDataPort = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			connectClient = false;
			openDataPort = false;
		}
	}
	public void put()
	{
		try {
			File localFile = new File(fileName);
			FileInputStream fis = new FileInputStream(localFile);
			Socket socket = new Socket(clientAddr,clientPort);
			connectClient = true;
			OutputStream out = socket.getOutputStream();
			byte[] buffer = new byte[4096];
			int count;
			responseMeg("150 download start");
			while((count = fis.read(buffer))!=-1){	
				//System.out.println(bytes);
				out.write(buffer,0,count);			
			}
			responseMeg("226 download complete.");
			fis.close();
			out.close();
			socket.close();
			openDataPort = false;
			
		} catch (IOException e) {
			responseMeg("451 cannot open the file");
			connectClient = false;
			openDataPort = false;
		}
	}
}

class passiveModel extends Thread{
	public Socket socket;
	public ServerSocket server;
	passiveModel ()throws Throwable
	{
		this.server = new ServerSocket(0);
		clientPort = this.server.getLocalPort();
		clientAddr = this.server.getInetAddress();
	}
	public void run()
	{
		try {
			socket = server.accept();
			if(isGet)
			{
				get();
			}
			else
			{
				put();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void put(){
		try{
		File localFile = new File(fileName);
		FileInputStream is = new FileInputStream(localFile);
		OutputStream out = socket.getOutputStream();
		int count;
		responseMeg("150 download start");
		while(true){
			byte[] buffer = new byte[4096];
			count = is.read(buffer);	
			if(count == -1 )
			{
				out.write(buffer,0,0);
				break;
			}
			out.write(buffer,0,count);
		}
		is.close();
		server.close();
		openDataPort = false;
		responseMeg("226 download complete.");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			responseMeg("451 cannot open the file");
			openDataPort = false;
			e.printStackTrace();
		}
	}
	public void get()
	{
	
		try {
			File localFile = new File(fileName);
			FileOutputStream is = null;
			is = new FileOutputStream(localFile);
			InputStream in = socket.getInputStream();
			byte[] buffer = new byte[4096];
			int count;
			responseMeg("150 update start");
			while((count = in.read(buffer))!=-1){	
				is.write(buffer,0,count);			
			}
			is.close();
			server.close();
			responseMeg("226 update complete.");
			openDataPort = false;
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				openDataPort = false;
			}
	}
}

}


