FROM bellsoft/liberica-openjdk-alpine:17
COPY target/mem-1.0-RELEASE.jar mem.jar
ENTRYPOINT ["java", "-Xmx128m", "-jar", "mem.jar"]