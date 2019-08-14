package com.aws.lambda;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class InvokeAsyncHandler implements RequestHandler<Object, String> {

    public String handleRequest(Object s, Context context) {

        String functionName = "zipfunction";
        String functionInput = "{\"who\":\"AWS SDK for Java\"}";

        AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.defaultClient();
        InvokeRequest req = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload(ByteBuffer.wrap(functionInput.getBytes()));

        Future<InvokeResult> future_res = lambda.invokeAsync(req);

        /*System.out.print("Waiting for future");
        while (!future_res.isDone()) {
            System.out.print(".");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("\nThread.sleep() was interrupted!");
                System.exit(1);
            }
        }

        try {
            InvokeResult res = future_res.get();
            if (res.getStatusCode() == 200) {
                System.out.println("\nLambda function returned:");
                ByteBuffer response_payload = res.getPayload();
                System.out.println(new String(response_payload.array()));
            } else {
                System.out.format("Received a non-OK response from AWS: %d\n",
                        res.getStatusCode());
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            //System.exit(1);
        } catch (ExecutionException e) {
            System.err.println(e.getMessage());
            //System.exit(1);
        }*/

        return req.toString();
    }
}
