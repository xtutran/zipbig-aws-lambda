package com.aws.lambdapoc;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MethodHandlerLambda {

    private void uploadToS3(AmazonS3 s3client, String bucketName, String fileName, byte[] bytes) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();

            metadata.setContentType("application/zip");
            metadata.addUserMetadata("x-amz-meta-title", "zip-data");
            metadata.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, byteArrayInputStream, metadata);
            s3client.putObject(putObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String handleRequest(String input, Context context) {
        context.getLogger().log("Input: " + input + "\n");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

        String bucketName = "my-emr-poc";
        String prefix = "axa_rev_sg/raw";

        ObjectListing listing = s3Client.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix));

        System.out.println("start loop");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(outputStream);

        try {
            for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
                System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());

                S3Object obj = s3Client.getObject(objectSummary.getBucketName(), objectSummary.getKey());
                InputStream objData = obj.getObjectContent();

                ZipEntry zipEntry = new ZipEntry(objectSummary.getKey());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = objData.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                objData.close();
                zipOut.closeEntry();

            }

            zipOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        uploadToS3(s3Client, "my-emr-poc", "test.zip", outputStream.toByteArray());
        System.out.println("end loop");

        return "Hello World - " + input;
    }
}
