package com.avast.continuity.javaapi;

import com.avast.continuity.IdentityThreadNamer$;
import com.avast.continuity.Netty$;
import com.avast.continuity.ThreadNamer;
import io.netty.channel.EventLoopGroup;

public final class Netty {

    private static final Netty$ NETTY = Netty$.MODULE$;

    private Netty() {
    }

    public static EventLoopGroup wrapEventLoopGroup(EventLoopGroup executor) {
        return NETTY.wrapEventLoopGroup(executor, IdentityThreadNamer$.MODULE$);
    }

    public static EventLoopGroup wrapEventLoopGroup(EventLoopGroup executor, ThreadNamer threadNamer) {
        return NETTY.wrapEventLoopGroup(executor, threadNamer);
    }

}
