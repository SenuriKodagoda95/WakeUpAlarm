const int pirPin = 7;
int RelayPin = 13;
int data;

void setup() {
// put your setup code here, to run once:
//pins
pinMode(pirPin, INPUT);
pinMode(RelayPin, OUTPUT);
Serial.begin(9600);

}



void sendData(){
 if(digitalRead(pirPin) == HIGH){
  Serial.write(4);
  Serial.println("Motion Detected");
 }

 else if(digitalRead(pirPin) == LOW){
  Serial.write(5);
  Serial.println("No Motion");
  }
}



void loop() {
// put your main code here, to run repeatedly:
  while (Serial.available() > 0) { //wait here until the data is available
  data = Serial.read();

  sendData();
  

  if (data == '2') {
    digitalWrite(RelayPin, LOW);
    Serial.println("Bulb On");
  }

  else if (data == '3'){
    digitalWrite(RelayPin, HIGH);
    Serial.println("Bulb Off");
  }
}

}


