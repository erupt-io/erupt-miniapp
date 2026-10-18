package xyz.erupt.miniapp.portal;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.miniapp.core.MaServices;

/**
 * Where WeChat pushes messages: {@code {callback-domain}/miniapp/portal/{appid}}, the server URL
 * of the app's WeChat console configuration. JSON and XML, plain and safe (AES) modes all work.
 *
 * @author YuePeng
 */
@Slf4j
@RestController
@RequestMapping(MaPortalController.PATH + "/{appid}")
public class MaPortalController {

    public static final String PATH = "/miniapp/portal";

    private static final String REFUSED = "Invalid signature";

    private final MaServices services;

    private final MaInboxService inboxService;

    public MaPortalController(MaServices services, MaInboxService inboxService) {
        this.services = services;
        this.inboxService = inboxService;
    }

    // WeChat verifies the URL once with a GET
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String check(@PathVariable("appid") String appid, @RequestParam("signature") String signature,
                        @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce, @RequestParam("echostr") String echostr) {
        Long appRowId = services.idOf(appid);
        return null != appRowId && services.get(appRowId).checkSignature(timestamp, nonce, signature) ? echostr : REFUSED;
    }

    @PostMapping(produces = "text/plain;charset=utf-8")
    public String receive(@PathVariable("appid") String appid, @RequestBody String body, @RequestParam("signature") String signature,
                          @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce,
                          @RequestParam(value = "encrypt_type", required = false) String encryptType,
                          @RequestParam(value = "msg_signature", required = false) String msgSignature) {
        Long appRowId = services.idOf(appid);
        if (null == appRowId) return REFUSED;
        WxMaService ma = services.get(appRowId);
        if (!ma.checkSignature(timestamp, nonce, signature)) return REFUSED;
        boolean aes = "aes".equalsIgnoreCase(encryptType);
        boolean json = !"XML".equalsIgnoreCase(ma.getWxMaConfig().getMsgDataFormat());
        WxMaMessage in = json
                ? (aes ? WxMaMessage.fromEncryptedJson(body, ma.getWxMaConfig()) : WxMaMessage.fromJson(body))
                : (aes ? WxMaMessage.fromEncryptedXml(body, ma.getWxMaConfig(), timestamp, nonce, msgSignature) : WxMaMessage.fromXml(body));
        try {
            inboxService.receive(appRowId, in);
        } catch (RuntimeException e) {
            log.error("Mini Program message of {} not handled", appid, e);
        }
        // A Mini Program never answers inline: "success" tells WeChat the message arrived
        return "success";
    }

}
