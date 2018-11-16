package com.latidude99.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.latidude99.model.Enquiry;

//import com.itextpdf.text.BaseColor;



public class GenerateEnquiryListPdfFromCode {
	
		    public static ByteArrayInputStream enquiryListReport(List<Enquiry> enquiries) {

		    	Document document = new Document(PageSize.A4, 20f, 20f, 20f, 20f);
		    	
		    	FormatStyle formatStyleDate = FormatStyle.MEDIUM;
		    	FormatStyle formatStyleTime = FormatStyle.SHORT;
		        
		    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	
		        try {
	
		            PdfPTable table = new PdfPTable(8);
		            table.setWidthPercentage(100);
		            table.setWidths(new int[]{5, 20, 35, 30, 20, 30, 30, 20});
	
		            Font headFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 8f);
		            Font cellFont = FontFactory.getFont(FontFactory.COURIER, 6.5f);
		            
		            
		            PdfPCell hcell;
		            hcell = new PdfPCell(new Phrase("Id", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
	
		            hcell = new PdfPCell(new Phrase("Customer", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
	
		            hcell = new PdfPCell(new Phrase("Customer's  Email", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
		            
		            hcell = new PdfPCell(new Phrase("Created", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
		            
		            hcell = new PdfPCell(new Phrase("Status", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
		            
		            hcell = new PdfPCell(new Phrase("Assigned to", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
		            
		            hcell = new PdfPCell(new Phrase("Closing date", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
		            
		            hcell = new PdfPCell(new Phrase("Closed by", headFont));
		            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
		            hcell.setBorderColorBottom(BaseColor.BLUE);
		            table.addCell(hcell);
	
		            for (Enquiry enquiry : enquiries) {
	
		                PdfPCell cell;
		                		                
	
		                cell = new PdfPCell(new Phrase(Long.toString(enquiry.getId()), cellFont));
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                table.addCell(cell);
	
		                cell = new PdfPCell(new Phrase(enquiry.getName(), cellFont));
		                cell.setPaddingLeft(5);
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                table.addCell(cell);
	
		                cell = new PdfPCell(new Phrase(String.valueOf(enquiry.getEmail()), cellFont));
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                cell.setPaddingRight(5);
		                table.addCell(cell);
		                
		                cell = new PdfPCell(new Phrase(enquiry.getCreatedDate().
		                		format(DateTimeFormatter.ofLocalizedDate(formatStyleDate)) + ", " +
		                		enquiry.getCreatedDate().
		                		format(DateTimeFormatter.ofLocalizedTime(formatStyleTime)), cellFont));
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                cell.setPaddingRight(5);
		                table.addCell(cell);
		                
		                cell = new PdfPCell(new Phrase(enquiry.getStatus(), cellFont));
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                cell.setPaddingRight(5);
		                table.addCell(cell);
		                
		                
		                String progressUser;
		                if(!enquiry.getStatus().equals("waiting") && enquiry.getSortedProgressUsersWithDate() != null) {
		                	progressUser = enquiry
		                			.getSortedProgressUsersWithDate().
		                			get(enquiry.getSortedProgressUsersWithDate().size() -1);
		                }else {
		                	progressUser = "-----";
		                }
		                cell = new PdfPCell(new Phrase(progressUser, cellFont));
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                cell.setPaddingRight(5);
		                table.addCell(cell);
		                
		                
		                String closedDate;
		                if(!enquiry.getStatus().equals("waiting") && enquiry.getClosedDate() != null) {
		                	closedDate = enquiry.getClosedDate()
		                			.format(DateTimeFormatter.ofLocalizedDate(formatStyleDate)) + ", " +
		                			enquiry.getClosedDate()
		                			.format(DateTimeFormatter.ofLocalizedDate(formatStyleTime));
		                }else {
		                	closedDate = "-----";
		                }
		                cell = new PdfPCell(new Phrase(closedDate, cellFont));
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                cell.setPaddingRight(5);
		                table.addCell(cell);
		                
		                
		                String closingUser;
		                if(!enquiry.getStatus().equals("waiting") && enquiry.getClosingUser() != null) {
		                	closingUser = enquiry.getClosingUser().getName();
		                }else {
		                	closingUser = "-----";
		                }
		                cell = new PdfPCell(new Phrase(closingUser, cellFont));
		                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		                cell.setBorderColorBottom(BaseColor.GREEN);
		                cell.setPaddingRight(5);
		                table.addCell(cell);
		            }
		            
		            
	
		            PdfWriter.getInstance(document, out);
		            
		            document.open();
		            document.add(table);
		            document.close();
		            
		        } catch (DocumentException ex) {
		        
		            Logger.getLogger(GenerateEnquiryListPdfFromCode.class.getName()).log(Level.SEVERE, null, ex);
		        }
	
		        return new ByteArrayInputStream(out.toByteArray());
		    }
		

}














