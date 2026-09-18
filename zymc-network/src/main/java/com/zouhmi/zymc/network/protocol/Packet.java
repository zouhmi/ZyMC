package com.zouhmi.zymc.network.protocol;

public interface Packet<L> {
    void handle(L listener);
}
