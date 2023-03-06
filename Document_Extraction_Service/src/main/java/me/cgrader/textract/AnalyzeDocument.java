// snippet-sourcedescription:[AnalyzeDocument.java demonstrates how to analyze a document.]
// snippet-keyword:[AWS SDK for Java v2]
// snippet-service:[Amazon Textract]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package me.cgrader.textract;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.io.*;
import java.util.*;

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 * For more information, see the following documentation topic:
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
public class AnalyzeDocument {
    static Logger logger = LogManager.getLogger(AnalyzeDocument.class);

    public static List analyzeDoc(TextractClient textractClient, String sourceDoc) {

        List<String> list = new ArrayList<>();

        try (InputStream sourceStream = new FileInputStream(sourceDoc)) {
            SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

            // Get the input Document object as bytes
            Document myDoc = Document.builder()
                    .bytes(sourceBytes)
                    .build();

            List<FeatureType> featureTypes = new ArrayList<>();
            featureTypes.add(FeatureType.FORMS);
            featureTypes.add(FeatureType.TABLES);

            AnalyzeDocumentRequest analyzeDocumentRequest = AnalyzeDocumentRequest.builder()
                    .featureTypes(featureTypes)
                    .document(myDoc)
                    .build();

            AnalyzeDocumentResponse analyzedDocument = textractClient.analyzeDocument(analyzeDocumentRequest);
            List<Block> docInfo = analyzedDocument.blocks();
            Iterator<Block> blockIterator = docInfo.iterator();

            while (blockIterator.hasNext()) {
                Block block = blockIterator.next();
                logger.debug("The block type is " +block.blockType().toString());
                if (block.blockType().equals(BlockType.LINE)) {
                    list.add(block.text());
                }
            }

            DocumentMetadata documentMetadata = analyzedDocument.documentMetadata();
            logger.info("The number of pages in the document is " + documentMetadata.pages());

        } catch (TextractException | IOException e) {
            logger.error(e.getMessage());
        }
        return list;
    }
}
