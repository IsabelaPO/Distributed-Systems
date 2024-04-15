# TupleSpaces

Distributed Systems Project 2024
  
**Group A38**

**Difficulty level: I am Death incarnate!** 


### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members


| Number | Name              | User                             | Email                                     |
|--------|-------------------|----------------------------------|-------------------------------------------|
| 102695 | Leonor Fortes     | <https://github.com/leonorf03>   | <mailto:leonor.fortes@tecnico.ulisboa.pt>         |
| 102703 | Isabela Pereira   | <https://github.com/IsabelaPO>     | <mailto:isabela.p.de.ornelas@tecnico.ulisboa.pt> |
| 102823 | Beatriz Paulo     | <https://github.com/beatrizp03>  | <mailto:beatriz.paulo@tecnico.ulisboa.pt> |

## Getting Started

The overall system is made up of several modules. The different types of servers are located in _ServerX_ (where X denotes stage 1, 2 or 3). 
The clients is in _Client_.
The definition of messages and services is in _Contract_. The name server
is in _NameServer_.

See the [Project Statement](https://github.com/tecnico-distsys/TupleSpaces) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

# Installation

## Package Instalation and Setup

You need to have installed version Python 3.5+

To confirm that you have it installed and which version it is, run in the terminal:

```s
python --version
```

### Create virtual environment in base directory

**Windows**
```s
python -m venv .venv
.venv\Scripts\activate
python -m pip install grpcio
python -m pip install grpcio-tools
deactivate
```

**Linux**
```s
python -m venv .venv
source .venv/bin/activate
python -m pip install grpcio
python -m pip install grpcio-tools
deactivate
```
(Use phyton3 if needed)

## Instructions for using Maven

**Contract**:

Make sure you have created virtual environment in base directory.

To compile and install all modules:
```s
mvn clean install

mvn exec:exec
```
**Server**

Make sure that you installed the contract module first.

To compile and run the server:
```s
mvn compile exec:java -Dexec.args="2001 A"
```

**Client**

Make sure that you installed the contract module first.

To compile and run the client:
```s
mvn compile exec:java -Dexec.args="TupleSpace A"
```

# Commands

**Put Function:**
```s
> put <field1,field2,field3>
```

**Read Function:**
```s
> read <field1,field2,field3>
or
> read <field1,field2,[^,]+>
```

**Take Function:**
```s
> take <field1,field2,field3>
or
> take <field1,field2,[^,]+>
```

**getTupleSpacesState Function:**
```s
> getTupleSpacesState <qualifier>
```

**Sleep Function**
```s
> sleep seconds
```

**Exit Function:**
```s
> exit
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
