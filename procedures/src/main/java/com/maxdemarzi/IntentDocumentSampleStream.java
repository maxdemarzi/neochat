package com.maxdemarzi;

import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;

import java.io.IOException;
import java.util.Vector;

public class IntentDocumentSampleStream implements ObjectStream<DocumentSample> {

    String category;
    ObjectStream<String> stream;


    public IntentDocumentSampleStream(String category, ObjectStream<String> stream) {
        this.category = category;
        this.stream = stream;
    }

    public DocumentSample read() throws IOException {
        String sampleString = stream.read();

        if (sampleString != null) {

            // Whitespace tokenize entire string
            String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(sampleString);

            //remove entities
            Vector<String> vector = new Vector<String>(tokens.length);
            boolean skip = false;
            for (String token : tokens) {
                if (!token.startsWith("<")) {
                    vector.add(token);
                }
            }

            tokens = new String[vector.size()];
            vector.copyInto(tokens);

            DocumentSample sample;

            if (tokens.length > 0) {
                sample = new DocumentSample(category, tokens);
            } else {
                throw new IOException("Empty lines are not allowed!");
            }

            return sample;
        } else {
            return null;
        }
    }

    public void reset() throws IOException, UnsupportedOperationException {
        stream.reset();
    }

    public void close() throws IOException {
        stream.close();
    }
}