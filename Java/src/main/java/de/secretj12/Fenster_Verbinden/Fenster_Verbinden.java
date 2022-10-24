package de.secretj12.Fenster_Verbinden;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URL;
import java.util.ResourceBundle;

import de.secretj12.Hauptfenster.Hauptfenster;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Fenster_Verbinden implements Initializable, EventHandler<WindowEvent>, Runnable {
	public static Fenster_Verbinden controller;
	private ObservableList<IP> IPs = FXCollections.observableArrayList();
	private Thread ListeningThread;
	private static Stage stage;
	
	public static void start() throws IOException {
		stage = new Stage();
		stage.setTitle("Verbinden");
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(Hauptfenster.stage);
		stage.getIcons().add(new Image(Hauptfenster.class.getResourceAsStream("icon.png")));
		
		FXMLLoader loader = new FXMLLoader(Fenster_Verbinden.class.getResource("Connect.fxml"));
		Parent root = loader.load();
		controller = loader.getController();		
		stage.setScene(new Scene(root));
		stage.setOnCloseRequest(controller);
		stage.setResizable(false);
		stage.show();
	}
	
	@FXML
	private ListView<IP> ip_list;
	@FXML
	private TextField ip;
	@FXML
	private TextField port;
	
	@FXML
	private void connect() {
		if(!ip.getText().matches("[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}")) {
			new Alert(AlertType.ERROR, "Ungültige IP! (XXX.XXX.XXX.XXX)", ButtonType.OK).showAndWait();
			return;
		}
		if(!port.getText().matches("[0-9]{1,5}")) {
			new Alert(AlertType.ERROR, "Ungültiger Port! (0-65535)", ButtonType.OK).showAndWait();
			return;
		}
		Hauptfenster.controller.connect(ip.getText(), Integer.parseInt(port.getText()));
		ListeningThread.interrupt();
		stage.close();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ip_list.setItems(IPs);
		ListeningThread = new Thread(this);
		ListeningThread.start();
		
		ip_list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<IP>() {

			@Override
			public void changed(ObservableValue<? extends IP> observable, IP oldValue, IP newValue) {
				if(newValue != null) {
					ip.setText(newValue.getIP());
					port.setText("" + newValue.getPort());
				}
			}
		});
	}
	
	
	//on close
	@Override
	public void handle(WindowEvent event) {
		ListeningThread.interrupt();
	}
	
	//Listening Thread
	@Override
	public void run() {
		DatagramSocket socket = null;
		try{
			socket = new DatagramSocket(5673);
		} catch (IOException e) {}
		if(socket == null) return;
		
		while(!Thread.interrupted()) {
			try {
				byte[] buf = new byte[6];
		        DatagramPacket packet = new DatagramPacket(buf, buf.length);
		        socket.receive(packet);
		        
		        IP ip = new IP(((int) 0xff & buf[0]) + "." +
		        		((int) 0xff & buf[1]) + "." +
		        		((int) 0xff & buf[2]) + "." +
		        		((int) 0xff & buf[3]),
		        		(((int) 0xff & (buf[4])) << 8) | ((int) 0xff & buf[5]));
		        if(!IPs.contains(ip)) IPs.add(ip);
			} catch (Exception e) {}
		}
		socket.close();
	}
}
