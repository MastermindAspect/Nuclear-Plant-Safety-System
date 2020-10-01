#include <SoftwareSerial.h>
#include <SPI.h>
#include <MFRC522.h>
#include <LiquidCrystal.h>

#define SS_PIN 10
#define RST_PIN 9
#define MAX_TIME_SECONDS 10
#define RX_PIN 2
#define TX_PIN 3
#define POT_PIN A5
#define BUTTON_PIN 8

#define BAUD_RATE 9600

MFRC522 mfrc522(SS_PIN, RST_PIN);
SoftwareSerial hc06(RX_PIN, TX_PIN);

LiquidCrystal lcd(A0, A1, 7, 6, 5, 4);

bool isLoggedIn = false;
unsigned long timer = 0;
int prevVal = 0;
int buttonVal = 0;
String msg="";
String intToString="";
bool hazmatSuitEquipped = false;

void radiationSimulator() {
  int readVal = analogRead(POT_PIN);
  readVal /= 10;
 
  if ( abs(readVal - prevVal) > 5 ) {
    // send msg
    prevVal = readVal;
    msg = "r:";
    intToString = String(prevVal);
    String sendRadVal =  msg + intToString;
    hc06.print(sendRadVal);
    Serial.print(sendRadVal);
    Serial.println();
  }
  delay(100);
}

void hazmatSuitSimulator(){
  buttonVal = digitalRead(BUTTON_PIN);
  if (buttonVal == HIGH) {
    // Button pressed
    if (hazmatSuitEquipped) {
      hazmatSuitEquipped = false;
      hc06.print("s:false");
      Serial.print("s:false");
      Serial.println();
    } 
    else {
      hazmatSuitEquipped = true;
      hc06.print("s:true");
      Serial.print("s:true");
      Serial.println();
    }
  }
  delay(100);
}

void displaySafetyStatus(){
  
}

void startTimer(){
  
}

void stopTimer(){
  
}

void setup() {
  //Initialize Serial Monitor
  Serial.begin(BAUD_RATE);
  SPI.begin();
  mfrc522.PCD_Init();
  Serial.println("Scan card/pin");
  Serial.println();
  //Initialize Bluetooth Serial Port
  hc06.begin(BAUD_RATE);

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

  // Send radiation level
  radiationSimulator();
  hazmatSuitSimulator();
  
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
    String rxMsg;
    while(hc06.available()){
      delay(1);
      if(hc06.available() > 0){
        char c = hc06.read();
        rxMsg += c;
      }
    }
  
  char command = rxMsg[0];
  
  
  String timer = rxMsg.substring(2);
  Serial.print(timer);
  Serial.println();
  // special case
  if (timer[timer.length() - 1] == 'n'){
    command = 'n';
  }
  
    switch(command) {  
      case 'r': 
        // System wide warning
        lcd.clear();
        lcd.print("Get out!");
        lcd.setCursor(0,1);
        lcd.print("It's hot!");
        break;
      case 'i': 
        // Check in
        lcd.clear();
        lcd.print("Checked in");
        break;
      case 'o': 
        // Check out
        lcd.clear();
        lcd.print("Checked out");
        break;
      case 'n': 
        // Interval warning
        lcd.clear();
        lcd.print("Interval warning");
        break;
      case 't':
        lcd.clear();
        lcd.print(timer);
        break;
      default:
        lcd.clear();
        lcd.print("inside default");
        break;
    }
  }
  
  if (hc06.available()){
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

  String uid = "u:";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    uid += String(mfrc522.uid.uidByte[i], HEX);
  }
  
  /*if (uid == "d9a7dd56") {
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
  }*/
  Serial.print(uid);
  Serial.println();
  hc06.print(uid);
  uid = "";

  delay(1000);
}
