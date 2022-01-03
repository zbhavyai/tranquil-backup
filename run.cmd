@REM CLEAR THE SCREEN
CLS

@REM COMPILE THE FILES
javac -cp ".;lib/*" -sourcepath src -d bin src/*.java

@REM RUN
java -cp ".;lib/*;bin" App

@REM DON'T SUDDENLY QUIT
PAUSE
