package com.simon.etlProcessor.enums;

public enum BucketName {
    INPUT_BUCKET("inputs-file-client1"),
    OUTPUT_BUCKET("output-files-client1");
    private final String bucketName;

    /**
     * @param text
     */
    BucketName(final String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return bucketName;
    }

}
