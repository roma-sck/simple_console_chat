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
			System.out.println("Сервер запущен");
			while (true) 
			{
				Socket socket = server.accept();
				System.out.println("локальный порт: " + server.getLocalPort() + " клиент присоединен, порт: " + socket.getPort());
				// Создаём объект Connection и добавляем его в список
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

	//Закрывает все потоки всех соединений а также серверный сокет
	private void closeAll() 
	{
		try {
			server.close();
			// Перебор всех Connection и вызов метода close() для каждого. Блок
			// synchronized {} необходим для правильного доступа к одним данным их разных нитей
			synchronized(connections) {
				Iterator<Connection> iter = connections.iterator();
				while(iter.hasNext()) 
				{
					((Connection) iter.next()).close();
				}
			}
		} catch (Exception e) {
			System.err.println("Потоки не были закрыты!");
		}
	}

	private class Connection extends Thread 
	{
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
	
		private String name = "";
		private String receiver = "";
	
		public Connection(Socket socket) //сокет, полученный из server.accept()
		{
			this.socket = socket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));	//входной поток
				out = new PrintWriter(socket.getOutputStream(), true);		//выходной поток
	
			} catch (IOException e) {
				e.printStackTrace();
				close();
			}
		}
	
		@Override
		public void run() {
			try {
				//запрашиваем имя пользователя и ожидаем от него сообщений
				name = in.readLine();
				// Отправляем всем клиентам сообщение о том, что зашёл новый пользователь
				synchronized(connections) {
					Iterator<Connection> iter = connections.iterator();
					List<String> online = new ArrayList<>();
					while(iter.hasNext()) 
					{
						Connection con = ((Connection) iter.next());
						con.out.println(name.toUpperCase() + " зашел в чат");
						online.add(con.name);
					}
					String onlineusers = ("пользователи онлайн: " + online);
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
					// Отправляем всем клиентам очередное сообщение
					synchronized(connections) 
					{
						Iterator<Connection> iter = connections.iterator();
						while(iter.hasNext()) 
						{
							Connection choiseclient = ((Connection) iter.next());

							if (choiseclient.name.equals(message[0])) {

								choiseclient.out.println(name.toUpperCase() + " написал: " + message[1]);
							}

						}
					}
				}
				synchronized(connections) 
				{
					Iterator<Connection> iter = connections.iterator();
					while(iter.hasNext()) {
						((Connection) iter.next()).out.println(name.toUpperCase() + " вышел из чата");
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
				// Если больше не осталось соединений, закрываем всё, что есть и завершаем работу сервера
				connections.remove(this);
				if (connections.size() == 0) {
					Server.this.closeAll();
					System.exit(0);
				}
			} catch (Exception e) {
				System.err.println("Потоки не были закрыты!");
			}
		}
	}
}

