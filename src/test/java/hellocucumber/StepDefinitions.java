package hellocucumber;

import io.cucumber.java.en.*;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import javax.xml.xpath.XPathConstants;

import static org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    @Given("job ref is {string}")
    public void job_ref_is(String string) {
        m_jobRef = string;
    }
    @When("GetJob is called")
    public void CallGetJob() {

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

        try {
            m_response = CallScheduler(request);
        }
        catch( java.io.IOException e )
        {
            System.out.println("IOException calling the Scheduler:" + e.toString());
            fail("get job failed.");
        }
    }

    @Then("return code should be {string}")
    public void return_code_should_be(String string) throws javax.xml.parsers.ParserConfigurationException, java.io.IOException, org.xml.sax.SAXException, XPathExpressionException {
        ByteArrayInputStream is = new ByteArrayInputStream(m_responseBytes);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
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

    private String CallScheduler(String request) throws java.io.IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[request.length()];
        buffer = request.getBytes();
        bout.write(buffer);
        byte[] b = bout.toByteArray();

        // open connection
        URL url = new URL("http://localhost:9542");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);
        http.setDoInput(true);
        con.setRequestProperty("Content-Length", String.valueOf(b.length));
//        con.setRequestProperty("Content-Type", "application/soap+xml;charset=UTF-8;action=\"capeconnect:servicepower:spPortType#GetJob\"");
        con.setRequestProperty("Content-Type", "application/soap+xml;charset=UTF-8");

        //Send request
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(b);
        wr.close();

        // receive response
        DataInputStream rd = new DataInputStream(con.getInputStream());
        InputStreamReader isr = new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(isr);
        m_responseBytes = readAllBytes((http.getInputStream()));
        rd.close();

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

    String m_jobRef;
    byte[] m_responseBytes;
    String m_response;
}
