import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.security.*;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jcajce.provider.keystore.bc.BcKeyStoreSpi.BouncyCastleStore;
import org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class ThreadCliente extends Thread {


	private Socket socketComServidor;
	private BufferedReader inServidor;
	private Cliente cliente;
	

	public ThreadCliente( Socket nSocketComServidor, Cliente pCliente )
	{
		socketComServidor = nSocketComServidor;
		cliente = pCliente;

	}

	public void run(){
		try {
			inServidor = new BufferedReader( new InputStreamReader( socketComServidor.getInputStream( ) ) );
			boolean servidorCaido = false;
			while(!servidorCaido){
				String mensaje1 = inServidor.readLine( );
				if( mensaje1 == null )
				{
					servidorCaido = true;
				}
				else{
					//MANDAR CERTIFICADO
					cliente.mandarMensaje("CERTCLNT");
					KeyPair pair = hacerKeyPair();
					
					if(pair != null){
						X509V3CertificateGenerator  certGen = new X509V3CertificateGenerator();
						certGen.setPublicKey(pair.getPublic());
						
						try {
							byte[] flujoDeBytes = (certGen.generate(pair.getPrivate()).getEncoded());
							
							cliente.mandarBytes(flujoDeBytes);
							inServidor = new BufferedReader( new InputStreamReader( socketComServidor.getInputStream( ) ) );
							String mensaje2 = inServidor.readLine();
							if(mensaje2.equals("OK")){
								inServidor = new BufferedReader( new InputStreamReader( socketComServidor.getInputStream( ) ) );
								String mensaje3 = inServidor.readLine();
								if(mensaje3.equals("CERTSRV")){
									CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
									InputStream in = new ByteArrayInputStream(cliente.recibirCertificado());
									X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
			}
		}


		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	public KeyPair hacerKeyPair(){
		try {
			KeyPairGenerator pair = java.security.KeyPairGenerator.getInstance("RSA");
			pair.initialize(1024);
			return pair.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

}
