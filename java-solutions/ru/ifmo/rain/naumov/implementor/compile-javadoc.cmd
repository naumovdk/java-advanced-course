set artifacts=..\..\..\..\..\..\..\java-advanced-2020\artifacts
set orig=..\..\..\..\..\..\..\..\java-advanced-2020\modules\info.kgeorgiy.java.advanced.implementor
mkdir _build
SET link=https://docs.oracle.com/en/java/javase/13/docs/api
SET data=.\info\kgeorgiy\java\advanced\implementor\
set package=ru.ifmo.rain.naumov.implementor
javac -d _build -cp %artifacts%\* *.java
cd _build
javadoc^
 -d ../_javadoc^
 -link %link%^
 -cp ..\;%orig%; ^
 -sourcepath ..\..\..\..\..\..\ ^
 -private -version ^
 %package%^
 %orig%\info\kgeorgiy\java\advanced\implementor\Impler.java^
 %orig%\info\kgeorgiy\java\advanced\implementor\ImplerException.java^
 %orig%\info\kgeorgiy\java\advanced\implementor\JarImpler.java
cd ..