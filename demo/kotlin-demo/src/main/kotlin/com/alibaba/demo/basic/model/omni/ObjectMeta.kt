package com.alibaba.demo.basic.model.omni

import java.util.ArrayList

class ObjectMeta {
    var annotations: Map<String, String>? = null
    var clusterName: String? = null
    var creationTimestamp: String? = null
    var deletionGracePeriodSeconds: Long? = null
    var deletionTimestamp: String? = null
    var finalizers: List<String?> = ArrayList<String?>()
    var generateName: String? = null
    var generation: Long? = null
    var labels: Map<String, String>? = null
    var name: String? = null
    var namespace: String? = null
    var resourceVersion: String? = null
    var selfLink: String? = null
    var uid: String? = null
}
