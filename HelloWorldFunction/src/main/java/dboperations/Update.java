package dboperations;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;

public class Update implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
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
        String oldPrice = table.getItem("CATEGORY", System.getenv("Category"), "TITLE", System.getenv("Title")).getString("PRICE");
        String oldStock = table.getItem("CATEGORY", System.getenv("Category"), "TITLE", System.getenv("Title")).getString("STOCK");
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("CATEGORY", System.getenv("Category"), "TITLE", System.getenv("Title"))
                .withUpdateExpression("set STOCK = :s, PRICE=:p")
                .withValueMap(new ValueMap().withNumber(":s", Integer.parseInt(System.getenv("NewStock"))).withNumber(":p", Integer.parseInt(System.getenv("NewPrice"))))
                .withReturnValues(ReturnValue.UPDATED_NEW);

        String s = "";
        try {
            System.out.println("Updating the item...");
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            s ="UpdateItem succeeded:\nPrevious STOCK: " + oldStock+ "\nPrevious COST: " + oldPrice + "\nNew Values:\n" + outcome.getItem().toJSONPretty();
            return response
                    .withStatusCode(200)
                    .withBody(s);
        }
        catch (Exception e) {
            s += "Unable to update item:\n";
            s += e.getMessage();
            return response
                    .withStatusCode(500)
                    .withBody(s);
        }

    }


}
