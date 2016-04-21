FROM debian:jessie

# Add proxy env
ENV HTTP_PROXY=$HTTP_PROXY
ENV HTTPS_PROXY=$HTTPS_PROXY
ENV NO_PROXY=$NO_PROXY
ENV http_proxy=$http_proxy
ENV https_proxy=$https_proxy
ENV no_proxy=$no_proxy

# install docker client and pip
RUN apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D && echo "deb http://apt.dockerproject.org/repo debian-jessie main" > /etc/apt/sources.list.d/docker.list && apt-get update && apt-get install -y docker-engine=1.11.0-0~jessie python-pip

# install docker compose
RUN pip install docker-compose==1.7

# copy bootstrap script
COPY bootstrap.sh /bootstrap.sh
RUN chmod 755 /bootstrap.sh

COPY docker-compose.yml /docker-compose.yml
WORKDIR /

ENTRYPOINT ["/bootstrap.sh"]