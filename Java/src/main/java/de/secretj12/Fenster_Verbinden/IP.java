package de.secretj12.Fenster_Verbinden;

public class IP {
	private String IP;
	private int Port;

	public IP(String IP, int Port) {
		this.IP = IP;
		this.Port = Port;
	}

	public String getIP() {
		return IP;
	}
	
	public int getPort() {
		return Port;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof IP && ((IP) obj).IP.equals(this.IP) && ((IP) obj).Port == this.Port;
	}
	
	@Override
	public String toString() {
		return IP + " - " + Port;
	}
}
