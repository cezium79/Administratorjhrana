@echo off
cd /d "C:\Users\Николай\IdeaProjects\Administrator Jhrana"
if exist target rmdir /s /q target
"C:\Program Files\apache-maven-3.9.16\bin\mvn.cmd" package -DskipTests -Dmaven.compiler.fork=true "-Dmaven.compiler.executable=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot\bin\javac.exe"
if errorlevel 1 (
    echo BUILD FAILED
    pause
) else (
    echo BUILD SUCCESS
    echo Starting application...
    java -jar target\jhrana-0.0.1-SNAPSHOT.jar
)
