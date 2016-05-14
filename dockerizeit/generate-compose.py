#!/usr/bin/env python

"""docker-compose.yml generator.
   It takes existing docker-compose.yml,
   replaces "build:" instructions with the "image:" instructions,
   preserves current binds - will read them from the running containers

Usage:
  generate-compose.py [ --debug ] --file=file --jmaster-image=jm_image --jmaster-version=jm_version --jslave-image=js_image --jslave-version=js_version
  generate-compose.py (-h | --help)

Options:
  --file=file                  Path to the base docker-compose.yml file
  --jmaster-image=jm_image     Jenkins master image name
  --jmaster-version=jm_version Jenkins master image version
  --jslave-name=js_name        Jenkins slave image name
  --jslave-version=js_version  Jenkins slave image version
  -h --help                    Show this screen.
  -d --debug                   Print debug info


"""
from docopt import docopt
from subprocess import check_output
import logging
import yaml
import re

def get_binds(service_name):
  logging.info("Read {} container config...".format(service_name))
  command = ["docker", "ps", "-a", "-q", "--no-trunc", "-f", "name=dockerizeit_{}_".format(service_name)]
  logging.debug("Get {} container id: {}".format(service_name, " ".join(command)))
  container_id = check_output(command).strip()
  logging.debug("{} container id: {}".format(service_name, container_id))
  if bool(re.match("[A-Za-z0-9]{65}", container_id)):
    raise ValueError("Unexpected result. Expected docker container id, got {}. Command: {}".format(container_id, " ".join(command)))
  binds = check_output(["docker", "inspect", '--format="{{ .HostConfig.Binds }}"', container_id]).strip()
  logging.debug("{} binds: {}".format(service_name, binds))
  return binds

def main():
  arguments = docopt(__doc__)

  if arguments['--debug']:
    logging.basicConfig(level=logging.DEBUG)

  logging.debug("Arguments: {}".format(arguments))

  with open(arguments['--file'], 'r') as f:
    doc = yaml.load(f)

  logging.debug("Provided docker compose file: {}".format(doc))

  # Update jmaster
  logging.info("Update definition for jmaster. Relace build with image...")
  logging.debug("Before: {}".format(doc["services"]["jmaster"]))
  del doc["services"]["jmaster"]["build"]
  doc["services"]["jmaster"]["image"] = "{}:{}".format(arguments['--jmaster-image'], arguments['--jmaster-version'])

  # We are resolving binds to make sure that we point out correct location of the home directory
  # Use case: starting containers using docker-machine - on the start path resolved to the home directory on the host
  # When munchausen restarts services he will be inside Linux virtual machine and home directory will be resolved differently
  logging.info("Update definition for jmaster. Resolve binds...")
  jmaster_binds = get_binds("jmaster")
  doc["services"]["jmaster"]["volumes"] = jmaster_binds.strip('[|]').replace(":rw","").split()
  # We have to check that we don't have not mounted volumes like 1ab3ba428445786de381d741cd2d3c4dff2e956342f02712ef205fa63ba47779:/var/jenkins_home
  # This volume comes from Jenkins Dockerfile - it is declared there but we do not mount it explicitly
  # If bring it to the compose file like this 1ab3ba428445786de381d741cd2d3c4dff2e956342f02712ef205fa63ba47779:/var/jenkins_home then
  # docker-compose won't be able to create new container to replace old one since we have direct reference to the volume mount point
  pattern=re.compile("^[a-zA-Z0-9]+:/var/jenkins_home$")
  for item in doc["services"]["jmaster"]["volumes"]:
    if re.findall(pattern, item):
      logging.debug("Found not mounted volume {}. Drop it from the volumes list...".format(re.findall(pattern, item)[0]))
      doc["services"]["jmaster"]["volumes"].remove(re.findall(pattern, item)[0])
  logging.debug("After: {}".format(doc["services"]["jmaster"]))

  # Update jslave
  logging.info("Update definition for jslave. Relace build with image...")
  logging.debug("Before: {}".format(doc["services"]["jslave"]))
  del doc["services"]["jslave"]["build"]
  doc["services"]["jslave"]["image"] = "{}:{}".format(arguments['--jslave-image'], arguments['--jslave-version'])
  jslave_binds = get_binds("jslave")
  doc["services"]["jslave"]["volumes"] =jslave_binds.strip('[|]').replace(":rw","").split()

  # Registry might be removed and replaced by something else so we have to check that it is there
  if "registry" in doc["services"]:
    logging.debug("Before: {}".format(doc["services"]["registry"]))
    registry_binds = get_binds("registry")
    doc["services"]["registry"]["volumes"] = registry_binds.strip('[|]').replace(":rw","").split()
    logging.debug("After: {}".format(doc["services"]["registry"]))

  with open('docker-compose.yml', 'w') as outfile:
    outfile.write( yaml.dump(doc, default_flow_style=False) )

if __name__ == "__main__":
    main()
