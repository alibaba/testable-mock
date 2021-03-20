package com.alibaba.demo.basic.model.omni;

/**
 * 这是一个简化了的Kubernetes Pod模型
 * This is a simplified kubernetes pod model
 */
public class Pod {

    // ---------- Member fields ----------

    private String apiVersion = "v1";
    private String kind = "Pod";
    private ObjectMeta metadata;
    private PodSpec spec;
    private PodStatus status;

    // ---------- Getters and Setters ----------

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public ObjectMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public PodSpec getSpec() {
        return spec;
    }

    public void setSpec(PodSpec spec) {
        this.spec = spec;
    }

    public PodStatus getStatus() {
        return status;
    }

    public void setStatus(PodStatus status) {
        this.status = status;
    }
}
