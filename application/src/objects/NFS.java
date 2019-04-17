package objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;

public class NFS {
    private String number;
    private String verificationCode;
    private String tomador;
    private String cnpjCpf;
    private String downloadLink;
    private String rootUrl = "http://www2.goiania.go.gov.br/";

    public NFS(String nfsNr, String nfsVerification, String nfsTomador, String nfsCnpjCpf, String inscMunicipal) {
        this.number = nfsNr;
        this.verificationCode = nfsVerification;
        this.tomador = nfsTomador;
        this.cnpjCpf = nfsCnpjCpf;
        this.downloadLink = writeDownloadLinkToField(nfsNr, nfsVerification, inscMunicipal);
    }

    public static NFS getNFSObjectFromNode(String nodeLine, String inscMunicipal)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        String xPathNfsNumber = "/GerarNfseResposta/ListaNfse/CompNfse/Nfse/InfNfse/Numero";
        String xPathNfsVerificationCode = "/GerarNfseResposta/ListaNfse/CompNfse/Nfse/InfNfse/CodigoVerificacao";
        String xPathNfsRazaoTomador = "/GerarNfseResposta/ListaNfse/CompNfse/Nfse/InfNfse/DeclaracaoPrestacaoServico/InfDeclaracaoPrestacaoServico/Tomador/RazaoSocial";
        String xPathNfsCpf = "/GerarNfseResposta/ListaNfse/CompNfse/Nfse/InfNfse/DeclaracaoPrestacaoServico/InfDeclaracaoPrestacaoServico/Tomador/IdentificacaoTomador/CpfCnpj/Cpf";
        String xPathNfsCnpj = "/GerarNfseResposta/ListaNfse/CompNfse/Nfse/InfNfse/DeclaracaoPrestacaoServico/InfDeclaracaoPrestacaoServico/Tomador/IdentificacaoTomador/CpfCnpj/Cnpj";

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        DocumentBuilder dBuilder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
        InputSource inputSource = new InputSource();
        inputSource.setCharacterStream(new StringReader(nodeLine));
        Node node = (dBuilder.parse(inputSource)).getDocumentElement().getFirstChild();

        String nfsNumber = (xpath.evaluate(xPathNfsNumber, node).trim());
        String nfsVerificationCode = (xpath.evaluate(xPathNfsVerificationCode, node).trim());
        String nfsTomador = (xpath.evaluate(xPathNfsRazaoTomador, node).trim());
        String nfsCPF = (xpath.evaluate(xPathNfsCpf, node).trim());
        String nfsCNPJ = (xpath.evaluate(xPathNfsCnpj, node).trim());

        return new NFS(nfsNumber, nfsVerificationCode, nfsTomador, (nfsCPF.equals("") ? nfsCNPJ : nfsCPF), inscMunicipal);
    }

    private String writeDownloadLinkToField(String nfsNumber, String nfsVerificationCode, String inscMunicipal) {
        String url = rootUrl + "sistemas/snfse/asp/snfse00200w0.asp?inscricao=%s&nota=%s&verificador=%s";
        return String.format(url, inscMunicipal, nfsNumber, nfsVerificationCode);
    }

    public String getNumber() {
        return number;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getTomador() {
        return tomador;
    }

    public String getCnpjCpf() {
        return cnpjCpf;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void getHtmlNfsContent() {
        try {
            // Getting URL to display to user
            System.out.println(downloadLink + "\n");
            URL nfseUrl = new URL(downloadLink);

            // Retrieve and parse HTML content
            Document parsedDocument = Jsoup.parse(nfseUrl, 15000);
            parsedDocument.charset(Charset.forName("UTF-8"));
            Elements imgElements = parsedDocument.getElementsByTag("img");
            for (Element imgTag : imgElements) {
                if (!imgTag.attr("src").contains(rootUrl))
                    imgTag.attr("src", rootUrl + imgTag.attr("src"));
            }

            // Save HTML into new file to convert it
            FileWriter fw = new FileWriter(String.valueOf(hashCode()) + ".html");
            fw.append(parsedDocument.html());
            fw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

