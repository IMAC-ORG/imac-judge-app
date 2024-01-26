# IMAC-Judge-App

# For AeroJudge setup instruction please take a look here
## https://github.com/IMAC-ORG/imac-judge-app/tree/main/scripts


# For developer environment please look below

## Requirements
1. VSCode
2. Docker Desktop
3. Score =>v4.52 with services enabled and started

## RUN imac-judge-app
1. Open in a dev container
2. Open new terminal
3. Edit the /var/opt/judge/settings.json file with correct host_ip and port that Score is running on. The file is mounted from the .devcontainer/judge folder and can be modified there instead.
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
4. Running the app in the dev container
``` 
cd /workspace
mvn spring-boot:run
```
5. Connecting to the dev container can be done in your local browser http://locahost:8080

## Build imac-judge
1. To build the jar file to be deployed to the PI-SCORE unit.
```
cd judge/
./mvnw clean package
```
Binary {build}.jar located in judge/target folder needs to be copied to the device and extracted in /var/opt/judge/bin
Also {build}-figures.zip file needs to be copied to the device and extracted in /var/opt/judge

## RUN imac-judge-app as a docker container (very similar to running in device)
1. Build imac-judge using above instructions
2. Right-click Dockerfile in root folder and choose Build Image... and name tag imacjudgeapp:latest
3. In terminal run: docker run -p 8080:8080 imacjudgeapp:latest
4. Connecting to the image can be done in your local browser http://locahost:8080
