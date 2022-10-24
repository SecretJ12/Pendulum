package de.secretj12.Hauptfenster;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.secretj12.Fenster_Verbinden.Fenster_Verbinden;
import de.secretj12.JSON.JSON;
import de.secretj12.JSON.JSONCollection;
import de.secretj12.JSON.ParseException;
import de.secretj12.Pendel.GyroData;
import de.secretj12.Pendel.PendelConnection;
import de.secretj12.Pendel.PendelConnection.DataListener;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.WindowEvent;

public class Hauptfenster_controller implements Initializable, DataListener, EventHandler<WindowEvent> {
	
	@FXML
	private Button connect;

	@FXML
	private CheckBox line_a;
	@FXML
	private CheckBox line_v;
	@FXML
	private CheckBox line_x;

	@FXML
	private TextField g_time;
	@FXML
	private TextField g_height;
	@FXML
	private Button pause;

	@FXML
	private Button rec_start;
	@FXML
	private Button rec_save;
	@FXML
	private Button rec_load;
	@FXML
	private Button rec_delete;
	
	private boolean isRec = false;
	private boolean showsRec = false;
	
	@FXML
	private LineChart<Number, Number> linechart;
	private NumberAxis yAxis;
	private NumberAxis xAxis;
	private int timeSpan = 30;
	private double pauseTime;
	private boolean paused = false;
	private Series<Number, Number> series_a = new Series<>();
	private Series<Number, Number> series_v = new Series<>();
	private Series<Number, Number> series_x = new Series<>();
	
	@FXML
	private Text r_gx;
	@FXML
	private Text r_gy;
	@FXML
	private Text r_gz;
	@FXML
	private Text r_gb;
	@FXML
	private Text r_ax;
	@FXML
	private Text r_ay;
	@FXML
	private Text r_az;
	
	@FXML
	private void connect() {
		if(Hauptfenster.pendel == null) {
			try {
				Fenster_Verbinden.start();
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			disconnect();
		}
	}
	
	@FXML
	private void change_line_a() {
		if(line_a.isSelected()) linechart.getData().add(series_a);
		else linechart.getData().remove(series_a);
		setYLegend();
	}
	
	@FXML
	private void change_line_v() {
		if(line_v.isSelected()) linechart.getData().add(series_v);
		else linechart.getData().remove(series_v);
		setYLegend();
	}
	
	@FXML
	private void change_line_x() {
		if(line_x.isSelected()) linechart.getData().add(series_x);
		else linechart.getData().remove(series_x);
		setYLegend();
	}
	
	private void setYLegend() {
		String s = (line_a.isSelected()?"***m/s²":"") +
				(line_v.isSelected()?" | m/s":"") +
				(line_x.isSelected()?" | m":"");
		yAxis.setLabel(s.length() > 2?s.substring(3):"");
	}

	@FXML
	private void change_g_time() {
		try {
			timeSpan = Integer.parseInt(g_time.getText());
		} catch (NumberFormatException e) {}
		if(timeSpan > 50) {
			timeSpan = 50;
			g_time.setText("50");
		}
		
		if(!showsRec) {
			xAxis.setLowerBound(pauseTime - timeSpan);
			xAxis.setUpperBound(pauseTime);
		}
	}

	@FXML
	private void change_g_height() {
		NumberAxis yAxis = (NumberAxis) linechart.getYAxis();
		try {
			int range = Integer.parseInt(g_height.getText());
			yAxis.setLowerBound(-range);
			yAxis.setUpperBound(range);
		} catch (Exception e) {
			yAxis.setLowerBound(-12);
			yAxis.setUpperBound(12);
		}
	}
	
	@FXML
	private void pause() {
		paused = !paused;
		if(!paused) dataClear();
		pause.setText(paused?"Weiter":"Pause");
	}
	
	@FXML
	private void rec_start() {
		if(!isRec) startRec();
		else stopRec();
	}
	
	private void startRec() {
		rec_start.setText("Aufzeichnung beenden");
		dataClear();
		
		isRec = true;
		showsRec = false;
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(0);
		rec_save.setDisable(true);
		rec_load.setDisable(true);
		pause.setDisable(true);
	}
	
	private void stopRec() {
		rec_start.setText("Aufzeichnung starten");
		showsRec = true;
		isRec = false;
		rec_save.setDisable(false);
		rec_load.setDisable(false);
	}
	
	@FXML
	private void rec_save() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Aufnahme speichern");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("de.secretj12.Pendel Daten", "*.jpendel"));
		File f = fileChooser.showSaveDialog(Hauptfenster.stage);
		
		if(f == null) return;
		
		JSONCollection json = new JSONCollection();

		for(Data<Number, Number> data : series_a.getData()) {
			json.getCollection(data.getXValue() + "").add("A", data.getYValue());
		}
		for(Data<Number, Number> data : series_v.getData()) {
			json.getCollection(data.getXValue() + "").add("V", data.getYValue());
		}
		for(Data<Number, Number> data : series_x.getData()) {
			json.getCollection(data.getXValue() + "").add("X", data.getYValue());
		}
		try {
			JSON.save(f, json);
		} catch (IOException e) {
			new Alert(AlertType.ERROR, "Ungültige Datei", ButtonType.OK).showAndWait();
		}
	}
	
