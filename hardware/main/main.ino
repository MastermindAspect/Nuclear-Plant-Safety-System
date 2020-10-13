#include <SoftwareSerial.h>
#include <SPI.h>
#include <MFRC522.h>
#include <LiquidCrystal.h>

#define SS_PIN 10
#define RST_PIN 9
#define RX_PIN 2
#define TX_PIN 3
#define POT_PIN A5
#define BUTTON_PIN 8
#define BUTTON_ROOM_PIN A2

#define BAUD_RATE 9600

enum s_room{CONTROL, REACTOR, BREAK};

MFRC522 mfrc522(SS_PIN, RST_PIN);

SoftwareSerial hc06(RX_PIN, TX_PIN);

LiquidCrystal lcd(A0, A1, 7, 6, 5, 4);


enum s_room current_room = BREAK;
int prevVal = 0;
int hazmatButton = 0;
int roomButton = 0;
String msg="";
String intToString="";

/*
* Reads the potentiometer value and sends it as a
* type String to the app, only sends after it has changed more
* than 5 units, to prevent "spamming" the app.
*/
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
        delay(100);
    }
}

/*
* Checks the button that is connected to the hazmat suit.
* if clicked, sends a message to the app to toggle the suit
*/
void hazmatSuitSimulator(){
    hazmatButton = digitalRead(BUTTON_PIN);
    if (hazmatButton == HIGH) {
        // Button pressed
    
        hc06.print("s:0");
        Serial.print("hazmat");
        Serial.println();   
        delay(100);
    }
}

/*
* Checks the button to change rooms. When clicked
* Sends a message to the app that indicates to change room
*/
void roomSimulator(){
    roomButton = digitalRead(BUTTON_ROOM_PIN);
    if(roomButton == HIGH){
        // Button pressed
        hc06.print("y:0");
        Serial.print("room");
        Serial.println();
        
        delay(100);
    }
}

void setup() {
    //Initialize Serial Monitor
    Serial.begin(BAUD_RATE);
    SPI.begin();
    mfrc522.PCD_Init();

    //Initialize Bluetooth Serial Port
    hc06.begin(BAUD_RATE);

    // set up the LCD's number of columns and rows:
    lcd.begin(16, 2);
    // Print a message to the LCD.
    lcd.print("Setup done");
}

void loop() {

    // Send radiation level
    radiationSimulator();
    hazmatSuitSimulator();
    roomSimulator();
    
    // The bluetooth module waits for message and puts it in rxMsg
    if (hc06.available()) {
        String rxMsg;
        while(hc06.available()){
            delay(1);
            if(hc06.available() > 0){
                char c = hc06.read();
                rxMsg += c;
            }
        }
        Serial.print(rxMsg);
        Serial.println();
        char command = rxMsg[0];
        
        String timer = rxMsg;
        // special case
        if (timer[timer.length() - 1] == 'n'){
            command = 'n';
    }
    
    switch(command) {  
        case 'r':
            // System wide warning
            lcd.clear();
            lcd.print("Get out NOW!");
            break;
        case 'i': 
            // Check in
            lcd.clear();
            lcd.print("Checked in");
            delay(300);
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
            delay(1500);
            
            if (current_room == BREAK) {
            lcd.clear();
            lcd.setCursor(0,0);
            lcd.print("Break room");
            current_room = BREAK;
            }
            else if (current_room == CONTROL) {
            lcd.clear();
            lcd.setCursor(0,0);
            lcd.print("Control room");
            current_room = CONTROL;
            }
            else {
            lcd.clear();
            lcd.setCursor(0,0);
            lcd.print("Reactor room");
            current_room = REACTOR;
            }
            break;
        case 't':
            lcd.setCursor(0,1);
            lcd.print(timer);
            lcd.print("              ");
            break;
        case '1':
            lcd.clear();
            lcd.setCursor(0,0);
            lcd.print("Break room");
            current_room = BREAK;
            break;
        case '2':
            lcd.clear();
            lcd.setCursor(0,0);
            lcd.print("Control room");
            current_room = CONTROL;
            break;
        case '3':
            lcd.clear();
            lcd.setCursor(0,0);
            lcd.print("Reactor room");
            current_room = REACTOR;
            break;
        default:
            break;
        }
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
    
    Serial.print(uid);
    Serial.println();
    hc06.print(uid);
    uid = "";
    delay(1000);
}
