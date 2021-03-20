package com.alibaba.demo.basic.model.omni

/**
 * 这是一个简化了的Kubernetes Pod模型
 * This is a simplified kubernetes pod model
 */
class Pod {
    var apiVersion = "v1"
    var kind = "Pod"
    var metadata: ObjectMeta? = null
    var spec: PodSpec? = null
    var status: PodStatus? = null
}