	@FXML
	private void rec_load() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Aufnahme speichern");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("de.secretj12.Pendel Daten", "*.jpendel"));
		File f = fileChooser.showOpenDialog(Hauptfenster.stage);
		
		if(f == null) return;
		
		pause.setDisable(true);
		dataClear();
		showsRec = true;
		
		JSONCollection json;
		try {
			json = (JSONCollection) JSON.parse(f);
			double highestValue = 0;
			for(String name : json.getNames()) {
				double value = Double.parseDouble(name);
				highestValue = Math.max(highestValue, value);
				series_a.getData().add(new Data<>(value, json.getCollection(name).getDecimal("A")));
				series_v.getData().add(new Data<>(value, json.getCollection(name).getDecimal("V")));
				series_x.getData().add(new Data<>(value, json.getCollection(name).getDecimal("X")));
			}
			
			xAxis.setLowerBound(0);
			xAxis.setUpperBound(highestValue);
		} catch (ParseException | IOException e) {
			System.out.println(e);
			new Alert(AlertType.ERROR, "Ungültige Datei", ButtonType.OK).showAndWait();
		}
	}
	
	@FXML
	private void rec_delete() {
		stopRec();
		dataClear();
		isRec = false;
		showsRec = false;
		paused = false;
		rec_save.setDisable(true);
		rec_delete.setDisable(false);
		pause.setDisable(false);
		pause.setText("Pause");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		rec_start.setDisable(true);
		rec_save.setDisable(true);
		rec_delete.setDisable(true);
		
		series_a.setName("A");
		series_v.setName("V");
		series_x.setName("X");
		linechart.getData().addAll(series_a, series_v, series_x);
		yAxis = (NumberAxis) linechart.getYAxis();
		try {
			int range = Integer.parseInt(g_height.getText());
			yAxis.setLowerBound(-range);
			yAxis.setUpperBound(range);
		} catch (Exception e) {
			yAxis.setLowerBound(-12);
			yAxis.setUpperBound(12);
		}
		yAxis.setAutoRanging(false);
		yAxis.setTickMarkVisible(false);
		yAxis.setTickLabelsVisible(false);
		xAxis = (NumberAxis) linechart.getXAxis();
		xAxis.setAutoRanging(false);
		xAxis.setTickUnit(1);
		xAxis.setMinorTickCount(4);
		xAxis.setLowerBound(pauseTime - timeSpan);
		xAxis.setUpperBound(pauseTime);
		setYLegend();
		linechart.setCreateSymbols(false);
		linechart.setAnimated(false);
	}
	
	private void dataClear() {
		series_a.getData().clear();
		series_v.getData().clear();
		series_x.getData().clear();
		currentTime = 0;
	}
 	
	public void connect(String ip, int port) {
		try {
			Hauptfenster.pendel = new PendelConnection(ip, port);
			connect.setText("Trennen");
			pause.setDisable(false);
			rec_start.setDisable(false);
			rec_save.setDisable(true);
			rec_delete.setDisable(false);
			
			dataClear();
			showsRec = false;
			
			Hauptfenster.pendel.addDataListener(this);
		} catch (IOException e) {
			System.out.println(e);
			new Alert(AlertType.ERROR, "Verbindung fehlgeschlagen.", ButtonType.OK).showAndWait();
		}
	}
	
	public void disconnect() {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				connect.setText("Verbinden");
				rec_start.setDisable(true);
				rec_save.setDisable(!showsRec);
				rec_delete.setDisable(!showsRec);
				stopRec();
			}
		});
		if(Hauptfenster.pendel != null) Hauptfenster.pendel.close();
	}
	
	float lastv = 0;
	int pendelmode = 0;
	private double currentTime = 0;
	@Override
	public void update(GyroData data) {
		if(!paused) {
			r_ax.setText(round(data.getAX()) + " m/s²");
			r_ay.setText(round(data.getAY()) + " m/s²");
			r_az.setText(round(data.getAZ()) + " m/s²");
			r_gb.setText(round(data.getA())  + " m/s²");
			
			r_gx.setText(round(data.getGX()) + " °/s");
			r_gy.setText(round(data.getGY()) + " °/s");
			r_gz.setText(round(data.getGZ()) + " °/s");
		}
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(!showsRec && !paused) {
					boolean formel = false;
					if(formel) {
						double asquared = Math.pow(9.81, 2) - Math.pow(Math.abs(data.getAZ()) - Math.abs((Math.pow(data.getGX()/180*Math.PI, 2) * 1)), 2);
						double a = 0.9*(lastv< data.getGX()?-1:1) * Math.sqrt(Math.abs(asquared));
						lastv = data.getGX();
						
						if(Math.abs(data.getGX()) > 20) pendelmode = 5;
						if(pendelmode > 0) 
							pendelmode--;
						else
							a = data.getAY();
						
						series_a.getData().add(new Data<Number, Number>(currentTime, a));
						series_v.getData().add(new Data<Number, Number>(currentTime, data.getGX()/20));
						series_x.getData().add(new Data<Number, Number>(currentTime, -a));
					} else {
						if(Math.abs(data.getAY()) < 1) pendelmode = 10;
						if(pendelmode > 0) {
							series_a.getData().add(new Data<Number, Number>(currentTime, (data.getGX() - lastv)/10));
							series_x.getData().add(new Data<Number, Number>(currentTime, (-data.getGX() + lastv)/10));
							pendelmode--;
						} else {
							series_a.getData().add(new Data<Number, Number>(currentTime, data.getAY()));
							series_x.getData().add(new Data<Number, Number>(currentTime, -data.getAY()));
						}

						series_v.getData().add(new Data<Number, Number>(currentTime, (data.getGX())/10));
						lastv = data.getGX();
					}
										
					if(currentTime > 50 && !isRec) {
						series_a.getData().remove(0);
						series_v.getData().remove(0);
						series_x.getData().remove(0);
					}
				}
				
				if(isRec && currentTime > 50) stopRec();

				if(!showsRec) {
					if(!isRec) {
						if(!paused) {
							xAxis.setLowerBound(currentTime - timeSpan);
							xAxis.setUpperBound(currentTime);
							pauseTime = currentTime;
						} else {
							xAxis.setLowerBound(pauseTime - timeSpan);
							xAxis.setUpperBound(pauseTime);
						}
					} else {
						xAxis.setLowerBound(0);
						xAxis.setUpperBound(currentTime);
					}
				}
				
				currentTime += 0.1;
			}
		});
	}
	
	private float round(float f) {
		return ((float) Math.round(f * 100)) / 100;
	}

	//on Close
	@Override
	public void handle(WindowEvent event) {
		disconnect();
		System.exit(0);
	}
}
