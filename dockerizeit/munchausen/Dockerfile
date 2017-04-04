FROM alpine:3.3

# Add support for proxies.
# Values should be passed as build args
# http://docs.docker.com/engine/reference/builder/#arg
ENV http_proxy ${http_proxy:-}
ENV https_proxy ${https_proxy:-}
ENV no_proxy ${no_proxy:-}

# install docker
RUN apk update && apk add curl && curl -o docker-1.12.1.tgz https://get.docker.com/builds/Linux/x86_64/docker-1.12.1.tgz && tar zxvf docker-1.12.1.tgz && mv docker/* /usr/bin/ && rm -rf docker-*

# install docker compose
RUN apk update && apk add py-pip && pip install docker-compose==1.8.0

# copy bootstrap script
COPY bootstrap.sh /bootstrap.sh
RUN chmod 755 /bootstrap.sh

COPY docker-compose.yml /docker-compose.yml
WORKDIR /

ENTRYPOINT ["/bootstrap.sh"]