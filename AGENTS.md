# AGENTS.md

This file provides guidance to coding agents (Claude Code, Codex, ...) when working with code in this repository.

## Commands

```bash
# Build and run the tests (what CI runs, .github/workflows/maven.yml, JDK 17)
mvn -B verify

# Tests only / a single test method (there is one test class)
mvn test
mvn test -Dtest=MiniappTest#savingALinkAsksWeChat

# Run the module on its own: http://localhost:8080, erupt / erupt
mvn spring-boot:run
```

Java 17, system `mvn` (no wrapper). The parent pom is `xyz.erupt:erupt:2.4.0`, which also supplies the
Spring Boot version and dependency management; if it does not resolve, install the erupt checkout
(`../erupt`, `mvn clean install -DskipTests`) into the local repository first.

## What this is

A library, not an application: a WeChat Mini Program admin for erupt, built on WxJava
(`weixin-java-miniapp`). A host application adds the dependency, registers an app and gets a
**Mini Program** menu: apps, users, subscribe templates and the template library, subscriptions, codes,
links, content checks, live rooms, the inbox and statistics. `erupt-spring-boot-starter` is `provided`
(the host brings the erupt stack); `spring-boot-maven-plugin` only serves `spring-boot:run`, repackaging
is off. `erupt-wechat` (Official Accounts) is a sibling repo with the same structure.

## Architecture

- **Registration.** `EruptMiniappAutoConfiguration` is listed in
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, implements
  `EruptModule` and builds the menus in `initMenus()` (root `$miniapp`; `MaSubscribeLog` is a hidden menu
  reached by drilling). Its `@ComponentScan` excludes `@SpringBootApplication` classes, because the
  standalone launcher `EruptMiniappApplication` lives in the same package. The launcher sets default
  properties only: an H2 file database under `./h2` and `erupt.miniapp.callback-domain`.
- **Workspace.** `core/MaScope` is an `@EruptScope` form with one field, the app, picked in the page header
  and sent with each request in the `erupt-scope` header. `MaContext` resolves the app of the current
  request and its `WxMaService`.
- **Two kinds of models.**
  - `model/`: JPA entities (`e_ma_*` tables, extending `MetaModel`) declared with
    `@EruptScope(MaScope.class)`; their `app` reference is bound to the scope, so lists are filtered and
    new rows land in the picked app. Side effects against WeChat happen in `proxy/` `DataProxy` classes
    (for example a code or link is generated on save).
  - `live/`: models read from the WeChat API on every open, nothing stored. Each declares
    `@EruptScope(value = MaScope.class, bind = false)` and `@EruptDataProcessor(MaDataService.DATA_PROCESSOR)`,
    and nests a `@Component` implementing `MaResource<T>` (`list`, optional `add`/`edit`/`delete`).
    `data/MaDataService` dispatches by model class and filters, sorts and pages in memory.
- **WeChat clients.** `core/MaServices` keeps one `WxMaService` per app row, registered at
  `ApplicationReadyEvent` and whenever an app is saved; the access token lives in memory. Every API call
  goes through `Ma.call` / `Ma.run`, which turns a `WxErrorException` into a translated
  `EruptWebApiRuntimeException`.
- **Endpoints without an erupt session (`portal/`).** `MaPortalController` receives WeChat's message push
  at `/miniapp/portal/{appid}` (JSON or XML, plain or AES) and hands it to `MaInboxService`;
  `MaAuthController` signs Mini Program users in (`POST /miniapp/{appid}/login`, `/phone`).
- **Row operations.** `handler/` holds the `OperationHandler`s, `form/` their input forms.
- **Configuration.** `EruptMiniappProperties` (`erupt.miniapp.callback-domain`, `erupt.miniapp.api-host`).
  App secrets, tokens and AES keys are rows of `MaApp`, never configuration: refer to them by field name.
- **i18n.** English keys, translated in `src/main/resources/i18n/erupt-miniapp.i18n.csv`; runtime error
  messages use `miniapp.*` keys through `I18nTranslate.$translate`.

## Tests

`MiniappTest` is a `@SpringBootTest` on a random port and an in-memory H2
(`src/test/resources/application.yml`). `MaApiStub` stands in for api.weixin.qq.com with the JDK's
`HttpServer` and is wired through `erupt.miniapp.api-host`; it records the calls it receives.

## Behaviour worth knowing (from README.md)

- Codes and links are generated once on save and kept; changing one means delete and save again.
- Statistics are reported by WeChat the morning after, so the trailing window ends yesterday.
- Code management for third-party platforms is out of scope (it needs a component access token).
