package com.github.herbix.ftpdemo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class FTPClient {

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
