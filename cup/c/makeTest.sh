rm *.java
rm *.class
jflex c.jflex
java -jar ../../java-cup-11b.jar -locations -interface -parser Parser -xmlactions c.cup
javac -cp ../../java-cup-11b-runtime.jar:. *.java
java -cp ../../java-cup-11b-runtime.jar:. Parser input.c simple.xml
java -cp ../../java-cup-11b-runtime.jar:. Parser complicated.c complicated.xml
