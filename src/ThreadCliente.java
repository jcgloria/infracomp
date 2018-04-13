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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.security.*;
import javax.security.auth.x500.X500Principal;

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
									if(cert.getPublicKey() == null){
										cliente.mandarMensaje("ERROR");
									}else{
										cliente.mandarMensaje("OK");
										inServidor = new BufferedReader( new InputStreamReader( socketComServidor.getInputStream( ) ) );
										String mensaje4[] = inServidor.readLine().split(":");
										if(mensaje4[0].equals("INICIO")){
											cliente.mandarMensaje("ACT1");
											cliente.mandarMensaje("ACT2");
										
										}
										inServidor = new BufferedReader( new InputStreamReader( socketComServidor.getInputStream( ) ) );
										String mensaje5 = inServidor.readLine();
										if(mensaje5.equals("OK")) System.out.println("ESTA MONDA SIRVE");
										
									}
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
	
	public String generarPosicion(){
		String a;
		Double decimal = Math.random()*99;
		NumberFormat formatter = new DecimalFormat("#00.0000");
		Integer entero = (int) Math.random()*99;
		a=entero+" "+formatter.format(decimal);

		return a;
	}

}
