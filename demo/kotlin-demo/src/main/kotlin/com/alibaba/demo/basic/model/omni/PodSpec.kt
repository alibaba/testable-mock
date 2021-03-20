package com.alibaba.demo.basic.model.omni

class PodSpec {
    var activeDeadlineSeconds: Long? = null
    var automountServiceAccountToken: Boolean? = null
    var containers: Array<Container> = arrayOf()
    var dnsPolicy: String? = null
    var enableServiceLinks: Boolean? = null
    var hostIPC: Boolean? = null
    var hostNetwork: Boolean? = null
    var hostPID: Boolean? = null
    var hostname: String? = null
    var initContainers: Array<Container> = arrayOf()
    var nodeName: String? = null
    var nodeSelector: Map<String, String>? = null
    var preemptionPolicy: String? = null
    var priority: Int? = null
    var priorityClassName: String? = null
    var restartPolicy: String? = null
    var runtimeClassName: String? = null
    var schedulerName: String? = null
    var serviceAccount: String? = null
    var serviceAccountName: String? = null
    var setHostnameAsFQDN: Boolean? = null
    var shareProcessNamespace: Boolean? = null
    var subdomain: String? = null
    var terminationGracePeriodSeconds: Long? = null
}
