JFLAGS = -g
JCC = javac

default: server.class client.class packet.class emulator.class

client.class: client.java
	$(JCC) $(JFLAGS) client.java

server.class: server.java
	$(JCC) $(JFLAGS) server.java

packet.class: packet.java
	$(JCC) $(JFLAGS) packet.java

emulator.class: emulator.java
	$(JCC) $(JFLAGS) emulator.java

clean: 
	$(RM) *.class