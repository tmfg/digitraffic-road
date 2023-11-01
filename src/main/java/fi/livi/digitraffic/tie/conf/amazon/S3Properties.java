package fi.livi.digitraffic.tie.conf.amazon;

public class S3Properties {
    protected final String s3BucketName;
    protected String s3Region;

    public S3Properties(final String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getS3Region() {
        return s3Region;
    }

    public void setS3Region(final String s3Region) {
        this.s3Region = s3Region;
    }
}
