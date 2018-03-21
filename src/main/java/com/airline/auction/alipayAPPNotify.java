package com.airline.auction;

import com.airline.member.passengerFlight;
import com.airline.tools.AlipayAPPUtil;
import com.airline.tools.HiKariCPHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Path("/auction/alipay_notify")
public class alipayAPPNotify {
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String verifyAlipay (Form form) {
        MultivaluedMap<String, String> body = form.asMap();
        if (body.size() == 0)
            return "success";
        else {
            String out_trade_no = body.getFirst("out_trade_number");
            String total_Amount = body.getFirst("total_Amount");
            String seller_id = body.getFirst("seller_id");
            String app_id = body.getFirst("app_id");
            String charset = body.getFirst("charset");

            Map<String, String> params = new HashMap<String, String>();
            for (Iterator it = body.entrySet().iterator(); it.hasNext(); ) {
                String name = (String) it.next();
                List<String> values = body.get(name);
                String valueStr = "";
                for (int i = 0; i < values.size(); i++) {
                    valueStr = (i == values.size() - 1) ? valueStr + values.get(i) : valueStr + values.get(i) + ",";
                }
                try {
                    valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return "fail";
                }
                params.put(name, valueStr);
            }
            boolean verifyResult = AlipayAPPUtil.verifyPayment(params, charset);
            if (!verifyResult) {
                return "fail";
            }
            else {
                Connection conn = null;
                try {
                    conn = HiKariCPHandler.getConn();
                    PreparedStatement pst;
                    ResultSet ret;
                    String searchSql = "SELECT totalAmount FROM tradeRecord WHERE transactionNo=?;";
                    pst = conn.prepareStatement(searchSql);
                    pst.setString(1, out_trade_no);
                    ret = pst.executeQuery();
                    if (ret.next()) {
                        double totalAmount = ret.getDouble(1);
                        Properties paymentProp = new Properties();
                        InputStream in = passengerFlight.class.getResourceAsStream("/paymentManage.properties");
                        paymentProp.load(in);
                        in.close();
                        if (app_id.equals(paymentProp.getProperty("appid")) && seller_id.equals(paymentProp.getProperty("sellerid")) && Double.parseDouble(total_Amount) == totalAmount) {
                            String updateSql = "UPDATE tradeRecord SET paymentState=1 WHERE transactionNo=?;";
                            pst = conn.prepareStatement(updateSql);
                            pst.setString(1, out_trade_no);
                            pst.executeUpdate();
                            System.out.println("payment verified success!");
                            return "success";
                        }
                        else {
                            return "fail";
                        }
                    } else {
                        return "fail";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "fail";
                } finally {
                    try {
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}