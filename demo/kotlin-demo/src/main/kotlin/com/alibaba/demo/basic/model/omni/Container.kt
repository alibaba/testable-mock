package com.alibaba.demo.basic.model.omni

import java.util.ArrayList
import java.util.HashMap

class Container {
    var args: List<String?> = ArrayList()
    var command: String? = null
    var env: Map<String, String> = HashMap()
    var image: String? = null
    var imagePullPolicy: String? = null
    var livenessProbe: Probe? = null
    var name: String? = null
    var ports: List<ContainerPort?> = ArrayList()
    var readinessProbe: Probe? = null
    var startupProbe: Probe? = null
    var stdin: Boolean? = null
    var stdinOnce: Boolean? = null
    var terminationMessagePath: String? = null
    var terminationMessagePolicy: String? = null
    var tty: Boolean? = null
    var workingDir: String? = null
}
