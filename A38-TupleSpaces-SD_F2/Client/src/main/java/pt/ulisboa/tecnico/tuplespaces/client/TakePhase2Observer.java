package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;


public class TakePhase2Observer implements StreamObserver<TakePhase2Response>{
    ResponseCollector collector;

    public TakePhase2Observer (ResponseCollector c){
        collector = c;
    }

    @Override
    public void onNext(TakePhase2Response r) {
        collector.addTakePhase2Response();
        //System.out.println("Received response" + r);
    }
    
    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        //System.out.println("Request completed");
    }
}