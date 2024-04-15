package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
//import io.grpc.StatusRuntimeException;
//import javax.sound.midi.SysexMessage;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;

//import java.util.Scanner;

public class ClientService {

    private final ManagedChannel channel;
    private final TupleSpacesGrpc.TupleSpacesBlockingStub stub;

    public ClientService(String target) {

        // Cria um canal (channel) gRPC para o servidor
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        // Cria um stub para chamar os métodos remotos do serviço
        this.stub = TupleSpacesGrpc.newBlockingStub(channel);
        TupleSpacesGrpc.TupleSpacesBlockingStub stub = TupleSpacesGrpc.newBlockingStub(channel);
    }

    // Implemente aqui os métodos para cada operação remota do serviço
    //Method for operation put()
   public void put(String tuple){
    TupleSpacesCentralized.PutRequest request = TupleSpacesCentralized.PutRequest.newBuilder().setNewTuple(tuple).build();
    TupleSpacesCentralized.PutResponse response = stub.put(request);
   }

    // Método para realizar a operação take
    public String take(String tuple) {
        // Construa a solicitação para a operação take
        TupleSpacesCentralized.TakeRequest request = TupleSpacesCentralized.TakeRequest.newBuilder().setSearchPattern(tuple).build();
        // Faça a chamada remota para o servidor e obtenha a resposta
        TupleSpacesCentralized.TakeResponse response = stub.take(request);
        // Retorna o tuplo retirado (ou null se nenhum for encontrado)
        return response.getResult();
    }

    //Method for read operation
    public String read(String tuple) {
      
        TupleSpacesCentralized.ReadRequest request = TupleSpacesCentralized.ReadRequest.newBuilder().setSearchPattern(tuple).build();
        
        TupleSpacesCentralized.ReadResponse response = stub.read(request);
        
        return response.getResult();
      }
  
    public String getTuppleString(){
        getTupleSpacesStateResponse response = stub.getTupleSpacesState(getTupleSpacesStateRequest.getDefaultInstance());
        return response.getTupleList().toString();
    }

    // Método para fechar o canal gRPC quando o cliente for encerrado
    public void shutdown() {
        channel.shutdown();
    }

}