package com.aliyun.openservices.spring.example.order;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.aliyun.openservices.ons.api.order.OrderProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.spring.example.common.CustomConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class QuotaConfigProducerClient {

    private static final Logger LOG = LoggerFactory.getLogger(QuotaConfigProducerClient.class);

    // mongo variables
    private static MongoCollection<Document> collection;
    private static BasicDBObject query;

    private static void initMongoConnection() {
        try {
            MongoClient mongoClient = MongoClients.create(CustomConfig.DEFAULT_MONGODB_URI);
            MongoDatabase database = mongoClient.getDatabase(CustomConfig.DEFAULT_MONGODB_DB);
            collection = database.getCollection("emp_advertisement");
            query = new BasicDBObject();
        } catch (Exception ex) {
            LOG.error("Error when initializing mongo config", ex);
        }
    }

    public static void main(String[] args) {

        // init mongo
        initMongoConnection();

        /**
         * 生产者Bean配置在order_producer.xml中,可通过ApplicationContext获取或者直接注入到其他类(比如具体的Controller)中.
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("order_producer.xml");
        OrderProducer orderProducer = (OrderProducer) context.getBean("producer");

        Properties properties = (Properties) context.getBean("commonProperties");
        String topic = properties.getProperty("Topic");
        String shardingKey = "OrderedKey";

        //循环发送消息
        MongoCursor<Document> cursor = collection.find().iterator();

        for (MongoCursor<Document> iterator = cursor; iterator.hasNext(); ) {
            Document doc = iterator.next();
            ObjectId adId = doc.getObjectId("_id");
            String adIdString = adId.toString();
            long adAndDeviceDayLimit = doc.getInteger("deviceLimit") != null ? doc.getInteger("deviceLimit") : Long.MAX_VALUE;
            long adDayLimit = doc.getInteger("dailyLimit") != null ? doc.getInteger("dailyLimit") : Long.MAX_VALUE;
            long adTotalLimit = doc.getInteger("quota") != null ? doc.getInteger("quota") : Long.MAX_VALUE;

            JSONObject adAndDeviceDayObject = new JSONObject();
            adAndDeviceDayObject.put("type", "AD_DEVICE_DAY");
            adAndDeviceDayObject.put("key", adIdString);
            adAndDeviceDayObject.put("value", adAndDeviceDayLimit);

            JSONObject adDayObject = new JSONObject();
            adDayObject.put("type", "AD_DAY");
            adDayObject.put("key", adIdString);
            adDayObject.put("value", adDayLimit);

            JSONObject adTotalObject = new JSONObject();
            adTotalObject.put("type", "AD_TOTAL");
            adTotalObject.put("key", adIdString);
            adTotalObject.put("value", adTotalLimit);

            List<JSONObject> objects = new ArrayList<JSONObject>();
            objects.add(adAndDeviceDayObject);
            objects.add(adDayObject);
            objects.add(adTotalObject);

            for (int i = 0; i < objects.size(); i++) {
                Message msg = new Message( //
                        // Message所属的Topic
                        topic,
                        // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                        "quota",
                        // Message Body 可以是任何二进制形式的数据， MQ不做任何干预
                        // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                        objects.get(i).toJSONString().getBytes());
                // 设置代表消息的业务关键属性，请尽可能全局唯一
                // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发
                // 注意：不设置也不会影响消息正常收发
                msg.setKey("quota_config");
                // 发送消息，只要不抛异常就是成功
                try {
                    SendResult sendResult = orderProducer.send(msg, shardingKey);
                    assert sendResult != null;
                    System.out.println(sendResult);
                } catch (ONSClientException ex) {
                    LOG.error("send config from mongo error", ex);
                    //出现异常意味着发送失败，为了避免消息丢失，建议缓存该消息然后进行重试。
                }
            }
        }

        orderProducer.shutdown();
    }
}
