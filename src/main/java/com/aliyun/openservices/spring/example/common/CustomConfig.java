package com.aliyun.openservices.spring.example.common;

/**
 * Custom config
 */
public class CustomConfig {
    // application
    public static final long EFFECTIVE_STAY_THRESHOLD = 3000;
    public static final long EFFECTIVE_WATCH_THRESHOLD = 8000;

    // flink
//    public static final int FLINK_MAX_PARALLELISM = 1000; // Required
    public static final int FLINK_MAX_PARALLELISM = 1; // Required


    // cassandra
    public static final String DEFAULT_CASSANDRA_HOST = "120.79.1.92";
    public static final String DEFAULT_CASSANDRA_USERNAME = "root";
    public static final String DEFAULT_CASSANDRA_PASSWORD = "admin123";

    // mongodb
    public static final String DEFAULT_MONGODB_URI = "mongodb://root:erisedmongodb@dds-wz902a2ec0214404-pub.mongodb.rds.aliyuncs.com:3717/?authSource=admin";
    public static final String DEFAULT_MONGODB_DB = "erised_media_publish";
    public static final String DEFAULT_MONGODB_COLLECTION = "emp_promotions";

    // hdfs
    public static final String DEFAULT_HDFS_URL = "hdfs://hadoop000:8020/";
    public static final long DEFAULT_HDFS_WRITE_BATCH_SIZE = 1024 * 1024 * 400;
    public static final long DEFAULT_HDFS_WRITE_BATCH_ROLLOVER_INTERVAL = 60 * 60 * 1000;
    public static final long DEFAULT_HDFS_WRITE_INACTIVE_BUCKET_THRESHOLD = 60 * 1000;

}
