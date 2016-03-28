package octoteam.tahiti.server.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import octoteam.tahiti.protocol.SocketMessageProtos.Message;
import octoteam.tahiti.protocol.SocketMessageProtos.SessionExpiredEventBody;
import octoteam.tahiti.server.event.RateLimitExceededEvent;
import octoteam.tahiti.server.session.PipelineHelper;
import octoteam.tahiti.shared.netty.MessageHandler;

/**
 * TODO
 */
@ChannelHandler.Sharable
public class SessionExpireHandler extends MessageHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof RateLimitExceededEvent) {
            RateLimitExceededEvent event = (RateLimitExceededEvent) evt;
            if (event.getName().equals(RateLimitExceededEvent.NAME_PER_SESSION)) {
                Message.Builder resp = Message.newBuilder()
                        .setDirection(Message.DirectionCode.EVENT)
                        .setService(Message.ServiceCode.SESSION_EXPIRED_EVENT)
                        .setSessionExpiredEvent(SessionExpiredEventBody.newBuilder()
                                .setReason(SessionExpiredEventBody.Reason.EXPIRED)
                        );
                ctx.channel().writeAndFlush(resp.build());
                PipelineHelper.clearSession(ctx);
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

}
