# IMAC-Judge-App

## Requirements
1. VSCode
2. Docker Desktop
3. Score =>v4.52 with services enabled and started

## RUN imac-judge-app
1. Open in a dev container
2. Open new terminal
3. Run the following commands
```
sudo mkdir /var/opt/judge
sudo chmod 777 -R /var/opt/judge
```
4. Create a settings.json file with correct host_ip and port that Score is running on.
A copy of the settings.json can be found under the dockerbuild folder.
```
sudo vi /var/opt/judge/settings.json

eg.
{
  "judge_id":1,
  "line_number":1,
  "score_host":"192.168.1.4",
  "score_http_port":8181,
  "language":"en"
}
```
5. Running the app in the dev container
``` 
cd judge/
mvn spring-boot:run
```
6. Connecting to the dev container can be done in your local browser http://locahost:8080

## Build imac-judge
1. To build the jar file to be deployed to the PI-SCORE unit.
```
cd judge/
mvn clean package
```
Binaries located in judge/target folder
