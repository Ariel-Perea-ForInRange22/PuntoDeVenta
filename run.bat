@echo off
echo Compilando el proyecto...

:: Limpiar el directorio bin
rmdir /s /q bin
mkdir bin

:: Compilar los archivos en orden de dependencia
javac -d bin -cp "lib/*;src" ^
src/com/puntoventa/model/*.java ^
src/com/puntoventa/dao/*.java ^
src/com/puntoventa/controller/*.java ^
src/com/puntoventa/util/*.java ^
src/com/puntoventa/view/*.java ^
src/com/puntoventa/view/Boton/inventory/*.java

if %errorlevel% neq 0 (
    echo Error en la compilacion
    pause
    exit /b %errorlevel%
)
echo Ejecutando la aplicacion...
java -cp "bin;lib/*" com.puntoventa.view.MainWindow
pause