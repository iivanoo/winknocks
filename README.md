winknocks
=========

a portknocking implementation for Windows platforms

What is winKnocks
-----------------

winKnocks is an encrypted port knocking tool. It is based on jpcap capturing library and Windows firewall. The project can be divided into two tools: Knocker and Listener. They are both written in Java and have a Swing GUI. Knocker and Listener share XML files containing the description of user-defined knock-sequences.
Users specify:

* number of packets of each knock sequence,
* payload and fields of the header of each packet(UDP, TCP, ICMP)

When sending a knock sequence, the user can define an urgent-script to execute server-side. The Listener can allow or not the execution of such scripts. When the server receives a knock sequence it can:

* open a port
* close a port
* execute a bash script

Each action have three additional fiels:

* wait(the action is executed after X seconds)
* timeout(the port is closed after Y seconds)
* exclusive (the port is opened only to the IP that sent the sequence)

The payload of each packet is encrypted using the DES algorithm and a secret password shared between Knocker and Listener. The server has logging capability. The Knocker can send smoke packets (the user defines the number of smoke packets); the Listener distinguish smoke packets from knock-sequences packets in two ways: (i) it analyses the packets defined into the XML files of the knock sequences and automatically defines a filter for the packet captor, (ii) the user defines such filter. Replay attacks are neutralized because the payload of each packet contains the timestamp and a random number(they are cripted); the Listener maintains the used numbers and checks that each incoming packet does not contain an already used number. In this case the received packet and the IP address of the aggressor are logged. 

The most important features of this tool are:

* it is easy-to-use because it runs using Windows official firewall
* the user can send knock sequences also to already opened ports: the tool captures packets at data-link level in non-blocking mode
* flexibility: there are not predefined knock-sequences, the user may define sequences that contain 1 packet to execute non-dangerous actions, 10 packets, 100 packets...to execute very important actions
* the tool does not need a predefined set of unused ports
* the knock sequences are defined in XML and the advantages are well-known!

--------

WinKnocks was my final project of an exam I took at the University of L'Aquila. I am not working on Winknocks since 2006. If you want to contact me, you can visit my [homepage](http://www.di.univaq.it/malavolta), or you can send me an email, I will be very happy to help.