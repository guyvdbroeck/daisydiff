import java.io.StringReader;
import java.util.Locale;

import junit.framework.TestCase;

import org.outerj.daisy.diff.DaisyDiff;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Test for regressions involving out of bounds exceptions
 * 
 * @author guy
 *
 */
public class OutOfBounds extends TestCase {

    public void testOutOfBounds1() throws Exception {
        
        String html1 = "<html><body>var v2</body></html>";
        String html2 = "<html>  \n  <body>  \n  Hello world  \n  </body>  \n  </html>";
    
        DaisyDiff.diffHTML(new InputSource(new StringReader(html1)), new InputSource(new StringReader(html2)), new DefaultHandler(), "test", Locale.ENGLISH);
        
    }
    
    public void testOutOfBounds2() throws Exception {
        
        String html1 = "<html>  \n  <body>  \n  Hello world  \n  </body>  \n  </html>";
        String html2 = "<html><body>var v2</body></html>";
    
        DaisyDiff.diffHTML(new InputSource(new StringReader(html1)), new InputSource(new StringReader(html2)), new DefaultHandler(), "test", Locale.ENGLISH);
        
    }
    
    public void testOutOfBounds3() throws Exception {
        
        String html1 = "<html><head></head><body><p>test</p></body></html>";
        String html2 = "<html><head></head><body></body></html>";
    
        DaisyDiff.diffHTML(new InputSource(new StringReader(html1)), new InputSource(new StringReader(html2)), new DefaultHandler(), "test", Locale.ENGLISH);
        
    }
    
    public void testOutOfBounds4() throws Exception {
        
        String html1 = "<html><head></head><body></body></html>";
        String html2 = "<html><head></head><body><p>test</p></body></html>";
    
        DaisyDiff.diffHTML(new InputSource(new StringReader(html1)), new InputSource(new StringReader(html2)), new DefaultHandler(), "test", Locale.ENGLISH);
        
    }
    
    public void testOutOfBounds5() throws Exception {
        
        String html1 = "<html><head></head><body><p>test</p><p>test</p></body></html>";
        String html2 = "<html><head></head><body></body></html>";
    
        DaisyDiff.diffHTML(new InputSource(new StringReader(html1)), new InputSource(new StringReader(html2)), new DefaultHandler(), "test", Locale.ENGLISH);
        
    }
    
    public void testOutOfBounds6() throws Exception {
        
        String html1 = "<html><head></head><body></body></html>";
        String html2 = "<html><head></head><body><p>test</p><p>test</p></body></html>";
    
        DaisyDiff.diffHTML(new InputSource(new StringReader(html1)), new InputSource(new StringReader(html2)), new DefaultHandler(), "test", Locale.ENGLISH);
        
    }
    
}
