import sys
import grpc
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from ServerMain import NamingServer

class NamingServerServiceImpl(pb2_grpc.NameServerServiceServicer):
    def __init__(self, *args, **kwargs):
        self.server = NamingServer()

    #Method to register the server with the NamingServer. If not successful returns error message. 
    def register(self, request, context):
        name = request.name
        ip = request.ipAddress
        qualifier = request.qualifier

        try:
            self.server.register(name, ip, qualifier)
            print("Sending response")
            return pb2.RegisterResponse()
        except:
            context.set_details("Not possible to register the server")
            context.set_code(grpc.StatusCode.INTERNAL)
            return pb2.RegisterResponse()

    #Method to lookup servers with matching name and qualifier provided.
    def lookup(self, request, context):   
        name = request.name
        qualifier = request.qualifier
        response = self.server.lookup(name, qualifier)
        r = pb2.LookupResponse()
        r.servers.extend(response)
        return r 

    #Method to delete server with matching name and qualifier provided. If not successful return error message. 
    def delete(self, request, context):
        name = request.name
        ipAddress = request.ipAddress
        try:
            self.server.delete(name, ipAddress)
            return pb2.DeleteResponse()
        except:
            context.set_details("Not possible to remove the server")
            context.set_code(grpc.StatusCode.INTERNAL)
            return pb2.RegisterResponse()