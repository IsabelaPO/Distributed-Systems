package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.tecnico.ulisboa.nameServer.contract.NameServer;
import pt.tecnico.ulisboa.nameServer.contract.NameServerServiceGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;

import java.util.List;
import java.util.Scanner;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CommandProcessor {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String SET_DELAY = "setdelay";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";

    private final ClientService clientService;
    private final String qualifier;

    public CommandProcessor(ClientService clientService, String qualifier) {
        this.clientService = clientService;
        this.qualifier = qualifier;
    }

    // Method to parse user input and execute corresponding commands
    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        // Continue parsing input until the exit command is given
        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);
             switch (split[0]) {
                case PUT:
                    this.put(split);
                    break;

                case READ:
                    this.read(split);
                    break;

                case TAKE:
                    this.take(split);
                    break;

                case GET_TUPLE_SPACES_STATE:
                    this.getTupleSpacesState(split);
                    break;

                case SLEEP:
                    this.sleep(split);
                    break;

                case SET_DELAY:
                    this.setdelay(split);
                    break;

                case EXIT:
                    exit = true;
                    break;

                default:
                    this.printUsage();
                    break;
             }
        }
    }

    /*Method to execute the put operation. Will receive a string and gets the tuple from it.
    It then uses the put function to add tuple and prints a confirmation message.*/
    private void put(String[] split){

        // check if input is valid
        if (!this.inputIsValid(split)) {
            return;
        }
        
        // get the tuple
        String tuple = split[1];
        clientService.put(tuple);

        // put the tuple
        System.out.println("OK\n");
    }

    /*Method to execute the read operation. Will receive a string and gets the tuple from it. 
    Uses read funtion to read tuple and will print a confirmarion message with the read tuple.*/
    private void read(String[] split){
        // check if input is valid
        if (!this.inputIsValid(split)) {
            return;
        }
        
        // get the tuple
        String tuple = split[1];

        // read the tuple
        String readTuple = clientService.read(tuple);

        System.out.println("OK");
        System.out.println(readTuple + "\n");
    }

    /*Method to execute the take operation. Will receive a string and gets tuple from it. 
    Uses take function to take tuple and will print a confirmation message with taken tuple.*/
    private void take(String[] split){
        // check if input is valid
        if (!this.inputIsValid(split)) {
            return;
        }
        
        // get the tuple
        String tuple = split[1];

        // take operation
        String takenTuple = clientService.take(tuple);

        System.out.println("OK");
        System.out.println(takenTuple + "\n");
    }
    
    /*Method to execute the getTupleSpacesState operation. If qualifier matches with the one provided, a confirmation message will be
    printed with tuples. If not, a channel is created for the name server and pricess is repeated.*/
    private void getTupleSpacesState(String[] split){

        if (split.length != 2){
            this.printUsage();
            return;
        }

        if(this.qualifier.equals(split[1])) {
            // get the tuple spaces state
            String tuple = clientService.getTuppleString();
            System.out.println("OK");
            System.out.println(tuple+ "\n");
        } else{
            //create new name server channel
            // target is the host and port for the nameserver
            final String target = "localhost" + ":" + "5001";

            //Create channel for Name Server
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        
            NameServerServiceGrpc.NameServerServiceBlockingStub stub = NameServerServiceGrpc.newBlockingStub(channel);

            String string_server = lookup(stub, "TupleSpace", split[1]);
            channel.shutdown();

            if(string_server.equals("")){
                return;
            }

            ClientService clientService = new ClientService(string_server);
            String tuple = clientService.getTuppleString();
            System.out.println("OK");
            System.out.println(tuple+ "\n");
            clientService.shutdown();
        }
    }

    /*Method to perform lookup operation. Receives stub, name and qualifier. If responce list is empty,
    return empty string. If server found, return responce to request.*/
    public static String lookup(NameServerServiceGrpc.NameServerServiceBlockingStub stub, String name, String qualifier){
        NameServer.LookupRequest request = NameServer.LookupRequest.newBuilder().setName(name).setQualifier(qualifier).build();
        NameServer.LookupResponse response = stub.lookup(request);
        if(response.getServersList().isEmpty()){
            System.out.println("No server found for this lookup.\n");
            return "";

        }else{
            String target ="";
            for (String entry: response.getServersList()){
                target = entry;
            }
            return target;  

        }
    }

     //Exception class
    public class ErrorMessage extends Exception {
        public ErrorMessage(String message) {
            super(message);
        }
    }

    // Method to execute the sleep command. Receives a string with an interger that represents the seconds client is blocked
    private void sleep(String[] split) {
      if (split.length != 2){
        this.printUsage();
        return;
      }
      Integer time;

      // checks if input String can be parsed as an Integer
      try {
         time = Integer.parseInt(split[1]);
      } catch (NumberFormatException e) {
        this.printUsage();
        return;
      }

      try {
        Thread.sleep(time*1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    // Method to execute the setdelay command. Receives a string with the server id and an interger. 
    private void setdelay(String[] split) {
      if (split.length != 3){
        this.printUsage();
        return;
      }
      String qualifier = split[1];
      Integer time;

      // checks if input String can be parsed as an Integer
      try {
        time = Integer.parseInt(split[2]);
      } catch (NumberFormatException e) {
        this.printUsage();
        return;
      }

      // register delay <time> for when calling server <qualifier>
      System.out.println("TODO: implement setdelay command (only needed in phases 2+3)");
    }

    // Method to print command usage
    private void printUsage() {
        System.out.println("Usage:\n" +
                "- put <element[,more_elements]>\n" +
                "- read <element[,more_elements]>\n" +
                "- take <element[,more_elements]>\n" +
                "- getTupleSpacesState <server>\n" +
                "- sleep <integer>\n" +
                "- setdelay <server> <integer>\n" +
                "- exit\n");
    }

    //Method that checks if input is valid. 
    private boolean inputIsValid(String[] input){
        
        if (input.length < 2 // fewer than 2 elements
            || 
            !input[1].substring(0,1).equals(BGN_TUPLE) // if the second element does not start with '<'
            || 
            !input[1].endsWith(END_TUPLE) // does not end with '>'
            || 
            input.length > 2 // more than 2 elements
            ) {
            this.printUsage();
            return false;
        }
        else {
            return true;
        }
    }
}