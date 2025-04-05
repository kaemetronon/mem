FROM bellsoft/liberica-openjdk-alpine:17
COPY target/mem-1.0-RELEASE.jar mem.jar
ENTRYPOINT ["java", "-Xmx128m", "-Dspring.profiles.active=${SPRING_PROFILE}", "-jar", "mem.jar"]