package com.github.herbix.ftpdemo;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer {

	public static void main(String[] args) throws Throwable {
		ServerSocket server = new ServerSocket(8888);
		Socket client = server.accept();

		InputStream in = client.getInputStream();
		OutputStream out = client.getOutputStream();
		byte[] buffer = new byte[4096];
		while(true)
		{
		int count = in.read(buffer);
		out.write("Server Back: ".getBytes());
		out.write(buffer, 0, count);
		out.write("\n".getBytes());
		}
		//server.close();
		//client.close();
	}

}
