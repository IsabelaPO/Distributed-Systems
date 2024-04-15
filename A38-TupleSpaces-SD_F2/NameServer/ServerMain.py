#Class ServerEntry defined by ipAddress and qualifier
class ServerEntry:
    def __init__(self, ipAddress, qualifier):
        self.ipAddress =  ipAddress
        self.qualifier = qualifier

#Class ServiceEntry defined by name and list of server entries 
class ServiceEntry:
    def __init__(self, name, ipAddress, qualifier):
        self.name = name
        self.list = [ServerEntry(ipAddress, qualifier)]

    #Method for registering a new server entry. It iterates through server entry list to find 
    #if entry is already present and finally append to the list if not. 
    def register(self, ipAddress, qualifier):
        for entry in self.list: 
            if entry.ipAddress == ipAddress:
                print(f"{ipAddress} is already registered.")
                raise ValueError()
            if entry.qualifier == qualifier:
                print(f"{qualifier} is already registered.")
                raise ValueError()
        self.list.append(ServerEntry(ipAddress, qualifier))

    #Method for looking up a server in server entry list. Iterates through list to 
    #append service with matching qualifier to empty servers list and returns it.  
    def lookup(self, qualifier):
        servers = []
        for service in self.list: 
            if service.qualifier == qualifier or qualifier == "":
                servers.append(service.ipAddress)
        return servers

    #Method for deleting a server in server entry list. Iterates through list to find
    #entry with the ipAddress provided and removes it from list. 
    def delete(self, ipAddress):
        print(self.list)
        found = 0
        for entry in self.list:
            if entry.ipAddress == ipAddress:
                found +=1 
                self.list.remove(entry)
        print(self.list)
        if found == 0: 
            raise Exception()

#Class NamingServer defined by a server map. 
class NamingServer:
    def __init__(self):
        self.server_map = {}
    
    #Method for registering a service in map by the name of the server its associated with.
    def register(self, name, ipAddress, qualifier):
        if ':' not in ipAddress:          
            raise Exception()
        if name in self.server_map:
            self.server_map[name].register(ipAddress,qualifier)
        else:
            entry = ServiceEntry(name,ipAddress,qualifier)
            self.server_map.update({name:entry})

    #Method for looking up a service in map that matches in qualifier field in its server entry list
    def lookup(self, name, qualifier):
        if name in self.server_map:
            return self.server_map[name].lookup(qualifier)
        return []

    #Method for deleting a service in map that matches in ipAddress field in its server entry list
    def delete(self, name, ipAddress):
        if name in self.server_map:
            self.server_map[name].delete(ipAddress)
        else:
            raise Exception()