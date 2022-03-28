package com.etherblood.luna.network.api;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;

public abstract class NetworkModule extends Listener {

    public abstract void initialize(Kryo kryo);


    public static void addModule(EndPoint endPoint, NetworkModule module) {
        module.initialize(endPoint.getKryo());
        endPoint.addListener(module);
    }
}