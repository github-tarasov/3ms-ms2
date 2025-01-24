# Use the official OpenJDK image
FROM openjdk:24-ea-21-bullseye

# Copy the built jar files into the container
ADD /target/dist/lib /app/lib
ADD /target/dist/app.jar /app/app.jar

# Start app
ENTRYPOINT exec java -XX:OnOutOfMemoryError="kill -9 %p" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heap_dump -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:${DEBUG_PORT} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar
