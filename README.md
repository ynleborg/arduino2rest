Arduino to REST 
=============================

A simple application that reads data from serial port and invokes web service.

##Running (assuming x64 architecture)
First grab RXTX native library to communicate with serial port: http://fizzed.com/oss/rxtx-for-java
 
Add RXTXcomm.jar to the CLASSPATH (do not use jar from maven repository, it is not compatible with x64)

Run program with following system properties:
 * -Djava.library.path="d:\mfz-rxtx-2.2-20081207-win-x64" (point to folder contaning rxtxSerial.dll)
 * -Dgnu.io.rxtx.SerialPorts=COM6 (check in device manager, which COM is assigned to Arduino)
 
The program accepts one argument with is an endpoint to sent data to. If not passed, the program is running in dry run mode.  