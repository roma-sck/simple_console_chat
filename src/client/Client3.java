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
			// Подключаемся в серверу и получаем потоки(in и out) для передачи сообщений
			socket = new Socket(ip, 5001);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.println("клиент подключен к серверу " + socket.getInetAddress() + " на порт " + socket.getLocalPort());
			System.out.println("для отправки сообщений введите \"ник>сообщение\" ");

			System.out.println("Введите свой ник:");
			out.println(scan.nextLine());

			// Запускаем вывод всех входящих сообщений в консоль
			Resender resend = new Resender();
			resend.start();

			// Пока пользователь не введёт "exit" отправляем на сервер всё, что введено из консоли
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
			System.err.println("Потоки не были закрыты!");
		}
	}
	
	private class Resender extends Thread {

		private boolean stoped;
		
		public void setStop() {
			stoped = true;
		}

		
		//Считывает все сообщения от сервера и печатает их в консоль. Останавливается вызовом метода setStop()
	    @Override
		public void run() {
			try {
				while (!stoped) {
					String str = in.readLine();
					System.out.println(str);
				}
			} catch (IOException e) {
				System.err.println("Ошибка при получении сообщения.");
				e.printStackTrace();
			}
		}
	}

}