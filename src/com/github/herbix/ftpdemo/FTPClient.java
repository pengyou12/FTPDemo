package com.github.herbix.ftpdemo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class FTPClient {

	public static void main(String[] args) throws Throwable {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		Socket socket = new Socket("localhost", 8888);

		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		byte[] buffer = new byte[4096];

		String line = stdin.readLine();
		out.write(line.getBytes());
		
		while(true) {
			int count = in.read(buffer);
			if(count < 0) {
				break;
			}
			System.out.print(new String(buffer, 0, count));
		}
		
		socket.close();
	}

}
