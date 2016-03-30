package job

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
            jm.availableFiles['file.sh'] =
                    '#!/usr/bin/env bash\n' +
                    'echo "Hello from file!"'

            jp.setJm(jm)
            jp
        }
}
