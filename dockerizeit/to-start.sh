docker-compose -p project build
docker-compose -p project up -d
docker-compose -p project scale jslave=3