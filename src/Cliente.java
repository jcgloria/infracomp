import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;



public class Cliente {

	private int puerto;
	private Socket socket;
	private OutputStreamWriter outCliente;
	private BufferedReader inCliente;
	//private ThreadEscucharServidor escucharServidor;

	public Cliente(int pPuerto) {
		puerto = pPuerto;
	}

	public boolean conectarServidor(String algS, String algA, String algHMAC) throws UnknownHostException, IOException{

		socket = new Socket( InetAddress.getByName( "localhost" ), puerto );
		outCliente = new OutputStreamWriter( socket.getOutputStream( ) );
		outCliente.write( "HOLA" );
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
			outCliente = new OutputStreamWriter( socket.getOutputStream( ) );
			outCliente.write( algS + ":" + algA + ":" + algHMAC );
			outCliente.flush();
			inCliente = new BufferedReader( new InputStreamReader( socket.getInputStream( ) ) );
			String respuesta2 = inCliente.readLine();
			if(respuesta2.equals("ERROR")){
				System.out.println("Se toteo");
				return false;
			}
			else if (respuesta2.equals("OK")){
				//se empieza thread
				return true;
			}
			else{
				System.out.println("se toteo null");
				return false;
			}
		}
	}

	public void mandarMensaje(String a) {
		try {
			outCliente.write(a);
			outCliente.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

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

}
