FROM eclipse-temurin:21-jdk as build
VOLUME ~/.gradle /root/.gradle
WORKDIR /workspace/app
COPY ./ ./
RUN ./gradlew assemble
RUN rm /workspace/app/build/libs/*-plain.jar> /dev/null
RUN java -Djarmode=layertools -jar /workspace/app/build/libs/*.jar extract --destination /extracted

FROM eclipse-temurin:21-jre
VOLUME /tmp
ARG EXTRACTED=/extracted
WORKDIR /app
RUN mkdir -p /app/.embedmongo/ && \
    chgrp -R 0 /app/.embedmongo/ && \
    chmod -R g+rwX /app/.embedmongo/
COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./
ENTRYPOINT ["java", "-Duser.home=/app", "org.springframework.boot.loader.launch.JarLauncher"]

