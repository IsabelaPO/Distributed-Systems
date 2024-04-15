package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;


public class TakeObserver implements StreamObserver<TakePhase1Response>{
    ResponseCollector collector;

    public TakeObserver (ResponseCollector c){
        collector = c;
    }

    @Override
    public void onNext(TakePhase1Response r) {
        collector.addTakeResponse(new ArrayList<>(r.getReservedTuplesList()));
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


    
