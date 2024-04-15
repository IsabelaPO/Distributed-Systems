package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays; 
import java.util.List;

public class TupleSpacesServiceImpl extends TupleSpacesGrpc.TupleSpacesImplBase{
    
    private ServerState serverState = new ServerState();

    // Method to handle 'put' operation
    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver){
        String tuple = request.getNewTuple();
        serverState.put(tuple);
        PutResponse response = PutResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Method to handle 'take' operation
    @Override
    public void take(TupleSpacesCentralized.TakeRequest request, StreamObserver<TupleSpacesCentralized.TakeResponse> responseObserver){
        String tuple = request.getSearchPattern();
        String removesTuple = serverState.take(tuple);
        TupleSpacesCentralized.TakeResponse response = TupleSpacesCentralized.TakeResponse.newBuilder().setResult(removesTuple).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Method to handle 'read' operation
    @Override
    public void read(TupleSpacesCentralized.ReadRequest request, StreamObserver<TupleSpacesCentralized.ReadResponse> responseObserver){
        String tuple = request.getSearchPattern();
        String readTuple = serverState.read(tuple);
        TupleSpacesCentralized.ReadResponse response = TupleSpacesCentralized.ReadResponse.newBuilder().setResult(readTuple).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    // Method to getTupleSpacesState operation
    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request, StreamObserver<getTupleSpacesStateResponse> responseObserver){
        List<String> tupleState = serverState.getTupleSpacesState();
        if(tupleState.isEmpty()){
            tupleState = Arrays.asList("");
        }
        getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder().addAllTuple(tupleState).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

