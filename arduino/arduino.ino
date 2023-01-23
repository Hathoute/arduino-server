#include <ESP8266WiFi.h>

#ifndef STASSID
#define STASSID "POCO M4 Pro 5G"
#define STAPSK "a1b2b2b2"
#endif

const char* ssid = STASSID;
const char* password = STAPSK;

const char* host = "arduino.staging.hathoute.com";
const uint16_t port = 8181;

const char* HEADER = "gzsh";
const char* GAZ_ID = "BUT";

#define GAZ_PIN A0

void setup() {
  Serial.begin(115200);

  // We start by connecting to a WiFi network

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  /* Explicitly set the ESP8266 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  float value = read_gaz_sensor();

  Serial.print("Gaz value: ");
  Serial.print(value);
  Serial.println();


  WiFiClient client;
  connect_server(&client);
  write_header(&client);
  send_value(&client, value);
  client.stop();

  delay(2000);
}

int read_gaz_sensor() {
  unsigned int sensorValue = analogRead(GAZ_PIN);  // Read the analog value from sensor
  unsigned int outputValue = map(sensorValue, 0, 1023, 0, 255); // map the 10-bit data to 8-bit data
  return outputValue;
}

int send_value(WiFiClient* client, float value)  {
  client->write(1);
  client->print(GAZ_ID);
  byte *b = (byte *)&value;
  client->write(b[3]);
  client->write(b[2]);
  client->write(b[1]);
  client->write(b[0]);

  while(!client->available()) {
    delay(50);
  }

  while(client->available()) {
    char c = client->read();
    Serial.print(c);
  }

  Serial.println();
  return 0;
}

int connect_server(WiFiClient* client) {
  Serial.print("Connecting to ");
  Serial.print(host);
  Serial.print(":");
  Serial.print(port);
  Serial.println();

  if (!client->connect(host, port)) {
    Serial.println("Connection failed");
    delay(5000);
    return -1;
  }

  Serial.println("Connection successful");
  return 0;
}

void write_header(WiFiClient* client) {
  client->print(HEADER);
}
