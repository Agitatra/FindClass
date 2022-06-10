javac -cp src/main/java src/main/java/de/mk_p/findclass/FindClass.java -d target
jar -c -f FindClass.jar -m META-INF/MANIFEST.MF -C target de
javadoc -cp src/main/java src/main/java/de/mk_p/findclass/FindClass.java -d javadoc
