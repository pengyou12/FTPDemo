package com.github.herbix.ftpdemo;

//import java.io.InputStream;
//import java.io.OutputStream;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPServer {
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
		int count = in.read(buffer);
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
				count = in.read(buffer);
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
		
		//after login in
		
		
		
		//server.close();
		client.close();
	}
		catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}
	}
	boolean openDataPort = false;
	
	
	

}

class dataPort extends Thread{
	
}
