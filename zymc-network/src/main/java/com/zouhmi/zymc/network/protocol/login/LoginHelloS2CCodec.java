package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.core.buffer.Strings;
import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public final class LoginHelloS2CCodec implements PacketCodec<LoginHelloS2C> {

    @Override
    public void encode(LoginHelloS2C packet, ByteBuf buf) {
        try {
            Strings.writeString(buf::writeByte, packet.serverId());
            ByteBufVarInts.writeBytes(buf, packet.publicKeyDer());
            ByteBufVarInts.writeBytes(buf, packet.nonce());
            ByteBufVarInts.writeBoolean(buf, packet.needsAuthentication());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginHelloS2C decode(ByteBuf buf) {
        try {
            String serverId = Strings.readString(buf::readByte);
            byte[] publicKeyDer = ByteBufVarInts.readBytes(buf);
            byte[] nonce = ByteBufVarInts.readBytes(buf);
            boolean needsAuthentication = ByteBufVarInts.readBoolean(buf);
            return new LoginHelloS2C(serverId, publicKeyDer, nonce, needsAuthentication);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
