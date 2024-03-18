START /B /WAIT cmd /c "gradlew clean build --exclude-task test"
START /B /WAIT cmd /c " docker build --build-arg DEPENDENCY=build/dependency -t 941kjw/flatrepository:v6.4 ."
START /B /WAIT cmd /c "docker push 941kjw/flatrepository:v6.4"