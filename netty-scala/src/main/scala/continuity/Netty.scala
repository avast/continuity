package com.avast.continuity

import io.netty.channel.EventLoopGroup

object Netty {

  def wrapEventLoopGroup(executor: EventLoopGroup)
                        (implicit threadNamer: ThreadNamer = IdentityThreadNamer): EventLoopGroup = new ContinuityEventLoopGroup(executor)(threadNamer)

}
