package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.nameServer.contract.NameServer;
import pt.tecnico.ulisboa.nameServer.contract.NameServerServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.HashMap;
import java.util.Map;

public class ClientMain {

    //static final int numServers = 3;
    
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
        if (args.length != 1) {
            debug(String.format("Argument(s) existing!"));
            debug(String.format("Usage: mvn exec:java <client ID>"));
            return;
        }

        String clientID = args[0];

        // target is the host and port for the nameserver
        String target = "localhost" + ":" + "5001";

        // number of servers connected to the name server
        final int numServers = 3;

        Map<String, String> targets = new HashMap<>();

        //Create channel for Name Server
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        
        NameServerServiceGrpc.NameServerServiceBlockingStub stub = NameServerServiceGrpc.newBlockingStub(channel);

        // Channel is the abstraction to connect to a service endpoint
		// Let us use plaintext communication because we do not have certificates

        for (int i = 0; i < numServers; i++) {
            target = null;
            switch(i){
                case 0: 
                    target = lookup(stub, "TupleSpace", "A");
                    break;
                case 1: 
                    target = lookup(stub, "TupleSpace", "B");
                    break;
                case 2: 
                    target = lookup(stub, "TupleSpace", "C");
                    break;
            }
            if (target != null) {
                targets.put(Character.toString((char)('A' + i)), target);
            }
        }

        ClientService clientService = new ClientService(numServers, targets);
        CommandProcessor parser = new CommandProcessor(clientService, clientID);

        channel.shutdown();
        //verify if the connection with the server is valid
        try{
            parser.parseInput();
        }catch (StatusRuntimeException e) {
            System.out.println("Could not reach server.");
        }
            
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
