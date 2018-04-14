import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.TreeSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.x509.X509V3CertificateGenerator;



public class Cliente {

	private int puerto;
	private Socket socket;
	private OutputStream outClienteStream;
	private PrintWriter outCliente;
	private BufferedReader inCliente;
	private X509Certificate certificadoServidor;
	//private ThreadEscucharServidor escucharServidor;

	public Cliente(int pPuerto) {
		puerto = pPuerto;
	}

	public boolean conectarServidor(String algS, String algA, String algHMAC) throws UnknownHostException, IOException{

		socket = new Socket( InetAddress.getByName( "localhost" ), puerto );
		outClienteStream = socket.getOutputStream( );
		outCliente=new PrintWriter(outClienteStream, true);
		outCliente.println( "HOLA" );
		outCliente.flush( );
		inCliente = new BufferedReader( new InputStreamReader( socket.getInputStream( ) ) );
		String respuesta1 = inCliente.readLine( );
		if( !respuesta1.equals( "INICIO" ) ){
			System.out.println("No es inicio");
			socket.close();
			return false;
		}
		else
		{
			outCliente = new PrintWriter(outClienteStream, true);
			outCliente.println( "ALGORITMOS:"+algS + ":" + algA + ":" + algHMAC );
			outCliente.flush();
			inCliente = new BufferedReader( new InputStreamReader( socket.getInputStream( ) ) );
			String respuesta2 = inCliente.readLine();
			if(respuesta2.equals("ESTADO:ERROR")){
				System.out.println("Se toteo");
				return false;
			}
			else if (respuesta2.equals("ESTADO:OK")){
				try {
					//MANDAR CERTIFICADO
					mandarMensaje("CERTCLNT");
					KeyPair pair = hacerKeyPair();

					X509V3CertificateGenerator  certGen = new X509V3CertificateGenerator();
					certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
					certGen.setIssuerDN(new X500Principal("CN=Test Certificate"));
					certGen.setNotBefore(new Date(System.currentTimeMillis() - 50000));
					certGen.setNotAfter(new Date(System.currentTimeMillis() + 50000));
					certGen.setSubjectDN(new X500Principal("CN=Test Certificate"));
					certGen.setPublicKey(pair.getPublic());
					certGen.setSignatureAlgorithm("SHA224withRSA");
					certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
					certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
					certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
					certGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name, "test@test.test")));



					try {
						byte[] flujoDeBytes = (certGen.generate(pair.getPrivate()).getEncoded());
						mandarBytes(flujoDeBytes);
						inCliente = new BufferedReader( new InputStreamReader( socket.getInputStream( ) ) );
						String mensaje2 = inCliente.readLine();
						if(mensaje2.equals("ESTADO:OK")){
							inCliente = new BufferedReader( new InputStreamReader( socket.getInputStream( ) ) );
							String mensaje3 = inCliente.readLine();
							if(mensaje3.equals("CERTSRV")){
								CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
								InputStream in = new ByteArrayInputStream(recibirCertificado());
								certificadoServidor = (X509Certificate) certFactory.generateCertificate(in);
								if(certificadoServidor.getPublicKey() == null){
									mandarMensaje("ESTADO:ERROR");
								}else{
									mandarMensaje("ESTADO:OK");
									inCliente = new BufferedReader( new InputStreamReader( socket.getInputStream( ) ) );
									String mensaje4[] = inCliente.readLine().split(":");
									if(mensaje4[0].equals("INICIO")){
										System.out.println(mensaje4[1]);
										System.out.println(pair.getPrivate().toString());
										//String msg=desencriptar(pair.getPrivate(), convertHexToString(mensaje4[1]));
										//System.out.println("MENSAJE DECRIPTADO :"+msg);
										mandarMensaje("ACT1");
										mandarMensaje("ACT2");

									}
									inCliente = new BufferedReader( new InputStreamReader( socket.getInputStream( ) ) );
									String mensaje5 = inCliente.readLine();
									if(mensaje5.equals("OK")) System.out.println("ESTA MONDA SIRVE");

								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}




				}


				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return true;
			}
			else{
				System.out.println("se toteo null");
				return false;
			}
		}
	}

	public void mandarMensaje(String a) {
		outCliente = new PrintWriter(outClienteStream, true);
		outCliente.println(a);
		outCliente.flush();
	}
	public void mandarBytes(byte[] bytes){
		try {
			socket.getOutputStream().write(bytes);
			socket.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] recibirCertificado(){
		try {
			byte[] array = new byte[999999999];
			socket.getInputStream().read(array);
			return array;
		} catch (IOException e) {
			return null;
		}
	}
	public static void main(String[] args) {
		Cliente c = new Cliente(8080);
		try{
			c.conectarServidor("BLOWFISH", "RSA", "HMACMD5");
		}catch(Exception e) {
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

	public String generarPosicion(){
		String a;
		Double decimal = Math.random()*99;
		NumberFormat formatter = new DecimalFormat("#00.0000");
		Integer entero = (int) Math.random()*99;
		a=entero+" "+formatter.format(decimal);

		return a;
	}

	public static X509Certificate generateV3Certificate(KeyPair pair)
			throws InvalidKeyException, NoSuchProviderException, SignatureException
	{
		// generate the certificate
		X509V3CertificateGenerator  certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(new X500Principal("CN=Test Certificate"));
		certGen.setNotBefore(new Date(System.currentTimeMillis() - 50000));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + 50000));
		certGen.setSubjectDN(new X500Principal("CN=Test Certificate"));
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));

		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

		certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));

		certGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name, "test@test.test")));

		return certGen.generateX509Certificate(pair.getPrivate(), "BC");
	}

	public String convertHexToString(String hex){

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		for( int i=0; i<hex.length()-1; i+=2 ){

			//grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			//convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char)decimal);

			temp.append(decimal);
		}

		return sb.toString();
	}


}


