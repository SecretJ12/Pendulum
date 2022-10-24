#include "MPU6050.h"
#include "math.h"
SYSTEM_MODE(MANUAL);

MPU6050 accelgyro;
int16_t ax, ay, az;
int16_t gx, gy, gz;
float g = 9.81;

UDP Udp;
IPAddress multiIP(255, 255, 255, 255);
int multiPort = 5673;

int serverPort = 4754;
TCPServer server = TCPServer(serverPort);
TCPClient client;
IPAddress ownIP;
byte ownIPPackage[6];

/*
 * 0 = +/- 2g
 * 1 = +/- 4g
 * 2 = +/- 8g
 * 3 = +/- 16g


 * 0 = +/- 250 degrees/sec
 * 1 = +/- 500 degrees/sec
 * 2 = +/- 1000 degrees/sec
 * 3 = +/- 2000 degrees/sec
*/

void setup() {
    Serial.begin(9600);

    Particle.disconnect();
    WiFi.connect();
    RGB.control(true);
    RGB.color(255, 0, 0);
    while(!WiFi.ready()) listen();
    ownIP = WiFi.localIP();
    ownIPPackage[0] = ownIP[0];
    ownIPPackage[1] = ownIP[1];
    ownIPPackage[2] = ownIP[2];
    ownIPPackage[3] = ownIP[3];
    ownIPPackage[4] = (byte) (serverPort >> 8);
    ownIPPackage[5] = (byte) serverPort;
    RGB.color(255, 69, 0);

    Udp.begin(multiPort);
    Udp.joinMulticast(multiIP);

    Wire.begin();

    accelgyro.initialize();
    accelgyro.setFullScaleAccelRange(0x01);
    accelgyro.setFullScaleGyroRange(0x00);

    accelgyro.setXAccelOffset(-3733);
    accelgyro.setYAccelOffset(1887);
    accelgyro.setZAccelOffset(555);
    accelgyro.setXGyroOffset(165);
    accelgyro.setYGyroOffset(-53);
    accelgyro.setZGyroOffset(28);

    server.begin();
}

void loop() {
    //Solange kein Client verbunden ist, sende IP Adresse an alle Geräte im Netzwerk
    RGB.color(255, 69, 0);
    while(!client.connected()) {
        delay(1000);
        if(System.buttonPushed() > 0) listen();
        delay(1000);
        if(System.buttonPushed() > 0) listen();
        delay(1000);
        if(System.buttonPushed() > 0) listen();

        RGB.color(0, 0, 255);
        Udp.sendPacket(ownIPPackage, 6, multiIP, multiPort);
        Udp.endPacket();
        delay(100);
        RGB.color(255, 69, 0);

        client = server.available();
    }

    //Sende Daten an verbundenen Client
    RGB.color(0, 255, 0);
    byte buffer[25];
    while(client.connected()) {
        //Lese Beschleunigung und Drehung
        accelgyro.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
        buffer[24] = 0;

        float fax = (float) ax / (8192) * g; //* 1.1541;
        float fay = (float) ay / (8192) * g; //* 1.2186;
        float faz = (float) az / (8192) * g; //* 1.1963;

        float fgx = (float) gx / 250;
        float fgy = (float) gy / 250;
        float fgz = (float) gz / 250;

        floatToByte(buffer, 0, fax);
        floatToByte(buffer, 4, fay);
        floatToByte(buffer, 8, faz);
        floatToByte(buffer, 12, fgx);
        floatToByte(buffer, 16, fgy);
        floatToByte(buffer, 20, fgz);

        for(int i = 0; i < 24; i++) {
            buffer[24] = (buffer[24] + buffer[i]) % 256;
        }

        client.write(buffer, 25);
        delay(100);
    }
}

void floatToByte(byte* arr, int pos, float value) {
     long l = *(long*) &value;

     arr[pos + 3] = l & 0x00FF;
     arr[pos + 2] = (l >> 8) & 0x00FF;
     arr[pos + 1] = (l >> 16) & 0x00FF;
     arr[pos + 0] = l >> 24;
}

void listen() {
    RGB.color(0, 0, 255);
    WiFi.listen();
}