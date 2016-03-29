Jenkins in Docker

To run up the master+slaves conatiners by docker-compose, please update the yml file. 
To have access to the GIT repo you might use SSH connection, for that you need to generate SSH keys, update the repo with users you want to have the access by SSH and mount the directory with all keys for the user to containers:

volumes:
   - /path/to/.ssh_jenkins:/var/jenkins_home/.ssh 

Also mount the directory to keep your jobs history in the case of Jenkins updating:
   - /path/to/backup/jenkins-home-backup/jobs:/var/jenkins_home/jobs

For the slaves, you have to mount SSH directory either. Here you might mount any sources, licences you need to provide for the jobs execution.

Add the links to some external containers your jobs or configuration will depend.  