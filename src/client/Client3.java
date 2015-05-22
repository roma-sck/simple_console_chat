package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client3 {
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;

	public static void main(String[] args) {
		new Client3();
	}

	public Client3() {
		Scanner scan = new Scanner(System.in);

		String ip = "127.0.0.1";

		try {
			// connect to the server and receive streams (in and out) to send messages
			socket = new Socket(ip, 5001);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.println("client connected to the server " + socket.getInetAddress() + " on port " + socket.getLocalPort());
			System.out.println("to send a message enter \"nickname>message\" ");

			System.out.println("Enter your nickname:");
			out.println(scan.nextLine());

			// Run the withdrawal of all incoming messages to the console
			Resender resend = new Resender();
			resend.start();

			// As long as the user has entered "exit" will send to the server all that entered from the console
			String str = "";
			while (!str.equals("exit")) {
				str = scan.nextLine();
				out.println(str);
			}
			resend.setStop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	private void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			System.err.println("Threads were not closed!");
		}
	}
	
	private class Resender extends Thread {

		private boolean stoped;
		
		public void setStop() {
			stoped = true;
		}

		
		//Reads all messages from the server and prints them to the console. Stops by calling setStop ()
	    @Override
		public void run() {
			try {
				while (!stoped) {
					String str = in.readLine();
					System.out.println(str);
				}
			} catch (IOException e) {
				System.err.println("Error getting message.");
				e.printStackTrace();
			}
		}
	}

}