package vn.yain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Controller tiep nhan Webhook truc tiep tai duong dan /webhook/** (khong co tien to /api)
 * Dong bo xu ly voi ApiController cho SePAY / VietQR / Casso
 */
@RestController
@RequestMapping("/webhook")
public class WebhookForwardController {

    @Autowired
    private ApiController apiController;

    @RequestMapping(value = {"/vietqr", "/vietqr/", "/sepay", "/sepay/"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> forwardWebhook(
            HttpServletRequest request,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secretHeader,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Api-Key", required = false) String apiKeyHeader,
            @RequestBody(required = false) Map<String, Object> webhookData) {
        return apiController.handleVietQRWebhook(request, secretHeader, authHeader, apiKeyHeader, webhookData);
    }
}
