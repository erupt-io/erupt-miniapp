# erupt-miniapp

A WeChat Mini Program admin on [erupt](https://www.erupt.xyz). Add the dependency, register an app, and a **Mini Program** menu appears with users, subscribe messages, codes and links, content checks, live rooms, the inbox and statistics. One app is picked in the page header and every page follows it.

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-miniapp</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

Needs erupt 2.4.0 or newer. H2 comes along as the zero-config database; set `spring.datasource.*` to use another. Built on [WxJava](https://github.com/binarywang/WxJava).

## Try it

```bash
mvn spring-boot:run
# http://localhost:8080  erupt / erupt
```

## What you get

| Menu | Source | Actions |
|---|---|---|
| Mini Programs | database | AppID, AppSecret, token, AES key, push format |
| Users | database | everyone who signed in through the API below, with the phone they shared; remark, send a customer service message |
| Subscribe templates | WeChat | add by library id, delete, send with a log |
| Template library | WeChat | browse WeChat's public templates by category, see their keywords, add one to the app |
| Subscriptions | database | who accepted or rejected which template, as WeChat pushes it |
| Codes | database | scene codes (unlimited) and path codes; the image is kept under the upload path |
| Links | database | URL links, URL schemes and short links, generated on save |
| Content checks | database | run a text through WeChat's content security check and keep the verdict |
| Live rooms | WeChat | the live streaming component's rooms |
| Inbox | database | every message and event the portal received |
| Daily statistics / Retention | WeChat | the last 7 days, or a searched range up to 30 days |

Pages marked *WeChat* read the API when opened and page in memory; nothing of theirs is stored.

## The Workspace

The header shows a **Workspace** picker with the Mini Program. Database pages are filtered by it and new rows land in it; WeChat pages call its API. The pick travels with every request as plain JSON (`erupt-scope` header) and the server checks it against the apps the user is offered. This is erupt's `@EruptScope` feature; `MaScope` is a plain erupt form.

## Signing users in

The Mini Program calls two endpoints, no erupt session needed. The session key stays on the server.

```
POST /miniapp/{appid}/login?code=<wx.login code>                → {"data": {"openid": "...", "unionid": "..."}}
POST /miniapp/{appid}/phone?openid=...&code=<getPhoneNumber code> → {"data": "13800000000"}
```

Each login upserts the user; the phone is kept on the user once shared.

## Message push

Copy the app's **Server URL** (`{callback-domain}/miniapp/portal/{appid}`) into the WeChat console, *Development → Message push*, with the same token and format (JSON or XML). Customer service messages and events land in the Inbox; subscribe prompt answers are kept under Subscriptions, so the Send action knows who may receive a template.

```yaml
erupt:
  miniapp:
    callback-domain: https://admin.example.com   # shown in the Server URL column
    # api-host: https://proxy.example.com        # route API calls through a proxy
```

## Notes

- **Subscribe messages** reach a user once per acceptance of a one-time template; a long-term template needs a qualified category.
- **Codes and links** are generated once on save and kept; delete and save again to change one.
- **Statistics** are reported the morning after, so the trailing window ends yesterday.
- Code management for third-party platforms is out of scope: it needs a component access token, not an app's own.

## Tests

`mvn test` runs the module against a stub of the WeChat API served by the JDK's `HttpServer`, on an in-memory H2.
