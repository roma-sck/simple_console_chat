package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Server 
{
	public static void main(String[] args) 
	{
			new Server();
	}
	private List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());
	private ServerSocket server;

	public Server() 
	{
		try {
			server = new ServerSocket(5001);
			System.out.println("Server is running");
			while (true) 
			{
				Socket socket = server.accept();
				System.out.println("local port: " + server.getLocalPort() + " client connected, port: " + socket.getPort());
				// create object Connection and add to the list
				Connection con = new Connection(socket);
				connections.add(con);

				con.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
	}

	//close all threads all connections and server socket
	private void closeAll() 
	{
		try {
			server.close();
			// close all Connections
			// synchronized {} necessary for proper access to some data of different strings
			synchronized(connections) {
				Iterator<Connection> iter = connections.iterator();
				while(iter.hasNext()) 
				{
					((Connection) iter.next()).close();
				}
			}
		} catch (Exception e) {
			System.err.println("Threads were not closed!");
		}
	}

	private class Connection extends Thread 
	{
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
	
		private String name = "";
		private String receiver = "";
	
		public Connection(Socket socket) //socket derived from server.accept()
		{
			this.socket = socket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));	//input stream
				out = new PrintWriter(socket.getOutputStream(), true);		//output stream
	
			} catch (IOException e) {
				e.printStackTrace();
				close();
			}
		}
	
		@Override
		public void run() {
			try {
				//ask for a user name and expect him messages
				name = in.readLine();
				// send a message to all clients that went new user
				synchronized(connections) {
					Iterator<Connection> iter = connections.iterator();
					List<String> online = new ArrayList<>();
					while(iter.hasNext()) 
					{
						Connection con = ((Connection) iter.next());
						con.out.println(name.toUpperCase() + " came into chat");
						online.add(con.name);
					}
					String onlineusers = ("users online: " + online);
					out.println(onlineusers);
				}
				
				String str = "";
				while (true) 
				{
					str = in.readLine();
					String[] message=null;
					if (!str.equals(null)) {

						message=str.split(">");

					}
					if(str.equals("exit")) break;
					// Send another message to all clients
					synchronized(connections) 
					{
						Iterator<Connection> iter = connections.iterator();
						while(iter.hasNext()) 
						{
							Connection choiseclient = ((Connection) iter.next());

							if (choiseclient.name.equals(message[0])) {

								choiseclient.out.println(name.toUpperCase() + " wrote: " + message[1]);
							}

						}
					}
				}
				synchronized(connections) 
				{
					Iterator<Connection> iter = connections.iterator();
					while(iter.hasNext()) {
						((Connection) iter.next()).out.println(name.toUpperCase() + " left the chat");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close();
			}
		}
	
		public void close() 
		{
			try {
				in.close();
				out.close();
				socket.close();
				// If there are no more connections, close all there and finish the job server
				connections.remove(this);
				if (connections.size() == 0) {
					Server.this.closeAll();
					System.exit(0);
				}
			} catch (Exception e) {
				System.err.println("Threads were not closed!");
			}
		}
	}
}

