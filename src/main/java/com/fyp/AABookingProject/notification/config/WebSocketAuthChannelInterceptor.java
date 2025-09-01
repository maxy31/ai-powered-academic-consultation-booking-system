package com.fyp.AABookingProject.notification.config;

import com.fyp.AABookingProject.security.jwt.JwtUtils;
import com.fyp.AABookingProject.security.services.UserDetailsServiceImpl;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public WebSocketAuthChannelInterceptor(JwtUtils jwtUtils,
                                           UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 只在 CONNECT 处理认证
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Authentication existing = SecurityContextHolder.getContext().getAuthentication();
            if (existing == null || !existing.isAuthenticated()) {
                // 1. 取 STOMP 头 Authorization
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                String token = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                } else {
                    // 2. TODO: 可扩展解析 query param ?token=...
                }
                // 如果还没有 token，这里可以再添加更多自定义来源解析逻辑
                if (token == null) {
                    // 最后看 session attributes（如果你在 HandshakeInterceptor 里放过）
                }

                if (token == null) {
                    log.warn("WS CONNECT missing token sessionId={}", accessor.getSessionId());
                    throw new IllegalArgumentException("Missing JWT for WebSocket CONNECT");
                }
                if (!jwtUtils.validateJwtToken(token)) {
                    log.warn("WS CONNECT invalid token sessionId={}", accessor.getSessionId());
                    throw new IllegalArgumentException("Invalid JWT");
                }
                Long userId = jwtUtils.getUserIdFromJwtToken(token);
                UserDetails userDetails = userDetailsService.loadById(userId);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                accessor.setUser(auth);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("WS CONNECT authenticated: principal='{}' userId={} sessionId={}", auth.getName(), userId, accessor.getSessionId());
            } else {
                log.info("WS CONNECT reused existing auth principal='{}' sessionId={}", existing.getName(), accessor.getSessionId());
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // 确保订阅时已经有用户
            if (accessor.getUser() == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                    throw new IllegalArgumentException("Unauthorized WebSocket subscription");
                }
                accessor.setUser(auth);
            }
            if (accessor.getUser() != null) {
                log.info("WS SUBSCRIBE principal='{}' destination='{}' sessionId={}", accessor.getUser().getName(), accessor.getDestination(), accessor.getSessionId());
            }
        }
        return message;
    }
}
