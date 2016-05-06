import hudson.model.*
import jenkins.model.*
import java.net.InetAddress
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry

def addGlobalEnvVariable(Class jenkins, String key, String value) {
  instance = jenkins.getInstance()
  globalNodeProperties = instance.getGlobalNodeProperties()
  envVarsNodePropertyList = globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)
  if ( envVarsNodePropertyList == [] ) {
      EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry(key, value);
      globalNodeProperties.add(new EnvironmentVariablesNodeProperty(entry))
  } else {
      envVarsNodePropertyList.get(0).getEnvVars().put(key, value)
  }
  println "--> added global environment variable ${key} = ${value}"
  instance.save()
}

def readProperties(String properties_file) {
  println "--> Read properties from ${properties_file}"
  Properties properties = new Properties()
  File propertiesFile = new File(properties_file)
  propertiesFile.withInputStream {
    properties.load(it)
  }
  return properties
}