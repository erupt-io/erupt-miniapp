package xyz.erupt.miniapp.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <pre>
 * erupt:
 *   miniapp:
 *     callback-domain: https://admin.example.com   # shown as the server URL to paste into the WeChat console
 *     api-host: https://api.weixin.qq.com          # override to go through a proxy, or a stub in tests
 * </pre>
 *
 * @author YuePeng
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.miniapp")
public class EruptMiniappProperties {

    private String callbackDomain = "";

    private String apiHost = "";

}
