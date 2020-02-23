@echo off

SET PWD=%~dp0/

docker run -it --init --rm ^
    --volume %PWD%/:/var/build/ ^
	--volume %USERPROFILE%/.m2/:/$HOME/.m2/ ^
    --workdir /var/build ^
    maven:3.6.3-jdk-13 ^
    mvn --settings /$HOME/.m2/settings.xml -Dmaven.repo.local=/$HOME/.m2/repository %*
