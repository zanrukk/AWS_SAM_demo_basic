package dboperations;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Select implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(System.getenv("dbEndpoint"),System.getenv("dbRegion"))).build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("ITEMS");
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("CATEGORY = :c")
                .withValueMap(new ValueMap()
                        .withString(":c", System.getenv("Category")));

        ItemCollection<QueryOutcome> items = table.query(spec);

        Iterator<Item> iterator = items.iterator();
        Item item = null;
        String s = "";
        while (iterator.hasNext()) {
            item = iterator.next();
            s += item.toJSONPretty();
        }

        return response
                .withStatusCode(200)
                .withBody(s);

    }


}