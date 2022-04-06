package com.etherblood.luna.application.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.client.ToolsClient;
import com.destrostudios.gametools.network.client.modules.jwt.JwtClientModule;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.client.ClientGameModule;
import com.etherblood.luna.network.client.timestamp.ClientTimestampModule;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String... args) throws IOException {
        try {
            startApp(args);
        } catch (Throwable t) {
            showErrorDialog(t);
            throw t;
        }
    }

    static void showErrorDialog(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        JOptionPane.showMessageDialog(null, sw.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void startApp(String[] args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        if (args.length == 0) {
            throw new IllegalArgumentException("First argument must be a jwt.");
        }
        new ApplicationClient(remoteProxy("destrostudios.com", args[0])).start();
    }

    static GameProxy remoteProxy(String host, String jwt) throws IOException {
//        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs
        Client client = new Client(10_000, 10_000);
        JwtClientModule jwtModule = new JwtClientModule(client);
        ClientTimestampModule timestampModule = new ClientTimestampModule(client, 40, 250);
        ClientGameModule gameModule = new ClientGameModule(client);
        ToolsClient toolsClient = new ToolsClient(client, jwtModule, timestampModule, gameModule);

        client.start();
        client.connect(10_000, host, NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        jwtModule.login(jwt);
        return new RemoteGameProxy(timestampModule, gameModule, new NoValidateJwtService().decode(jwt).user);
    }

    static String getTestJwt(long playerId) {
        Map<String, ?> user = Map.of("id", playerId, "login", "player " + playerId);
        return JWT.create()
                .withIssuedAt(new Date())
                .withClaim("user", user)
                .sign(Algorithm.none());
    }
}
