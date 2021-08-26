FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /work/
COPY /build/*-runner /work/application
RUN chmod 775 /work
ENTRYPOINT ["./application"]