#!/bin/bash

rm /Users/graham_kim/IdeaProjects/SemSim-Java-API/lib/SemSimAPI.jar
rm -r /Users/graham_kim/IdeaProjects/SemSim-Java-API/doc/
echo "Making jar"
ant -buildfile build.xml create_jar
echo "DONE with jar"
echo "Making JavaDoc"
ant -buildfile javadoc.xml javadoc
echo "DONE with JavaDoc...zipping"
cd /Users/graham_kim/IdeaProjects/SemSim-Java-API/doc
zip -r ~/SemSimAPIjavaDoc.zip ./*
rm -r /Users/graham_kim/IdeaProjects/SemSim-Java-API/doc/*
mv ~/SemSimAPIjavaDoc.zip /Users/graham_kim/IdeaProjects/SemSim-Java-API/doc/SemSimAPIjavaDoc.zip
echo "DONE!"
