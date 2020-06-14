set artifacts=..\..\..\..\..\..\..\java-advanced-2020\artifacts
mkdir _build
javac -d _build -cp %artifacts%\* *.java
cd _build
jar xf ..\%artifacts%\info.kgeorgiy.java.advanced.implementor.jar
jar cfm .\..\_implementor.jar ..\Manifest.mf ru\ifmo\rain\naumov\implementor\*.class info\kgeorgiy\java\advanced\implementor\*.class
cd ..
rmdir _build /s /q
