#include <Arduino.h>
#include <SoftwareSerial.h>
#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN 10
#define RST_PIN 9

MFRC522 mfrc522(SS_PIN, RST_PIN);
SoftwareSerial hc06(1, 2);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  SPI.begin();
  mfrc522.PCD_Init();
  Serial.println("Approximate your card to the reader...");
  Serial.println();

  // Init bluetooth serial port
  hc06.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  // Look for new cards
  if ( !mfrc522.PICC_IsNewCardPresent()) {
    return ;
  }
  // Select one of the cards
  if (!mfrc522.PICC_ReadCardSerial()) {
    return;
  }

  if (hc06.available()){
      Serial.write(hc06.read());
  }

  // Write from Serial monitor to hc06
  if (Serial.available()){
      hc06.write(Serial.read());
  }

  // Show UID on serial monitor
  Serial.print("UID tag :");
  String content = "";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
    Serial.print(mfrc522.uid.uidByte[i], HEX);
    content.concat(String(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " "));
    content.concat(String(mfrc522.uid.uidByte[i], HEX));
  }
  Serial.println();
  Serial.print("Message : ");
  content.toUpperCase();
  if (content.substring(1) == "D9 A7 DD 56" || content.substring(1) == "99 56 AE 47"){
    Serial.println("Authorized access =)");
    Serial.println();
    delay(3000);
  }
  else {
    Serial.println(" Access denied");
  }
  
}