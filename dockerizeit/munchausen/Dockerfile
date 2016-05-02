FROM alpine:3.3

# Add proxy env
ENV HTTP_PROXY=$HTTP_PROXY
ENV HTTPS_PROXY=$HTTPS_PROXY
ENV NO_PROXY=$NO_PROXY
ENV http_proxy=$http_proxy
ENV https_proxy=$https_proxy
ENV no_proxy=$no_proxy

# install docker
RUN apk update && apk add curl && curl -o docker-1.11.0.tgz https://get.docker.com/builds/Linux/x86_64/docker-1.11.0.tgz && tar zxvf docker-1.11.0.tgz && mv docker/* /usr/bin/ && rm -rf docker-*

# install docker compose
RUN apk update && apk add py-pip && pip install docker-compose==1.7

# copy bootstrap script
COPY bootstrap.sh /bootstrap.sh
RUN chmod 755 /bootstrap.sh

COPY docker-compose.yml /docker-compose.yml
WORKDIR /

ENTRYPOINT ["/bootstrap.sh"]