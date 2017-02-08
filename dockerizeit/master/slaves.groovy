import java.lang.System
import hudson.model.*
import jenkins.model.*
import hudson.slaves.*
import hudson.plugins.sshslaves.*
import hudson.model.Node.*

// load helpers and read properties
def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/slaves.properties").toURI().toURL())

println properties

properties.slaves.each {
  println "--> Create ${it.value.type} slave ${it.key}"
  println it
  ComputerLauncher launcher = null
  switch (it.value.type) {
    case "jnlp":
        // Launch strategy, JNLP is the Java Web Start setting services use
        launcher = new JNLPLauncher()
      break
    case "ssh":
        launcher = new SSHLauncher(it.value.host,                 // The host to connect to
                                   it.value.get('port', 22),      // The port to connect on
                                   it.value.credentialsId,        // The credentials id to connect as
                                   it.value.get('jvmOptions',""),  // Options passed to the java vm
                                   it.value.get('javaPath', ""),            // Path to the host jdk installation
                                   it.value.get('prefixStartSlaveCmd', ""),  // This will prefix the start slave command
                                   it.value.get('suffixStartSlaveCmd', ""), // This will suffix the start slave command
                                   it.value.get('launchTimeoutSeconds', null), // Launch timeout in seconds
                                   it.value.get('maxNumRetries', null),       // The number of times to retry connection if the SSH connection is refused
                                   it.value.get('retryWaitTime', null))        // The number of seconds to wait between retries
      break
    default:
      throw new UnsupportedOperationException("${it.value.type} slave type is not supported!")
      break
  }
  DumbSlave dumb = new DumbSlave(it.value.name,                  // Agent name
                                 it.value.description,           // Agent description
                                 it.value.remoteFS,              // Workspace on the agent's computer
                                 it.value.executors,             // Number of executors
                                 it.value.mode,                  // "Usage" field, EXCLUSIVE is "only tied to node", NORMAL is "any"
                                 it.value.labels,                // Labels
                                 launcher,                       // Launch strategy, JNLP is the Java Web Start setting services use
                                 it.value.retention)             // Is the "Availability" field and INSTANCE means "Always"

  // Add env variables
  def entryList = []
  for (var in it.value.env) {
    entryList.add(new EnvironmentVariablesNodeProperty.Entry(var.key, var.value))
  }
  def evnp = new EnvironmentVariablesNodeProperty(entryList)
  dumb.nodeProperties.add(evnp)

  Jenkins.instance.addNode(dumb)
}