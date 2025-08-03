# About

This small program will send PMTK commands to the GPS module. Quite often GPS modules constantly transmit NMEA messages and it is tricky to send and get response for system commands. This program will filter out all NMEA-related messages and output only request/response commands.

# Build

```
mvn clean package
```

# Run

```
java -Dos.arch_full=armv6hf -jar ./target/pmtk.jar /dev/ttyS0 9600 PMTK605
```

# Usage

```
<serial device> <baud rate> <command>
```

# Example

```
java -Dos.arch_full=armv6hf -jar ./target/pmtk.jar /dev/ttyS0 9600 PMTK605
Sending: $PMTK605*31
$PMTK705,AXN_5.1.6_3333_18041700,0005,1616D,1.0*6F
```

Tested with [Adafruit Ultimate GPS HAT for Raspberry Pi](https://www.adafruit.com/product/2324)
