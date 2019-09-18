package site.syso.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import site.syso.bean.Response;
import site.syso.util.QRCodeUtil;
import site.syso.util.RandomGenerator;
import site.syso.controller.websocket.ScanQRCodeServer;

@Controller
@RequestMapping("/qrcode")
public class QRCodeController {

    private static ConcurrentHashMap<String, Object> tickets = new ConcurrentHashMap<>();

    @Autowired
    private ScanQRCodeServer scanQRCodeServer;

    @GetMapping
    public String index() {
        return "index";
    }

    @ResponseBody
    @GetMapping("/generate")
    public Response generateQRCode() {
        String ticket = RandomGenerator.generate(32, RandomGenerator.LETTER | RandomGenerator.NUMBER);
        tickets.put(ticket, System.currentTimeMillis());
        String url = "localhost:9000/qrcode/validate?ticket=" + ticket;
        String qr = QRCodeUtil.genBase64QR(url, 200, 200);
        HashMap<String, String> map = new HashMap<String, String>() {{
            this.put("ticket", ticket);
            this.put("qr", qr);
        }};
        return Response.success(map);
    }

    @ResponseBody
    @GetMapping("/validate")
    public Response validateTicket(@RequestParam String ticket) {
        System.out.println("validate ticket. " + ticket);
        if (!tickets.containsKey(ticket)) {
            return Response.error(400, "二维码已失效");
        }
        tickets.remove(ticket);
        // 生成token
        Response<HashMap<String, Object>> success = Response.success(new HashMap<String, Object>() {{
            this.put("token", "this is token");
        }});
        try {
            scanQRCodeServer.response(ticket, JSONObject.toJSONString(success));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.success();
    }


}
