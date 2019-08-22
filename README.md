# Save Metropolis
A 2006 MIDlet game - Could an helicopter save the world from the alien invasion ?

![screenshot](https://github.com/fstarred/save-metropolis/blob/master/docs/picture.jpg?raw=true) 

## Requirements

* Java
* Maven 2.x (tested with 2.0.2)
* Sun Java Wireless Toolkit

## Usage

### How to compile

Set _wtk.home_ property on _pom.xml_ or run maven with 

```
-Denv.wtkhome=<wtk directory>
```

example:
```
mvn clean package -Denv.wtkhome=/D/Software/WTK2.5.2_01
```

### How to run

```
mvn j2me:run -Denv.wtkhome=<wtk directory>
```

### How to debug

Don't know if is possible to configure j2me-maven-plugin from pom, by the way you can manually launch the emulator from *WTK* directory with debug listening on a certain port

```
<WTK_HOME>/bin/emulator.exe -Xdevice:DefaultColorPhone -Xheapsize:1M -Xdescriptor: -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8787 -Xdescriptor:<jad_file>
```

### How to run on Android device

1. Copy jar and jad files under target directory to your device
2. Install [J2ME Loader](https://apkpure.com/j2me-loader/ru.playsoftware.j2meloader) on your device
3. Open J2ME Loader, locate the jad file and run it
