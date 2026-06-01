FROM alpine:3.21
RUN apk add --no-cache gettext bash
COPY render.sh /usr/local/bin/render
WORKDIR /workspace
ENTRYPOINT ["render"]
