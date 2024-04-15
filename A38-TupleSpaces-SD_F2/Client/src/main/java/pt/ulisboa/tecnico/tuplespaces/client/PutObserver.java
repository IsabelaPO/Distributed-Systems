package pt.ulisboa.tecnico.tuplespaces.client;


import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;


public class PutObserver implements StreamObserver<PutResponse>{
    ResponseCollector collector;

    public PutObserver (ResponseCollector c){
        collector = c;
    }

    @Override
    public void onNext(PutResponse r) {
        collector.addPutResponse();
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


    
