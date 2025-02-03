package hellocucumber;

import io.cucumber.java.en.*;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import javax.xml.xpath.XPathConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import static org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    public StepDefinitions() throws javax.xml.parsers.ParserConfigurationException, TransformerConfigurationException
    {
        m_factory = DocumentBuilderFactory.newInstance();
        m_builder = m_factory.newDocumentBuilder();
        m_transformerFactory = TransformerFactory.newInstance();
        m_transformer = m_transformerFactory.newTransformer();
    }

    @Given("job ref is {string}")
    public void job_ref_is(String string) throws SAXException, IOException {
        m_jobRef = string;
        String request = "";
        request += "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:sp=\"http://www.servicepower.com/sp.xsd1\">\n";
        request += "    <soap:Header/>\n";
        request += "    <soap:Body>\n";
        request += "        <sp:GetJobRequest>\n";
        request += "            <login>\n";
        request += "                <databaseUser>mc</databaseUser>\n";
        request += "                <password>mc</password>\n";
        request += "                <spUser>m.coburn</spUser>\n";
        request += "            </login>\n";
        request += "            <jobID>" + m_jobRef + "</jobID>\n";
        request += "        </sp:GetJobRequest>\n";
        request += "    </soap:Body>\n";
        request += "</soap:Envelope>";

        m_doc = m_builder.parse(new InputSource(new StringReader(request)));
    }
    
    @Given("request file is {string}")
    public void request_file_is(String string) throws SAXException, IOException {
        File xmlFile = new File("src/test/resources/hellocucumber/" + string);
        m_doc = m_builder.parse(xmlFile);
    }
    
    @When("GetJob is called")
    public void CallGetJob() throws SAXException, IOException {

        try {
            m_response = CallScheduler();
        }
        catch( java.io.IOException e )
        {
            System.out.println("IOException calling the Scheduler:" + e.toString());
            fail("get job failed.");
        }
        catch( TransformerException e )
        {
        }
    }

    @Then("return code should be {string}")
    public void return_code_should_be(String string) throws javax.xml.parsers.ParserConfigurationException, java.io.IOException, org.xml.sax.SAXException, XPathExpressionException {
        
        ByteArrayInputStream is = new ByteArrayInputStream(m_responseBytes);
        Document doc = m_builder.parse(is);
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList)xPath.evaluate("//result/code", doc, XPathConstants.NODESET);

        boolean success = true;
        for (int i = 0; i < nodes.getLength(); ++i) {
            Element e = (Element) nodes.item(i);
            System.out.println("tag name: " + e.getTagName());
            System.out.println("content: " + e.getTextContent());

            if( e.getTextContent().compareTo(string) != 0 )
            {
                success = false;
            }
        }

        if( !success )
        {
            System.out.println("Return code does NOT match");
            fail("get job failed.");
        }
    }

    @Then("response data should be {string}")
    public void CompareResponseDataToFile(String string) throws javax.xml.parsers.ParserConfigurationException, java.io.IOException, org.xml.sax.SAXException, XPathExpressionException {
        File xmlFile = new File("src/test/resources/hellocucumber/" + string);
        m_doc = m_builder.parse(xmlFile);
    }

    private String CallScheduler() throws java.io.IOException, TransformerException {

        // save dom document to bytearray
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bout);
        DOMSource source = new DOMSource(m_doc);
        m_transformer.transform(source, result);
        byte[] b = bout.toByteArray();

        // open connection
        URL url = new URL("http://localhost:9542");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setDoInput(true);
        con.setRequestProperty("Content-Length", String.valueOf(b.length));
        con.setRequestProperty("Content-Type", "application/soap+xml;charset=UTF-8");

        // write request
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(b);
        wr.close();

        // read response
        m_responseBytes = readAllBytes((http.getInputStream()));
        return m_responseBytes.toString();
    }

    public static byte[] readAllBytes(java.io.InputStream inputStream) throws java.io.IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        java.io.IOException exception = null;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (java.io.IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (java.io.IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    DocumentBuilderFactory m_factory;
    DocumentBuilder m_builder;
    Document m_doc;
    TransformerFactory m_transformerFactory;
    Transformer m_transformer;

    String m_jobRef;
    byte[] m_responseBytes;
    String m_response;
}
