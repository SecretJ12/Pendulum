package de.secretj12.Pendel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class PendelListener implements Runnable {
	private Thread ListeningThread;
	
	/**
	 * Wird aufgerufen wenn ein neues de.secretj12.Pendel gefunden wird
	 * @param IP IP des Pendels als String
	 * @param port Port des Pendels als Integer
	 * @param pendelListener Der PendelListener selbst (beispielsweise um ihn zu stoppen)
	 */
	public abstract void newPendel(String IP, int port, PendelListener pendelListener);
	
	/**
	 * Listener schaut nach neuen Pendeln, erst nach dieser Methode wird "newPendel" aufgerufen
	 */
	public void startListening() {
		if(ListeningThread == null) {
			ListeningThread = new Thread(this);
			ListeningThread.setName("ListeningThread");
			ListeningThread.start();
		}
	}
	
	@Override
	public void run() {
		DatagramSocket socket = null;
		try{
			socket = new DatagramSocket(5673);
			socket.setSoTimeout(10000);
		} catch (IOException e) {}
		if(socket == null) return;
		
		while(!Thread.interrupted()) {
			try {
				byte[] buf = new byte[6];
		        DatagramPacket packet = new DatagramPacket(buf, buf.length);
		        socket.receive(packet);
		        
		        newPendel(((int) 0xff & buf[0]) + "." +
		        		((int) 0xff & buf[1]) + "." +
		        		((int) 0xff & buf[2]) + "." +
		        		((int) 0xff & buf[3]),
		        		(((int) 0xff & (buf[4])) << 8) | ((int) 0xff & buf[5]),
		        		this);
			} catch (Exception e) {}
		}
		socket.close();
	}
	
	/**
	 * Listener achtet nicht mehr auf de.secretj12.Pendel
	 */
	public void stopListening() {
		ListeningThread.interrupt();
		ListeningThread = null;
	}
 }
