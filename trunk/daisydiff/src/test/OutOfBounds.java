import java.io.StringReader;
import java.util.Locale;

import junit.framework.TestCase;

import org.outerj.daisy.diff.DaisyDiff;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;


public class OutOfBounds extends TestCase {

    public void testOutOfBounds() throws Exception {
        
        String html1 = "<html><body>var v2</body></html>";
        String html2 = "<html>  \n  <body>  \n  Hello world  \n  </body>  \n  </html>";
    
        DaisyDiff.diffHTML(new InputSource(new StringReader(html1)), new InputSource(new StringReader(html2)), new DefaultHandler(), "test", Locale.ENGLISH);
        
    }
    
}
