package com.fyp.AABookingProject.ocr.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@Service
public class GoogleOcrService {

    public String extractTextFromImage(MultipartFile file) throws Exception {
        // 读取图片字节
        ByteString imgBytes = ByteString.readFrom(file.getInputStream());

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = response.getResponses(0);

            // 检查是否有错误
            if (res.hasError()) {
                throw new RuntimeException("Google Vision OCR failed: " + res.getError().getMessage());
            }

            // 检查是否有识别内容
            if (!res.hasFullTextAnnotation()) {
                return ""; // 返回空字符串代表没有识别到文本
            }

            String text = res.getFullTextAnnotation().getText();
            System.out.println("OCR text: " + text); // 方便调试
            return text;
        }
    }
}
