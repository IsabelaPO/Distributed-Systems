package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.ulisboa.nameServer.contract.NameServer;
import pt.tecnico.ulisboa.nameServer.contract.NameServerServiceGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;

public class ClientMain {
    /** Set flag to true to print debug messages. 
	 * The flag can be set using the -Ddebug command line option. */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/** Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

    public static void main(String[] args) {
        // check arguments
        if (args.length != 2) {
            debug(String.format("Argument(s) missing!"));
            debug(String.format("Usage: mvn exec:java -Dexec.args=<host> <port>"));
            return;
        }

        // target is the host and port for the nameserver
        final String target = "localhost" + ":" + "5001";

        //Create channel for Name Server
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    
        NameServerServiceGrpc.NameServerServiceBlockingStub stub = NameServerServiceGrpc.newBlockingStub(channel);

        String string_server = lookup(stub, args[0], args[1]);

        channel.shutdown();

        if(string_server.equals("")){
            return;
        }
    
        ClientService clientService = new ClientService(string_server);
        CommandProcessor parser = new CommandProcessor(clientService, args[1]);
        parser.parseInput();
            
        // Shutdown the channel when done
        clientService.shutdown();
    }
    
    /*Method to perform a lookup operation on the Name Server with attributes stub, name and qualifier. If server list empty,
    return empty string. If server found, return responce to request.*/
    public static String lookup(NameServerServiceGrpc.NameServerServiceBlockingStub stub, String name, String qualifier){
        NameServer.LookupRequest request = NameServer.LookupRequest.newBuilder().setName(name).setQualifier(qualifier).build();
        NameServer.LookupResponse response = stub.lookup(request);
        if(response.getServersList().isEmpty()){
            debug(String.format("No server found for this lookup.\n"));
            return "";

        }else{
            String target ="";
            for (String entry: response.getServersList()){
                target = entry;
            }
            return target;  

        }
    }
}
