package com.app.superpos.pdf_report;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class PDFTemplate {

    private Context context;
    private File pdfFile;
    private Document document;
    PdfWriter pdfWriter;
    private Paragraph paragraph;
    //here you can change fonts,fonts size and fonts color


    private Font fTitle = FontFactory.getFont("assets/arial.ttf", BaseFont.IDENTITY_H, 6, Font.NORMAL, BaseColor.BLACK);
    private Font fSubTitle = FontFactory.getFont("assets/arial.ttf", BaseFont.IDENTITY_H, 4, Font.NORMAL, BaseColor.BLACK);
    private Font fText = FontFactory.getFont("assets/arial.ttf", BaseFont.IDENTITY_H, 4, Font.NORMAL, BaseColor.BLACK);
    private Font fHighText = FontFactory.getFont("assets/arial.ttf", BaseFont.IDENTITY_H, 4, Font.NORMAL, BaseColor.BLACK);
    private Font fRowText = FontFactory.getFont("assets/arial.ttf", BaseFont.IDENTITY_H, 4, Font.NORMAL, BaseColor.BLACK);

    public PDFTemplate(Context context) {
        this.context = context;
    }

    public void openDocument() {
        createFile();
        try {

            //adjust your page size here
            Rectangle pageSize = new Rectangle(164.41f, 500.41f); //14400 //for 58 mm pos printer
            document = new Document(pageSize);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
        } catch (Exception e) {
            Log.e("createFile", e.toString());
        }
    }

    private void createFile() {
        File folder = new File(Environment.getExternalStorageDirectory().toString(), "PDF");
        if (!folder.exists())
            folder.mkdir();

        //your file name
        pdfFile = new File(folder, "order_receipt.pdf");


    }

    public void closeDocument() {
        document.close();
    }

    public void addMetaData(String title, String subject, String author) {
        document.addTitle(title);
        document.addSubject(subject);
        document.addAuthor(author);

    }

    public void addTitle(String title, String subTitle, String date) {


        try {


            paragraph = new Paragraph();
            addChildP(new Paragraph(title, fTitle));
            addChildP(new Paragraph(subTitle, fSubTitle));
            addChildP(new Paragraph("Order Time:" + date, fHighText));
            document.add(paragraph);
        } catch (Exception e) {
            Log.e("addTitle", e.toString());
        }
    }

    public void addChildP(Paragraph childParagraph) {

        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }

    public void addParagraph(String text) {

        try {

            paragraph = new Paragraph(text, fText);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
        } catch (Exception e) {
            Log.e("data", e.toString());
        }


    }


    public void addRightParagraph(String text) {

        try {

            paragraph = new Paragraph(text, fText);
            paragraph.setSpacingAfter(1);
            paragraph.setSpacingBefore(1);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
        } catch (Exception e) {
            Log.e("addParagraph", e.toString());
        }


    }


    public void addImage(Bitmap bm) {

        try {

            Bitmap bmp = bm;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);

            byte[] byteArray = stream.toByteArray();
            // PdfImage img = new PdfImage(arg0, arg1, arg2)

            // Converting byte array into image Image
            Image img = Image.getInstance(byteArray);

            img.setAlignment(Image.ALIGN_BOTTOM);
            img.setAlignment(Image.ALIGN_CENTER);
            img.scaleAbsolute(80f, 20f);
            //img.setAbsolutePosition(imageStartX, imageStartY); // Adding Image

            document.add(img);
        } catch (Exception e) {
            Log.e("addParagraph", e.toString());
        }


    }

    public void createTable(String[] header, ArrayList<String[]> clients) {

        try {


            paragraph = new Paragraph();
            paragraph.setFont(fText);
            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.setSpacingBefore(1);

            PdfPCell pdfPCell;

            int indexC = 0;
            while (indexC < header.length) {
                pdfPCell = new PdfPCell(new Phrase(header[indexC++], fSubTitle));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBorderColor(BaseColor.GRAY);
                pdfPTable.addCell(pdfPCell);
            }

            for (int indexR = 0; indexR < clients.size(); indexR++) {
                String[] row = clients.get(indexR);

                for (indexC = 0; indexC < header.length; indexC++) {
                    pdfPCell = new PdfPCell(new Phrase(row[indexC], fRowText));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBorder(Rectangle.NO_BORDER);
                    pdfPTable.addCell(pdfPCell);
                }
            }

            paragraph.add(pdfPTable);
            document.add(paragraph);

        } catch (Exception e) {
            Log.e("createTable", e.toString());
        }
    }

    public void viewPDF() {


        Intent intent = new Intent(context, ViewPDFActivity.class);
        intent.putExtra("path", pdfFile.getAbsolutePath());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);


    }


}
