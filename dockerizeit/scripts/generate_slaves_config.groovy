// Run this script from your Jenkins script console to generate slaves.properties
// Known limitations (for now no one asked for those so create an issue if you need any of those)
// - No support for environment variables and tools location
// - No support for "Take this slave on-line according schedule" and "Take this slave online when in demand and off when idle" availability types

import java.util.*
import jenkins.model.*
import hudson.model.*
import hudson.slaves.*
import groovy.util.ConfigObject

def result = [:]

for (aSlave in hudson.model.Hudson.instance.slaves) {
  Boolean isSupported = true
  def launcher = aSlave.getLauncher()
  def slave = [:]
  switch (launcher.getClass().getName()) {
    case 'hudson.slaves.JNLPLauncher':
      slave['type'] = 'jnlp'
      break
    case 'hudson.plugins.sshslaves.SSHLauncher':
      slave['type'] = 'ssh'
      // Fill in SSH specific details
      slave['host'] = launcher.getHost()
      slave['port'] = launcher.getPort()
      slave['credentialsId'] = launcher.getCredentialsId()
      slave['jvmOptions'] = launcher.getJvmOptions()
      slave['javaPath'] = launcher.getJavaPath()
      slave['prefixStartSlaveCmd'] = launcher.getPrefixStartSlaveCmd()
      slave['suffixStartSlaveCmd'] = launcher.getSuffixStartSlaveCmd()
      slave['launchTimeoutSeconds'] = launcher.getLaunchTimeoutSeconds()
      slave['maxNumRetries'] = launcher.getMaxNumRetries()
      slave['retryWaitTime'] = launcher.getRetryWaitTime()
      slave['env'] = [:]
      props = aSlave.nodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)
      for (prop in props) {
        prop.getEnvVars().each{ k, v -> slave['env'][k] = v }
      }
      break
    default:
      slave['type'] = launcher.getClass().getName()
      isSupported = false
      break
  }
  println ""
  if (isSupported == false) {
    println "Slave type ${slave['type']} not supported. No configuration will be generated for slave ${aSlave.getNodeName()}"
    continue
  }
  println "Found slave ${aSlave.getNodeName()} with type ${slave['type']}. Generating configuration"
  slave['name'] = aSlave.getNodeName()
  slave['description'] = aSlave.getNodeDescription()
  // Replace single backslash wiht doble backslash. \\\\ === \
  slave['remoteFS'] = aSlave.getRemoteFS().replaceAll("\\\\", "\\\\\\\\")
  slave['executors'] = aSlave.getNumExecutors().toString()
  slave['labels'] = aSlave.getLabelString()
  // Expected output from aSlave.getMode().getClass().getName() is hudson.model.Node$Mode
  // That's why we replace $ with . and then glue mode to the end
  slave['mode'] = aSlave.getMode().getClass().getName().replaceAll(~/\$/, ".") + '.' + aSlave.getMode()
  // Expected output of .getClass().getName() is hudson.slaves.RetentionStrategy$Always
  // So we replace $ with .
  String retention = aSlave.getRetentionStrategy().getClass().getName().replaceAll(~/\$/, ".")
  // Capitalize last word
  retention = retention.replaceAll(retention.tokenize('.').last(), retention.tokenize('.').last().toUpperCase())
  retention = retention.replaceAll('ALWAYS', 'INSTANCE')
  slave['retention'] = retention
  // Slave name shouldn't contain any - otherwise parser going crazy
  result["${aSlave.getNodeName()}".replaceAll(~/-/, "").replaceAll(~/\s/, "")] = slave
}

StringWriter sw = new StringWriter();
ConfigObject co = new ConfigObject()
co.put('slaves', result)
co.writeTo(sw)

// The whole method is a hack because groovy 2.x has its own implementation of prettyPrint
// but at the moment of writting Jenkins runs 1.8.9
def prettyPrint(String config) {
  println "Input config: \n $config \n"
  def ps = config.replaceAll(~/=\['/, " {\n\t")
  ps = ps.replaceAll(~/, '/, "\n\t")
  ps = ps.replaceAll(~/':\['/, " {\n\t")
  ps = ps.replaceAll(~/\]/, "\n}\n")
  ps = ps.replaceAll(~/':/, " = ")
  ps = ps.replaceAll(~/^\s:/, "\t")
  // It should be possible to do it better
  ps = ps.replaceAll(~/mode = \'/, "mode = ")
  ps = ps.replaceAll(~/retention = \'/, "retention = ")
  ps = ps.replaceAll(~/RetentionStrategy.INSTANCE\'/, "RetentionStrategy.INSTANCE")
  ps = ps.replaceAll(~/Mode.EXCLUSIVE\'/, "Mode.EXCLUSIVE")
  ps = ps.replaceAll(~/Mode.NORMAL\'/, "Mode.NORMAL")
  println "Output config: \n $ps \n"
}

prettyPrint(sw.toString())
