package job

public class Helpers {

  public Helpers() {
  }

  public static String readEnvVariable(variableName, defaultValue = null) {
    def thr = Thread.currentThread()
    println "--> Read environment variable ${variableName}"
    // if we are running in Jenkins
    def value = ""
    if (thr.hasProperty('executable')) {
      def build = thr?.executable
      value = build.parent.builds[0].properties.get("envVars")[variableName]
    } else {
      value = defaultValue
    }
    println "--> ${variableName} = ${value}"
    return value
  }
}
