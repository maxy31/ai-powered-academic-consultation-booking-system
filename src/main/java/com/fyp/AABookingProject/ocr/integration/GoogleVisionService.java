//package com.fyp.AABookingProject.ocr.integration;
//
//import com.google.cloud.vision.v1.*;
//import com.google.protobuf.ByteString;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class GoogleVisionService {
//
//    public String detectDocumentText(byte[] imageBytes) throws IOException {
//        List<AnnotateImageRequest> requests = new ArrayList<>();
//        ByteString imgBytes = ByteString.copyFrom(imageBytes);
//
//        Image img = Image.newBuilder().setContent(imgBytes).build();
//        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
//        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
//                .addFeatures(feat)
//                .setImage(img)
//                .build();
//        requests.add(request);
//
//        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
//            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
//            List<AnnotateImageResponse> responses = response.getResponsesList();
//
//            for (AnnotateImageResponse res : responses) {
//                if (res.hasError()) {
//                    throw new IOException("OCR Error: " + res.getError().getMessage());
//                }
//                return res.getFullTextAnnotation().getText();
//            }
//        }
//        return "";
//    }
//}