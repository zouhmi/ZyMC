package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class LoginKeyC2SCodec implements PacketCodec<LoginKeyC2S> {

    @Override
    public void encode(LoginKeyC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeBytes(buf, packet.encryptedSecretKey());
        ByteBufVarInts.writeBytes(buf, packet.nonce());
    }

    @Override
    public LoginKeyC2S decode(ByteBuf buf) {
        byte[] encryptedSecretKey = ByteBufVarInts.readBytes(buf);
        byte[] nonce = ByteBufVarInts.readBytes(buf);
        return new LoginKeyC2S(encryptedSecretKey, nonce);
    }
}
