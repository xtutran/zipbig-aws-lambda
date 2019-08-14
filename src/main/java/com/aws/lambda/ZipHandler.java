package com.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHandler implements RequestHandler<RequestInput, String>{

    private void uploadToS3(AmazonS3 s3client, String bucketName, String fileName, InputStream stream, int length) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();

            metadata.setContentType("application/zip");
            metadata.addUserMetadata("x-amz-meta-title", "zip-data");
            metadata.setContentLength(length);
            //ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, stream, metadata);
            s3client.putObject(putObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String handleRequest(RequestInput input, Context context) {
        context.getLogger().log("Input: " + input.getBucketName() + "\n");
        context.getLogger().log("Input: " + input.getPrefix() + "\n");
        context.getLogger().log("Input: " + input.getMaxSize() + "\n");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

        String bucketName = input.getBucketName();
        String prefix = input.getPrefix();
        long maxSize = input.getMaxSize();

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddTHHmmss"));
        String outputFile = String.format("archive_%s.zip", currentTime);

        ListObjectsRequest objRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix);
        ObjectListing listing = s3Client.listObjects(objRequest);


        //final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
        ZipOutputStream zipOut = new ZipOutputStream(cbb.getOutputStream());

        int count = 0;
        long totalSize = 0;

        try {
            System.out.println("start loop");
            do {
                for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
                    System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
                    totalSize += objectSummary.getSize();
                    if (objectSummary.getKey().endsWith("/")) {
                        System.out.println("Skip folder");
                        continue;
                    }

                    if (totalSize > maxSize) {
                        System.out.println("Exceed 10GB");
                        totalSize -= objectSummary.getSize();
                        break;
                    }

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
                    obj.close();
                    zipOut.closeEntry();
                    count++;
                }
                objRequest.setMarker(listing.getNextMarker());
            } while (listing.isTruncated());
            System.out.println("end loop");

            zipOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int length = cbb.getAvailable();
        System.out.println("In available: " + length);

        uploadToS3(s3Client, bucketName, outputFile, cbb.getInputStream(), length);
        cbb.clear();

        return "Hello World - count: " + count + " total size: " + totalSize;
    }
}
