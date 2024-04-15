package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.server.domain.TupleSpaceState;

import java.util.ArrayList;
import java.util.Arrays; 
import java.util.List;

public class TupleSpacesServiceImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase{
    
    private ServerState serverState = new ServerState();
    int lastClientId;

    // Method to handle 'put' operation
    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver){
        String tuple = request.getNewTuple();
        serverState.put(tuple);
        PutResponse response = PutResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    // Method to handle 'takePhase1' operation
    @Override
    public void takePhase1(TakePhase1Request request, StreamObserver<TakePhase1Response> responseObserver){
        //search for the tuple in question
        String tuple = request.getSearchPattern();
        int clientID = request.getClientId();
        //get list of matching tuples in server
        ArrayList<TupleSpaceState> matchingTuples = serverState.takePhase1(tuple, clientID);
        List<String> response_lst = new ArrayList<>();

        for(TupleSpaceState tuple_class : matchingTuples){
            response_lst.add(tuple_class.getTuple().get(0));
        }
        TupleSpacesReplicaXuLiskov.TakePhase1Response response = TupleSpacesReplicaXuLiskov.TakePhase1Response.newBuilder().addAllReservedTuples(response_lst).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Method to handle 'takePhase1Release' operation
    @Override
    public void takePhase1Release(TakePhase1ReleaseRequest request, StreamObserver<TakePhase1ReleaseResponse> responseObserver){
        int clientId = request.getClientId();
        serverState.takeRelease(clientId);
        TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse response = TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Method to handle 'takePhase2' operation
    @Override
    public void takePhase2(TakePhase2Request request, StreamObserver<TakePhase2Response> responseObserver){
        int clientId = request.getClientId();
        String tuple = request.getTuple();
        serverState.takePhase2(tuple,clientId);
        TupleSpacesReplicaXuLiskov.TakePhase2Response response = TakePhase2Response.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Method to handle 'read' operation
    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver){
        String tuple = request.getSearchPattern();
        String readTuple = serverState.read(tuple);
        ReadResponse response = ReadResponse.newBuilder().setResult(readTuple).build();
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
        TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse response = TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse.newBuilder().addAllTuple(tupleState).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

