package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.core.buffer.Strings;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public final class LoginSuccessS2CCodec implements PacketCodec<LoginSuccessS2C> {

    @Override
    public void encode(LoginSuccessS2C packet, ByteBuf buf) {
        try {
            Strings.writeString(buf::writeByte, packet.name());
            ByteBuffer uuidBytes = ByteBuffer.allocate(16);
            uuidBytes.putLong(packet.profileId().getMostSignificantBits());
            uuidBytes.putLong(packet.profileId().getLeastSignificantBits());
            buf.writeBytes(uuidBytes.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginSuccessS2C decode(ByteBuf buf) {
        try {
            String name = Strings.readString(buf::readByte);
            long mostSig = buf.readLong();
            long leastSig = buf.readLong();
            UUID profileId = new UUID(mostSig, leastSig);
            return new LoginSuccessS2C(name, profileId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
