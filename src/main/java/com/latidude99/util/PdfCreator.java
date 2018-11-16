package com.latidude99.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

//import com.lowagie.text.DocumentException;

import static com.itextpdf.text.pdf.BaseFont.EMBEDDED;
import static com.itextpdf.text.pdf.BaseFont.IDENTITY_H;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

@Component
public class PdfCreator {
	private static final String UTF_8 = "UTF-8";
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@SuppressWarnings("rawtypes")
	public ByteArrayInputStream createPdf(String templateName, Map map) throws Exception {
		Assert.notNull(templateName, "The templateName can not be null");
		Context ctx = new Context();
		if (map != null) {
		     Iterator itMap = map.entrySet().iterator();
		       while (itMap.hasNext()) {
			  Map.Entry pair = (Map.Entry) itMap.next();
		          ctx.setVariable(pair.getKey().toString(), pair.getValue());
			}
		}
		
		  String processedHtml = templateEngine.process(templateName, ctx);
		  String xHtml = convertToXhtml(processedHtml);
		
		  ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		  String fileName = UUID.randomUUID().toString();
//	        try {
//	            final File outputFile = File.createTempFile(fileName, ".pdf");
//	            os = new FileOutputStream(outputFile);

	            ITextRenderer renderer = new ITextRenderer();
	            renderer.setDocumentFromString(xHtml);
	            renderer.layout();
	            renderer.createPDF(bos, false);
	            renderer.finishPDF();
	            System.out.println("PDF created successfully");
	            System.out.println(File.createTempFile("temp-file", "tmp").getParent());
//	        }
//	        finally {
//	            if (bos != null) {
//	                try {
//	                    bos.close();
//	                } catch (IOException e) { /*ignore*/ }
//	            }
//	        }
	        
	    return new ByteArrayInputStream(bos.toByteArray());
	        
	}
	
	private String convertToXhtml(String html) throws UnsupportedEncodingException {
	    Tidy tidy = new Tidy();
	    tidy.setInputEncoding(UTF_8);
	    tidy.setOutputEncoding(UTF_8);
	    tidy.setXHTML(true);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(UTF_8));
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    tidy.parseDOM(inputStream, outputStream);
	    return outputStream.toString(UTF_8);
	}
}




















