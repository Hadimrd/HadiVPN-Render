FROM caddy:2-alpine

RUN apk add --no-cache ca-certificates curl unzip \
 && curl -fsSL "https://github.com/XTLS/Xray-core/releases/latest/download/Xray-linux-64.zip" -o /tmp/xray.zip \
 && unzip -o /tmp/xray.zip xray -d /usr/local/bin/ \
 && chmod 0755 /usr/local/bin/xray \
 && rm -f /tmp/xray.zip

COPY Caddyfile /etc/caddy/Caddyfile
COPY xray-config.json.template /etc/xray-config.json.template
COPY start.sh /usr/local/bin/start-hadi.sh

RUN chmod 0755 /usr/local/bin/start-hadi.sh

ENV PORT=10000
EXPOSE 10000

CMD ["/usr/local/bin/start-hadi.sh"]
