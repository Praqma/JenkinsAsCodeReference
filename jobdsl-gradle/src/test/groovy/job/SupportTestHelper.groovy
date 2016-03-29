package job

/**
 * Created by asa on 21/03/16.
 */
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.MemoryJobManagement

class SupportTestHelper {
        def static JobParent getJobParent() {
            JobParent jp = new JobParent() {
                @Override
                Object run() {
                    return null
                }
            }
            JobManagement jm = new MemoryJobManagement()
            jp.setJm(jm)
            jp
        }
}
