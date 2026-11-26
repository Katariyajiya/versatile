package com.versatile.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class PdfExtractionService {
    private static final int MAX_EXTRACTED_CHARS=12_000;

    public String extractText(MultipartFile file){
        if (file==null || file.isEmpty()){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST,"Uploaded PDF file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are supported");
        }
        try(PDDocument document =Loader.loadPDF(file.getBytes())){
            if (document.isEncrypted()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encrypted PDFs are not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);
            String cleanedText = rawText.replaceAll("\\s+"," ").trim();
            if (cleanedText.isEmpty()){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No readable text found in PDF. Scanned/image-only PDFs are not supported yet."
                );
            }
            return cleanedText.length()>MAX_EXTRACTED_CHARS?cleanedText.substring(0,MAX_EXTRACTED_CHARS):cleanedText;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read PDF file: " + e.getMessage());
              }
         }
    }

