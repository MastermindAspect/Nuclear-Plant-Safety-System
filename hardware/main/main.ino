#include <SoftwareSerial.h>
#include <SPI.h>
#include <MFRC522.h>
#include <LiquidCrystal.h>

#define SS_PIN 10
#define RST_PIN 9
#define MAX_TIME_SECONDS 10

MFRC522 mfrc522(SS_PIN, RST_PIN);
SoftwareSerial hc06(2, 3);

LiquidCrystal lcd(A0, A1, 7, 6, 5, 4);

bool isLoggedIn = false;
unsigned long timer = 0;


void setup() {
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
  lcd.print("Setup done");
}

void loop() {

  // set the cursor to column 0, line 1
  // (note: line 1 is the second row, since counting begins with 0):
  // lcd.setCursor(0, 1);
  // print the number of seconds since reset:
  // lcd.print(millis() / 1000);

  // display timer of when someone is checked in
  if (timer != 0) {
    lcd.setCursor(0, 1);
    lcd.print( (millis() - timer) / 1000);
  }

  if ( (millis() - timer) / 1000 >= MAX_TIME_SECONDS && isLoggedIn) {
    lcd.print(" GET OUT!");
  }


  //Write data from HC06 to Serial Monitor
  if (hc06.available()) {
    Serial.write(hc06.read());
  }

  //Write from Serial Monitor to HC06
  if (Serial.available()) {
    hc06.write(Serial.read());
  }

  if (!mfrc522.PICC_IsNewCardPresent()) {
    return;
  }
  if (!mfrc522.PICC_ReadCardSerial()) {
    return;
  }

  String uid = "";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    uid += String(mfrc522.uid.uidByte[i], HEX);
  }
  if (uid == "d9a7dd56") {
    if (isLoggedIn) {
      // Reseting timer so it wont be printed
      timer = 0;
      isLoggedIn = false;
      lcd.clear();
      lcd.print("Checked out");
    } else {
      // Checking in ...

      // Start counting from check in
      timer = millis();
      isLoggedIn = true;
      lcd.clear();
      lcd.print("Checked in");
    }
  }
  Serial.println("millis: ");
  Serial.print(millis());
  Serial.println();
  Serial.println("timer: ");
  Serial.print(timer);
  Serial.println();
  Serial.print(uid);
  Serial.println();
  hc06.print(uid);
  uid = "";

  delay(1000);
}
