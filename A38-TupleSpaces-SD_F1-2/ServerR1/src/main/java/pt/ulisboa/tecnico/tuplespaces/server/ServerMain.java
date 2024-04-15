package pt.ulisboa.tecnico.tuplespaces.server;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.ulisboa.nameServer.contract.NameServer;
import pt.tecnico.ulisboa.nameServer.contract.NameServerServiceGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.grpc.TupleSpacesServiceImpl;
import io.grpc.StatusRuntimeException;
import java.lang.Runtime;


public class ServerMain {

	/** Set flag to true to print debug messages. 
	 * The flag can be set using the -Ddebug command line option. */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/** Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}
    public static void main(String[] args) throws IOException, InterruptedException{
		debug(String.format(ServerMain.class.getSimpleName()));

		 // receive and print arguments
		debug(String.format("Received %d arguments%n", args.length));
		for (int i = 0; i < args.length; i++) {
			debug(String.format("arg[%d] = %s", i, args[i]));
		}

			// check arguments
		if (args.length != 2) {
			debug(String.format("Argument(s) missing!"));
            debug(String.format("Usage: mvn exec:java -Dexec.args=<host> <port>"));
			return;
		}

		//For NameServer
		String name = "TupleSpace";
		String port_str = args[0];
		String qualifier = args[1];

		//For Server

		final BindableService impl = new TupleSpacesServiceImpl();

		final int port = Integer.parseInt(port_str);

		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(port).addService(impl).build();

		// target is the host and port for the nameserver
        final String target = "localhost" + ":" + "5001";

        //Create channel for Name Server
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    
        NameServerServiceGrpc.NameServerServiceBlockingStub stub = NameServerServiceGrpc.newBlockingStub(channel);
		
		String address = "localhost:" + port_str;
		register(stub, name, address, qualifier);
		// Start the server
		server.start();

		// Server threads are running in the background.
		debug(String.format("Server started"));
		
		 // Create a shutdown hook to delete the server entry when the program is terminated
		 Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				delete(stub, name, address);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	
		
		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
    }

	/* Method for register funtion. Receives attributes stub, name, address and qualifier. Registers the request for a name server with
	the attributes name, address and qualifier. Finally registers it in the NameServer and returns if successful or not */
	public static void register(NameServerServiceGrpc.NameServerServiceBlockingStub stub, String name, String address, String qualifier){
		NameServer.RegisterRequest request = NameServer.RegisterRequest.newBuilder().setName(name).setIpAddress(address).setQualifier(qualifier).build();
		try{
			// Sending the register request and receiving the response
			NameServer.RegisterResponse response = stub.register(request);
			debug(String.format("Server registered in Nameserver"));
		} catch(StatusRuntimeException e){
			debug(String.format("RPC failed: "+e.getStatus().getDescription()));
			System.exit(1); // Exiting the program with an error status

		}
	}

	/*Method for delete function. Receives attributes stub, name and address. Registers the request for a name server with the attributes
	 name and address. Finally deletes it in the NameServer and returns if successful or not*/
	public static void delete(NameServerServiceGrpc.NameServerServiceBlockingStub stub, String name, String address){
		NameServer.DeleteRequest request = NameServer.DeleteRequest.newBuilder().setName(name).setIpAddress(address).build();
		try{
			// Sending the delete request and receiving the response
			NameServer.DeleteResponse response = stub.delete(request);
			debug(String.format("Server deleted from Nameserver"));
		} catch(StatusRuntimeException e){
			debug(String.format("RPC failed: "+e.getStatus().getDescription()));
			System.exit(1); // Exiting the program with an error status
		}
	}

}

