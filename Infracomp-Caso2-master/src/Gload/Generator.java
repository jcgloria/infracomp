package Gload;

import servidorCliente.Cliente;
import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class Generator {
 
	private LoadGenerator generator;
	
	public Generator() {
		Task work = createTask();
		int numberOfTasks = 10;
		int gapBetweenTasks=1000;
		generator=new LoadGenerator("Client - Server Load Test", numberOfTasks, work, gapBetweenTasks);
		generator.generate();
	}

	private Task createTask() {
		
		return new ClientServerTask(new Cliente());
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Generator gen = new Generator();
	}
}
