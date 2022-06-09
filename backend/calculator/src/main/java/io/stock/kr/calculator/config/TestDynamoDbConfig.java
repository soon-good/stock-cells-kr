package io.stock.kr.calculator.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.*;

@Configuration
@Profile({"test-docker", "test-in-memory", "test-live"})
@EnableDynamoDBRepositories(
        basePackages = {
                "io.stock.kr.calculator"
        },
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "io.stock.kr.calculator.*.repository.dynamo.*Repository"),
//                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "io.stock.kr.calculator.*.domain.dynamo.*Document")
        }
)
public class TestDynamoDbConfig {
        @Profile({"test-docker", "test-in-memory", "test-live"})
        @Bean
        @Primary
        public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB){
                return new DynamoDBMapper(amazonDynamoDB);
        }

        @Profile({"test-docker", "test-live"})
        @Bean
        public AmazonDynamoDB amazonDynamoDB(){
                AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                "ACCESS KEY",
                                "SECRET KEY"
                        )
                );

                EndpointConfiguration endpointConfiguration = new EndpointConfiguration(
                        "http://localhost:8000",
                        Regions.AP_NORTHEAST_2.getName()
                );

                AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                        .withCredentials(credentialsProvider)
                        .withEndpointConfiguration(endpointConfiguration)
//                        .withRegion(Regions.AP_NORTHEAST_2)
                        .build();

                return amazonDynamoDB;
        }

        @Profile("test-in-memory")
        @Bean
        public AmazonDynamoDB inMemoryAmazonDB(){
                return null;
        }
}
