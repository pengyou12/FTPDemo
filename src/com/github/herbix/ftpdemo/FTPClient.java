package com.github.herbix.ftpdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FTPClient {
	public static String Path = "D:/";
	public static String fileName;
	public static int model = 0;//1 means PORT,2 means PSV
	public static int clientPort;
	public static boolean isGet = false;
	public static OutputStream out;
	public static InputStream in;
	static InetAddress clientAddr;
	public static void main(String[] args) throws Throwable {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		Scanner scanner =new Scanner(System.in);
		String serverHost;
		serverHost = scanner.nextLine();
		Socket socket = new Socket(serverHost, 21);

		out = socket.getOutputStream();
		in = socket.getInputStream();
		String sendCmd = "";
		String line = " ";
		Thread t = null;
		while(!socket.isClosed()) {
			byte[] buffer = new byte[4096];
			//get info from server
			String cmd = new String(line);
			String[] name = cmd.split(" ");
			if(name.length > 1)
			{
				fileName = new String(name[1]);
			}
			switch(sendCmd)
			{
				case"STOR":
					isGet = false;
					break;
				case"RETR":
					isGet = true;
					break;
				default:
					break;
			}
			int count = in.read(buffer);
			if(count < 0)
			{
				break;
			}
			String reply = new String(buffer);
			String serverInfo = getHead(reply);//get the first info of server response
			switch(sendCmd)
			{
				case"PORT":
					if(serverInfo.equals("200"))
					{
						cmd = cmd.trim();
						String[] temp = cmd.split(" ");
						cmd = cmd.trim();
						if(!getPort(temp[1]))
						{
							System.out.print("getPort error");
						}
					}
					t = new portModel(clientPort);//start port model listen to port
					model = 1;
					t.start();
					break;
				case"PASV":
					if(serverInfo.equals("227"))
					{
						String[] temp = reply.split(" ");
						temp[1] = temp[1].substring(1);
						System.out.print(temp[1]);
						if(!getIPPort(temp[1]))
						{
							break;
						}
					}
					t = new passiveModel(clientAddr,clientPort);//start port model listen to port
					model = 2;
					break;
				case"STOR":
					if(serverInfo.equals("150"))
					{
						isGet = false;
						if(model == 1)//port model
						{
							t.join();
						}
						else if(model == 2)
						{
							t.start();
							t.join();
						}
						String re = getReply();
						System.out.print(re);
					}
					break;
				case"RETR":
					if(serverInfo.equals("150"))
					{
						isGet = true;
						if(model == 1)//port model
						{
							t.join();
						}
						else if(model == 2)
						{
							t.start();
							t.join();
						}
						String re = getReply();
						System.out.print(re);
					}
					break;
				default:
					break;
			}
			sendCmd = null;
			System.out.print(new String(buffer, 0, count));
			String line1 = stdin.readLine();//send command to server
			sendCmd = getHead(line1);
			System.out.print("cmd is "+cmd);
			System.out.print("line1 is "+new String(line1));
			out.write(line1.getBytes());
			line = new String(line1);
		}
		socket.close();
		scanner.close();
	
	}

public static String getReply() throws IOException
{
	byte[] temp = new byte[4096];
	in.read(temp);
	String re = new String(temp);
	return re;
}
static class portModel extends Thread{
		int port;
		Socket socket;
		ServerSocket server;
		portModel (int port)throws Throwable
		{
			this.port = port;
			this.server = new ServerSocket(port);
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
		} catch (IOException e) {
			System.out.println("IOException " + e.getMessage());
				e.printStackTrace();
			}
		}
		public void put() throws IOException{
			try{
			File localFile = new File(Path+fileName);
			FileInputStream is = new FileInputStream(localFile);
			OutputStream out = socket.getOutputStream();
			byte[] buffer = new byte[4096];
			int count;
			while((count = is.read(buffer))!=-1){	
				out.write(buffer,0,count);			
			}
			is.close();
			out.close();
			server.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.print("can not open the file" +fileName);
				socket.close();
				server.close();
			}
		}
		public void get()
		{
		
			try {
				File localFile = new File(Path+fileName);
				FileOutputStream is = null;
				is = new FileOutputStream(localFile);
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[4096];
				int count;
				while((count = in.read(buffer))!=-1){	
					is.write(buffer,0,count);			
				}
				is.close();
				server.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		}

static class passiveModel extends Thread{
	int port;
	InetAddress addr;
	passiveModel(InetAddress addr,int port)throws Throwable
	{
		this.port = port;
		this.addr = addr;
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
				File localFile = new File(Path+fileName);
				FileOutputStream is = new FileOutputStream(localFile);
				Socket socket = new Socket(addr,port);
				InputStream in = socket.getInputStream();
				int count;
				while(true){
					byte[] buffer = new byte[4096];
					count = in.read(buffer);
					if(count == -1)
					{
						break;
					}
					is.write(buffer,0,count);			
				}
				is.close();
				in.close();
				socket.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
				
		
	}
	public void put()
	{
		try {
			File localFile = new File(Path+fileName);
			FileInputStream fis = new FileInputStream(localFile);
			Socket socket = new Socket(addr,port);
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
		}
			
	}
	
	}	


	
public static boolean getPort(String para) throws UnknownHostException
{
	para = para.trim();
	String[] temp = para.split(",| ");
	if(temp.length == 6)
	{
		int i = 0;
		for(; i < 6;i++)
		{
			if(Integer.parseInt(temp[i]) <0 || Integer.parseInt(temp[i]) > 255)
			{
				return false;
			}
		}
		clientPort = Integer.parseInt(temp[4])*256 + Integer.parseInt(temp[5]);
		return true;
	}
	return false;
}

public static boolean getIPPort(String para) throws UnknownHostException
{
	try{
		para = para.trim();
	String[] temp = para.split(",| ");
	if(temp.length == 6)
	{
		int i = 4;
		for(; i < 6;i++)
		{
			if(Integer.parseInt(temp[i]) <0 || Integer.parseInt(temp[i]) > 255)
			{
				return false;
			}
		}
		String ip = temp[0]+"."+temp[1]+"."+temp[2]+"."+temp[3];
		clientAddr = InetAddress.getByName(ip);
		clientPort = Integer.parseInt(temp[4])*256 + Integer.parseInt(temp[5]);
		return true;
	}
	return false;
	}
	catch(UnknownHostException e){
		System.out.println("UnknownHostException: " + e.getMessage());
		return false;
	}
}

public static String getHead(String text)
{
	String[] temp = text.split(" ");
	return temp[0];
}

}