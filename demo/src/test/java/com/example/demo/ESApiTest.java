package com.example.demo;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.management.Query;
import java.io.IOException;
import java.util.ArrayList;

@SpringBootTest
public class ESApiTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;



    // 创建索引
    @Test
    public void testCreateIndex() throws IOException {
        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("test_index");
        // 执行创建索引请求
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testIndexExists() throws IOException {
        // 判断索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest("test_index");
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println("索引test_index是否存在？ "+exists);
    }

    @Test
    void testDeleteIndex() throws IOException {
        // 删除索引
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("test_index");
        AcknowledgedResponse response = restHighLevelClient.indices()
                .delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println("索引test_index是否删除成功？ "+response.isAcknowledged());
    }

    // 创建文档
    @Test
    void testCreateDocument() throws IOException {
        User user = new User("动火作业",22);
        // 创建请求
        IndexRequest indexRequest = new IndexRequest("test_index");
        // 设置文档id
        indexRequest.id("100");
        // 放入数据
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
        // 客户端发送请求，获得响应结果
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        System.out.println("响应结果 "+indexResponse.toString());
        System.out.println("状态 "+indexResponse.status());
    }

    // 获取文档
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("test_index", "123");
        // 不获取 _source 返回的上下文，效率会更高
//        getRequest.fetchSourceContext(new FetchSourceContext(false));
//        getRequest.storedFields("_none_");
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println("文档是否存在？ "+exists);
        if (exists){
            GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            System.out.println("获取文档内容 "+response.getSourceAsString());
//            System.out.println("获取文档 "+response);
        }
    }

    // 更新文档
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("test_index", "123");
        User user = new User("李四", 24);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println("更新？ "+update.status());
    }

    // 删除文档
    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("test_index", "123");
        DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println("删除？ "+response.status());
    }

    // 批量插入
    @Test
    void testBulkAddDocument() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userList = new ArrayList<>(3);
        userList.add(new User("动物园",21));
        userList.add(new User("动作片",22));
        userList.add(new User("做作业",23));
        for (int i=0;i<userList.size();i++){
            bulkRequest.add(new IndexRequest("test_index")
                    .id(""+(i+1))
                    .source(JSON.toJSONString(userList.get(i)),XContentType.JSON)

            );
        }

        BulkResponse responses = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println("是否失败？ "+responses.hasFailures());
    }

    // 查询
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("test_index");
        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 查询条件
        // QueryBuilders.termQuery 精确查询
        TermQueryBuilder termBuilder = QueryBuilders.termQuery("name", "张三");
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("name", "动物园");
        MatchQueryBuilder queryBuilder2 = QueryBuilders.matchQuery("age", "22");


        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //must中的条件，必须全部匹配。需要将字段的type设置为keyword 或者 指定字段时用 `字段.keyword`(实际测试并不生效，可能还和analyzer有关)
        //boolQueryBuilder.must(QueryBuilders.matchQuery("name", "动业"));
        //boolQueryBuilder.must(QueryBuilders.matchQuery("age", 22));
        //匹配should中的条件（匹配1个或多个，根据需求配置）
        //boolQueryBuilder.should(QueryBuilders.matchQuery("age", "21"));
        //matchPhraseQuery 通配符搜索查询，支持 * 和 ?, ?匹配任意单个字符，这么查询可能慢
        //必须匹配的 should条件数量
        //boolQueryBuilder.minimumShouldMatch(1);




//        builder.from(0);
//        builder.size(10);
        //builder.query(queryBuilder);
        //builder.query(queryBuilder2);
        builder.query(queryBuilder);
        searchRequest.source(builder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("获取查询对象 "+JSON.toJSONString(searchResponse.getHits()));
        System.out.println("===================");
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            System.out.println("for循环获取 "+documentFields.getSourceAsMap());
        }
    }

}
