package com.github.herbix.ftpdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class FTPClient {
	public int model = 0;//1 means PORT,2 means PSV
	public static void main(String[] args) throws Throwable {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		Scanner scanner =new Scanner(System.in);
		String serverHost;
		serverHost = scanner.nextLine();
		serverHost = "localhost";
		Socket socket = new Socket(serverHost, 8888);

		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		byte[] buffer = new byte[4096];
		
		Thread t = new portModel(25600,"233.txt");
		t.start();
		
		while(!socket.isClosed()) {
			//get info from server
			int count = in.read(buffer);
			if(count < 0)
			{
				break;
			}
			System.out.print(new String(buffer, 0, count));
			
			String line = stdin.readLine();//send command to server
			//System.out.print(line);
			out.write(line.getBytes());
		}
		
		socket.close();
		scanner.close();
	}

}

class portModel extends Thread{
	int port;
	String file;
	portModel (int port,String file)throws Throwable
	{
		this.file = file;
		this.port = port;
	}
	public void run()
	{
		try {
		File localFile = new File(file);
		FileOutputStream is = null;
		is = new FileOutputStream(localFile);
		ServerSocket server = null;
		server = new ServerSocket(port);
		Socket socket = server.accept();
		InputStream in = socket.getInputStream();
		byte[] buffer = new byte[4096];
		int count;
		while((count = in.read(buffer))!=-1){	
			//System.out.println(bytes);
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