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

arguments = docopt(__doc__)

if arguments['--debug']:
  logging.basicConfig(level=logging.DEBUG)

logging.debug("Arguments: {}".format(arguments))

with open(arguments['--file'], 'r') as f:
  doc = yaml.load(f)

logging.debug("Provide docker compose file: {}".format(doc))

logging.info("Read jmaster container config...")
# TODO: add exceptions handling in case we need sudo to run docker
jmaster_id=check_output(["docker", "ps", "-a", "-q", "-f", "name=jmaster"]).strip()
logging.debug("jmaster container id: {}".format(jmaster_id))
# TODO: check that we got one id - not two and not none
jmaster_binds=check_output(["docker", "inspect", '--format="{{ .HostConfig.Binds }}"', jmaster_id]).strip()
logging.debug("jmaster binds: {}".format(jmaster_binds))
logging.info("Read jslave container config...")
jslave_id=check_output(["docker", "ps", "-a", "-q", "-f", "name=jslave"]).strip()
logging.debug("jslave container id: {}".format(jslave_id))
# TODO: check that we got one id - not two and not none
jslave_binds=check_output(["docker", "inspect", '--format="{{ .HostConfig.Binds }}"', jslave_id]).strip()
logging.debug("jslave binds: {}".format(jslave_binds))

logging.info("Update definition for jmaster. Relace build with image...")
logging.debug("Before: {}".format(doc["services"]["jmaster"]))
del doc["services"]["jmaster"]["build"]
doc["services"]["jmaster"]["image"] = "{}:{}".format(arguments['--jmaster-image'], arguments['--jmaster-version'])
doc["services"]["jmaster"]["volumes"] = jmaster_binds.strip('[|]').replace(":rw","").split()
logging.debug("After: {}".format(doc["services"]["jmaster"]))

logging.info("Update definition for jslave. Relace build with image...")
logging.debug("Before: {}".format(doc["services"]["jslave"]))
del doc["services"]["jslave"]["build"]
doc["services"]["jslave"]["image"] = "{}:{}".format(arguments['--jslave-image'], arguments['--jslave-version'])
doc["services"]["jslave"]["volumes"] =jslave_binds.strip('[|]').replace(":rw","").split()
logging.debug("After: {}".format(doc["services"]["jslave"]))

with open('docker-compose.yml', 'w') as outfile:
    outfile.write( yaml.dump(doc, default_flow_style=False) )
