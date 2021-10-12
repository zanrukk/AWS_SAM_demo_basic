package dboperations;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;



import java.util.HashMap;
import java.util.Map;

public class Insert implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
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
        Item item = new Item()
                .withPrimaryKey("CATEGORY", System.getenv("Category"), "TITLE", System.getenv("Title"))
                .withNumber("PRICE", Integer.parseInt(System.getenv("Price")))
                .withNumber("STOCK", Integer.parseInt(System.getenv("Stock")));
        PutItemOutcome outcome = table.putItem(item);

        String s = String.format("{ \"message\": \"Item: %s added.\"}",item.getString("TITLE"));
        return response
                .withStatusCode(200)
                .withBody(s);

    }


}
