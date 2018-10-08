/**
 * Class for Py4J bridge.
 * Run the main class here to start Py4J gateway server.
 *
 * SemSim can be accessed in Python by:
 * from py4j.java_gateway import JavaGateway
 * gateway = JavaGateway()
 * semsim = gateway.jvm.semsim
 */


package semsim;

import py4j.GatewayServer;

public class Py4J {

    public static void main(String[] args) {
        Py4J app = new Py4J();
        // app is now the gateway.entry_point
        GatewayServer server = new GatewayServer(app);
        server.start();
        System.out.println("Py4J server started.");
    }
}