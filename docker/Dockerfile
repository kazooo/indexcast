FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=docker/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /indexcast/lib
COPY ${DEPENDENCY}/META-INF /indexcast/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /indexcast
ENTRYPOINT ["java","-cp","indexcast:indexcast/lib/*","cz.mzk.IndexcastApplication"]
