package de.secretj12.Pendel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GyroData {
	private float ax;
	private float ay;
	private float az;
	private float gx;
	private float gy;
	private float gz;

	/**
	 * Liest die Daten aus den gesendeten bytes des Pendels
	 * @param buffer Erhaltene Daten von de.secretj12.Pendel
	 */
	public GyroData(byte[] buffer) {
		ByteBuffer ByteBuf = ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

		ax = ByteBuf.getFloat();
		ay = ByteBuf.getFloat();
		az = ByteBuf.getFloat();
		gx = ByteBuf.getFloat();
		gy = ByteBuf.getFloat();
		gz = ByteBuf.getFloat();
	}
	
	/**
	 * Erzeugt neues Daten aus einzelnen Werten
	 * @param ax Beschleunigung in X-Richtung
	 * @param ay Beschleunigung in Y-Richtung
	 * @param az Beschleunigung in Z-Richtung
	 * @param gx Drehgeschwindigkeit um X-Achse
	 * @param gy Drehgeschwindigkeit um Y-Achse
	 * @param gz Drehgeschwindigkeit um Z-Achse
	 */
	public GyroData(float ax, float ay, float az, float gx, float gy, float gz) {
		this.ax = ax;
		this.ay = ay;
		this.az = az;
		this.gx = gx;
		this.gy = gy;
		this.gz = gz;
	}
	
	/**
	 * @return Beschleunigung in X-Richtung
	 */
	public float getAX() {
		return ax;
	}
	
	/**
	 * @return Beschleunigung in Y-Richtung
	 */
	public float getAY() {
		return ay;
	}

	/**
	 * @return Beschleunigung in Z-Richtung
	 */
	public float getAZ() {
		return az;
	}
	
	/**
	 * @return Gesamtbeschleunigung
	 */
	public float getA() {
		return (float) Math.sqrt(Math.pow(getAX(), 2) + Math.pow(getAY(), 2) + Math.pow(getAZ(), 2));
	}
	
	/**
	 * @return Drehgeschwindigkeit um X-Achse
	 */
	public float getGX() {
		return gx;
	}
	
	/**
	 * @return Drehgeschwindigkeit um Y-Achse
	 */
	public float getGY() {
		return gy;
	}
	
	/**
	 * @return Drehgeschwindigkeit um Z-Achse
	 */
	public float getGZ() {
		return gz;
	}

	@Override
	public String toString() {
		return "AX: " + ax + " - AY: " + ay + " - AZ: " + az + "\n" + "GX: " + gx + " - GY: " + gy + " - GZ: " + gz;
	}
}
