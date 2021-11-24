package com.aws.lambda;

public class RequestInput {
    private String bucketName;
    private String prefix;
    private long maxSize;

    public RequestInput() {}

    public RequestInput(String bucketName, String prefix, long maxSize) {
        this.bucketName = bucketName;
        this.prefix = prefix;
        this.maxSize = maxSize;
    }

    public boolean isNull() {
        return (this.bucketName == null) || (this.prefix == null);
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
}
