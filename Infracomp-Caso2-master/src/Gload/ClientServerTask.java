package Gload;

import servidorCliente.Cliente;
import uniandes.gload.core.Task;

public class ClientServerTask extends Task {
	
	private Cliente cliente;
	
	public ClientServerTask(Cliente pCliente) {
		this.cliente=pCliente;
	}

	@Override
	public void fail() {
		// TODO Auto-generated method stub
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() {
		// TODO Auto-generated method stub
		System.out.println(Task.OK_MESSAGE);
	}

	@Override
	public void execute() {
		
		cliente.run();
		
	}

	
}
