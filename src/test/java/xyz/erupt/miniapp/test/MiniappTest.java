package xyz.erupt.miniapp.test;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.util.crypto.SHA1;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.scope.EruptScopeService;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.core.MaServices;
import xyz.erupt.miniapp.live.MaDailyStat;
import xyz.erupt.miniapp.live.MaSubscribeTemplate;
import xyz.erupt.miniapp.model.*;
import xyz.erupt.miniapp.portal.MaPortalController;
import xyz.erupt.upms.service.EruptTokenService;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The module against a stub of the WeChat API: the workspace picks the app, live pages read
 * through it, saving a code or a link asks WeChat, and sign-in and the portal keep users current.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MiniappTest {

    private static final MaApiStub stub = new MaApiStub();

    @DynamicPropertySource
    static void apiHost(DynamicPropertyRegistry registry) {
        registry.add("erupt.miniapp.api-host", stub::url);
    }

    @Resource
    private EruptDao eruptDao;

    @Resource
    private MaServices services;

    @Resource
    private EruptService eruptService;

    @Resource
    private EruptModifyService eruptModifyService;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TestRestTemplate rest;

    private static MaApp app;

    private String token;

    @AfterAll
    static void stopStub() {
        stub.stop();
    }

    @BeforeEach
    void setUp() {
        stub.reset();
        stub.on("/wxaapi/newtmpl/gettemplate", "{\"errcode\":0,\"data\":[{\"priTmplId\":\"T1\",\"title\":\"Order shipped\",\"content\":\"Order {{character_string1.DATA}}\",\"example\":\"Order 1\",\"type\":2}]}");
        if (null == app) {
            app = new MaApp();
            app.setName("Demo");
            app.setAppId("wx-ma-demo");
            app.setSecret("secret");
            app.setToken("portal-token");
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> eruptDao.persist(app));
            services.register(app);
        }
        List<MetaMenu> menus = new ArrayList<>();
        for (Class<?> model : List.of(MaSubscribeTemplate.class, MaCode.class, MaLink.class, MaDailyStat.class, MaUser.class)) {
            menus.add(MetaMenu.createEruptClassMenu(model, null, 1));
        }
        MetaUserinfo user = new MetaUserinfo();
        user.setId(-60L);
        user.setAccount("ma-user");
        user.setUsername("ma-user");
        token = Erupts.generateCode(22);
        eruptTokenService.loginToken(user, menus, token, 5);
        this.workspace(app.getId());
    }

    @AfterEach
    void tearDown() {
        MetaContext.remove();
        RequestContextHolder.resetRequestAttributes();
    }

    private void workspace(Long appRowId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (null != appRowId) {
            request.addHeader(EruptScopeService.HEADER, URLEncoder.encode("{\"MaScope\":{\"app\":\"" + appRowId + "\"}}", StandardCharsets.UTF_8));
        }
        request.addHeader("token", token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MetaContext.remove();
        MetaContext.register(new MetaUser(-60L, "ma-user", "ma-user"));
        MetaContext.registerToken(token);
        MetaContext.register(new MetaErupt(MaSubscribeTemplate.class.getSimpleName()));
    }

    private List<Map<String, Object>> list(Class<?> clazz) {
        MetaContext.register(new MetaErupt(clazz.getSimpleName()));
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(100);
        return new ArrayList<>(eruptService.getEruptData(EruptCoreService.getErupt(clazz.getSimpleName()), query, null).getList());
    }

    private Object add(Class<?> clazz, JsonObject data) {
        MetaContext.register(new MetaErupt(clazz.getSimpleName()));
        return eruptModifyService.insertEruptData(EruptCoreService.getErupt(clazz.getSimpleName()), data);
    }

    @Test
    void subscribeTemplatesFollowTheWorkspace() {
        assertEquals(List.of("Order shipped"), list(MaSubscribeTemplate.class).stream().map(it -> it.get("title")).toList());
        this.workspace(null);
        assertThrows(EruptWebApiRuntimeException.class, () -> list(MaSubscribeTemplate.class), "no app picked: nothing is guessed");
    }

    @Test
    void addingATemplateGoesToWeChat() {
        stub.on("/wxaapi/newtmpl/addtemplate", "{\"errcode\":0,\"priTmplId\":\"T2\"}");
        JsonObject template = new JsonObject();
        template.addProperty("tid", "401");
        template.addProperty("kids", "1, 2");
        template.addProperty("scene", "orders");
        this.add(MaSubscribeTemplate.class, template);
        String body = stub.calls("/wxaapi/newtmpl/addtemplate").get(0).body();
        assertTrue(body.contains("\"tid\":\"401\"") && body.contains("[1,2]"), body);
    }

    @Test
    void savingACodeKeepsTheImageUnderTheUploadPath() {
        stub.on("/wxa/getwxacodeunlimit", "\u0089PNG stub image", "image/png");
        JsonObject code = new JsonObject();
        code.addProperty("name", "Spring campaign");
        code.addProperty("kind", MaCode.UNLIMITED);
        code.addProperty("page", "pages/index/index");
        code.addProperty("scene", "spring");
        code.addProperty("width", 430);
        code.addProperty("envVersion", "release");
        Object id = this.add(MaCode.class, code);
        MaCode saved = eruptDao.find(MaCode.class, id);
        assertEquals(app.getId(), saved.getApp().getId(), "the app comes from the workspace, not the form");
        assertTrue(new File(eruptProp.getUploadPath(), saved.getImage()).length() > 0, saved.getImage());
        assertTrue(stub.calls("/wxa/getwxacodeunlimit").get(0).body().contains("\"scene\":\"spring\""));
    }

    @Test
    void savingALinkAsksWeChat() {
        stub.on("/wxa/generate_urllink", "{\"errcode\":0,\"url_link\":\"https://wxaurl.cn/abc\"}");
        JsonObject link = new JsonObject();
        link.addProperty("name", "Home");
        link.addProperty("kind", MaLink.URL_LINK);
        link.addProperty("path", "pages/index/index");
        link.addProperty("expireDays", 7);
        Object id = this.add(MaLink.class, link);
        assertEquals("https://wxaurl.cn/abc", eruptDao.find(MaLink.class, id).getLink());
        assertTrue(stub.calls("/wxa/generate_urllink").get(0).body().contains("\"expire_interval\":7"));
    }

    @Test
    void dailyStatisticsMergeVisitsAndSummary() {
        stub.on("/datacube/getweanalysisappiddailyvisittrend", "{\"list\":[{\"ref_date\":\"20261018\",\"session_cnt\":12,\"visit_pv\":40,\"visit_uv\":9,\"visit_uv_new\":3,\"stay_time_uv\":55.5,\"visit_depth\":2.5}]}");
        stub.on("/datacube/getweanalysisappiddailysummarytrend", "{\"list\":[{\"ref_date\":\"20261018\",\"visit_total\":1000,\"share_pv\":4,\"share_uv\":2}]}");
        List<Map<String, Object>> rows = list(MaDailyStat.class);
        assertEquals(7, rows.size(), "the last seven days");
        assertEquals(40L, rows.get(0).get("visitPv"));
        assertEquals(1000L, rows.get(0).get("visitTotal"));
    }

    @Test
    void signInCreatesTheUserAndThePortalRecordsSubscriptions() {
        stub.on("/sns/jscode2session", "{\"openid\":\"o-1\",\"session_key\":\"k\",\"unionid\":\"u-1\"}");
        String login = rest.postForEntity("/miniapp/wx-ma-demo/login?code=abc", null, String.class).getBody();
        assertNotNull(login);
        assertTrue(login.contains("o-1") && !login.contains("session_key"), login);
        rest.postForEntity("/miniapp/wx-ma-demo/login?code=abc", null, String.class);
        MaUser user = eruptDao.lambdaQuery(MaUser.class).eq(MaUser::getOpenid, "o-1").one();
        assertEquals(2, user.getLoginCount());
        assertEquals("u-1", user.getUnionid());

        stub.on("/wxa/business/getuserphonenumber", "{\"errcode\":0,\"phone_info\":{\"phoneNumber\":\"+8613800000001\",\"purePhoneNumber\":\"13800000001\",\"countryCode\":\"86\"}}");
        rest.postForEntity("/miniapp/wx-ma-demo/phone?openid=o-1&code=xyz", null, String.class);
        assertEquals("13800000001", eruptDao.lambdaQuery(MaUser.class).eq(MaUser::getOpenid, "o-1").one().getPhone());

        String timestamp = "1700000000", nonce = "n1";
        String signature = SHA1.gen("portal-token", timestamp, nonce);
        String url = MaPortalController.PATH + "/wx-ma-demo?signature=" + signature + "&timestamp=" + timestamp + "&nonce=" + nonce;
        String event = "{\"ToUserName\":\"gh_demo\",\"FromUserName\":\"o-1\",\"CreateTime\":1700000000,\"MsgType\":\"event\",\"Event\":\"subscribe_msg_popup_event\","
                + "\"List\":[{\"TemplateId\":\"T1\",\"SubscribeStatusString\":\"accept\",\"PopupScene\":\"0\"}]}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assertEquals("success", rest.postForEntity(url, new HttpEntity<>(event, headers), String.class).getBody());
        MaSubscription subscription = eruptDao.lambdaQuery(MaSubscription.class).eq(MaSubscription::getOpenid, "o-1").one();
        assertEquals("T1", subscription.getTemplateId());
        assertEquals("accept", subscription.getStatus());
        assertEquals(1, eruptDao.lambdaQuery(MaInbox.class).eq(MaInbox::getOpenid, "o-1").count());
        assertEquals("Invalid signature", rest.postForEntity(url.replace(signature, "bad"), new HttpEntity<>(event, headers), String.class).getBody());
    }

}
