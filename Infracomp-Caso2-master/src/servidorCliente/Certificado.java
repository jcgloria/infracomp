package servidorCliente;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;

@SuppressWarnings("deprecation")
public class Certificado {

	X509Certificate certificado;
	private PrivateKey privada;
	private PublicKey publica;
	
	//Constructor
	public Certificado() throws Exception
	{
		// TODO Auto-generated constructor stub
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		KeyPairGenerator keygen= KeyPairGenerator.getInstance("RSA");
		
		keygen.initialize(1024);
		
		KeyPair pair= keygen.generateKeyPair();
		setPrivada(pair.getPrivate());
		setPublica(pair.getPublic());
		
		BigInteger suma= BigInteger.valueOf(0);
		for (int i = 5; i < 5000; i++)
		{
			if(i%5==0)
			{
				suma=suma.add(BigInteger.valueOf(i));
			}
		}
		
		//numero serial del certificado
		BigInteger serialNumber=suma;
		
		X509V3CertificateGenerator certifGen= new X509V3CertificateGenerator();
		X500Principal dnName= new X500Principal("CN=Test Certificate");
		certifGen.setSerialNumber(serialNumber);
		certifGen.setIssuerDN(dnName);
		certifGen.setNotBefore(new Date(System.currentTimeMillis()-20000));
		certifGen.setNotAfter(new Date(System.currentTimeMillis()+20000));
		certifGen.setSubjectDN(dnName);
		certifGen.setPublicKey(pair.getPublic());
        certifGen.setSignatureAlgorithm("SHA256withRSA");
		certifGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		certifGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature|KeyUsage.keyEncipherment) );
		certifGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
		certifGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name,"test@test.test")));
		X509Certificate cert= certifGen.generateX509Certificate(pair.getPrivate(), "BC");
		
		certificado=cert;
		
	}
	
	public PrivateKey getPrivada() 
	{
		return privada;
	}
	
	public void setPrivada(PrivateKey privada) 
	{
		this.privada = privada;
	}
	
	public PublicKey getPublica() 
	{
		return publica;
	}
	
	public void setPublica(PublicKey publica) 
	{
		this.publica = publica;
	}
	
}
