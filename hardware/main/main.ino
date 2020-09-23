#include <SoftwareSerial.h>
#include <SPI.h>
#include <MFRC522.h>
#include <LiquidCrystal.h>

#define SS_PIN 10
#define RST_PIN 9

MFRC522 mfrc522(SS_PIN, RST_PIN);
SoftwareSerial hc06(2,3);

LiquidCrystal lcd(A0, A1, 7, 6, 5, 4);

bool isLoggedIn = false;

void setup(){
  //Initialize Serial Monitor
  Serial.begin(9600);
  SPI.begin();
  mfrc522.PCD_Init();
  Serial.println("Scan card/pin");
  Serial.println();
  //Initialize Bluetooth Serial Port
  hc06.begin(9600);

  // set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  // Print a message to the LCD.
  lcd.print("hello, world!");
}

void loop(){
  //Write data from HC06 to Serial Monitor
  if (hc06.available()){
    Serial.write(hc06.read());
  }
  
  //Write from Serial Monitor to HC06
  if (Serial.available()){
    hc06.write(Serial.read());
  }

  if (!mfrc522.PICC_IsNewCardPresent()){
    return;
  }
  if (!mfrc522.PICC_ReadCardSerial()){
    return;
  }

  String uid = "";
  for (byte i = 0; i < mfrc522.uid.size; i++){
    uid += String(mfrc522.uid.uidByte[i], HEX);
  }
  Serial.print("Should send following: ");
  Serial.print(uid);
  Serial.println();
  // Sends string to bluetooth unit
  hc06.print(uid);


  // set the cursor to column 0, line 1
  // (note: line 1 is the second row, since counting begins with 0):
  lcd.setCursor(0, 1);
  // print the number of seconds since reset:
  lcd.print(millis() / 1000);

  
  delay(3000);
}
