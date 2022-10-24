package de.secretj12.Pendel;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PendelConnection implements Runnable {
	private Socket pendel;
	private Thread thread;
	private ArrayList<DataListener> DataListeners;
	private ArrayList<CloseListener> CloseListeners;

	/**
	 * Erstellt eine neue Verbindung zu einem de.secretj12.Pendel
	 * @param ip IP des Pendels als String
	 * @param port Port des Pendels als String
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public PendelConnection(String ip, int port) throws UnknownHostException, IOException {
		pendel = new Socket(ip, port);
		thread = new Thread(this);
		thread.setName("PendelConnection");
		thread.start();
		DataListeners = new ArrayList<>();
		CloseListeners = new ArrayList<>();
	}
	
	/**
	 * Schließt die Verbindung zum de.secretj12.Pendel, sollte am Ende aufgerufen werden
	 */
	public void close() {
		thread.interrupt();
		try {
			pendel.close();
		} catch (IOException e) {}

		for(CloseListener closeListener : CloseListeners) {
			closeListener.onClose();
		}
		de.secretj12.Hauptfenster.Hauptfenster.pendel = null;
		de.secretj12.Hauptfenster.Hauptfenster.controller.disconnect();
	}

	@Override
	public void run() {
		try {
			byte[] buf = new byte[25];
			InputStream in = pendel.getInputStream();

			long lastData = System.currentTimeMillis();
			while(!Thread.interrupted() & !pendel.isClosed()) {
				if(System.currentTimeMillis() - lastData > 5 * 1000) close();
				if(in.available() >= 25) {
					lastData = System.currentTimeMillis();
					in.read(buf);
					int check = 0;
					for(int i = 0; i < 24; i++) {
						check = (check + ((int) 0xff & buf[i])) % 256;
					}
					if(check != ((int) 0xff & buf[24])) {
						System.out.println("Checksum failed");
						continue;
					}
					
					GyroData data = new GyroData(buf);
					for(DataListener listener : DataListeners) {
						listener.update(data);
					}
				}
			}
		} catch (IOException e) {}
		if(!Thread.interrupted()) close();
	}
	
	public interface DataListener {
		/**
		 * Wird aufgerufen wenn neue Daten vom de.secretj12.Pendel vorhanden sind
		 * @param data Daten des Pendels
		 */
		public void update(GyroData data);
	}
	
	/**
	 * Fügt neuen DataListener hinzu
	 * @param dataListener der DataListener
	 */
	public void addDataListener(DataListener dataListener) {
		DataListeners.add(dataListener);
	}
	
	public interface CloseListener {
		/**
		 * Wird aufgerufen wenn neue de.secretj12.Pendel-Verbindung geschlossen wurde
		 */
		public void onClose();
	}
	
	/**
	 * Fügt neuen CloseListener hinzu
	 * @param closeListener der CloseListener
	 */
	public void addCloseListener(CloseListener closeListener) {
		CloseListeners.add(closeListener);
	}
}