# Requires Java 8 (preferred) or later.
# Check if you have Java compiler installed:
# javac -version
# To install Java compiler use:
# sudo apt install openjdk-8-jdk

PKG = ncl/tsetlin/tools

.PHONY: default all clauses genlogger pkbits spectrum clean

all: clauses genlogger pkbits spectrum

clauses: bin/$(PKG)/clauses/*.class

genlogger: bin/$(PKG)/genlogger/*.class

pkbits: bin/$(PKG)/pkbits/*.class

spectrum: bin/$(PKG)/spectrum/*.class

bin/$(PKG)/clauses/*.class: bin src/$(PKG)/clauses/*.java
	javac -d bin src/$(PKG)/clauses/*.java

bin/$(PKG)/genlogger/*.class: bin src/$(PKG)/genlogger/*.java src/$(PKG)/genlogger/*.str
	javac -d bin src/$(PKG)/genlogger/*.java
	cp src/$(PKG)/genlogger/*.str bin/$(PKG)/genlogger

bin/$(PKG)/pkbits/*.class: bin src/$(PKG)/pkbits/*.java
	javac -d bin src/$(PKG)/pkbits/*.java

bin/$(PKG)/spectrum/*.class: bin src/$(PKG)/spectrum/*.java
	javac -d bin src/$(PKG)/spectrum/*.java

bin:
	mkdir bin

clean:
	-rm -rf bin
