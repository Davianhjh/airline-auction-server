package com.airline.auction;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.getIpAddressUtil;
import com.airline.tools.msgSendUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/searchTicket")
public class searchTicket {
    private static final boolean TEXTSWITCH = true;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public searchTicketRes search (@Context HttpServletRequest request, @Context HttpHeaders hh, searchTicketParam st) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        searchTicketRes res = new searchTicketRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifySearchTicketParams(st);
        if (!verifyResult) {
            res.setAuth(-1);
            res.setCode(1000);                               // parameters not correct
            return res;
        }
        String ipAddress = getIpAddressUtil.getIpAddress(request);

        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                               // fail to get mysql connection
            return res;
        }
        try {
            StringBuffer verifyCode = new StringBuffer("");
            for(int i=0; i<6; i++){
                int tmp = (int)Math.floor(Math.random()*10);
                verifyCode.append(tmp);
            }
            // for member search ticket
            if (AgiToken != null) {
                String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
                String verifySql = "SELECT id FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
                pst = conn.prepareStatement(verifySql);
                pst.setString(1, AgiToken);
                pst.setString(2, utcTimeStr);
                ret = pst.executeQuery();
                if (ret.next()) {
                    int uid = ret.getInt(1);
                    String insertSql = "INSERT INTO searchRecord (uid, ipAddr, tel, tel_country, platform, verifyCode, expire) VALUES (?,?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:10:00'))";
                    pst = conn.prepareStatement(insertSql);
                    pst.setInt(1, uid);
                    pst.setString(2, ipAddress);
                    pst.setString(3, st.getTel());
                    pst.setString(4, st.getTelCountry());
                    pst.setString(5, st.getPlatform());
                    pst.setString(6, verifyCode.toString());
                    pst.executeUpdate();
                    // TODO
                    // sending msg module
                    String smsText="【Agiview竞拍平台】您的验证码是" + verifyCode.toString() + "，10分钟内有效，请勿向任何人泄露。";
                    res.setAuth(1);
                    res.setCode(0);
                    res.setSearch(1);
                    if (TEXTSWITCH) {
                        res.setVerifyCode(verifyCode.toString());
                        return res;
                    } else if (msgSendUtil.sendMsg(st.getTel(), smsText) == 0){
                        return res;
                    } else {
                        res.setAuth(-2);
                        res.setCode(1070);
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1020);                           // user not found
                    return res;
                }
            }
            // for visitor search ticket
            else {
                String insertSql = "INSERT INTO searchRecord (uid, ipAddr, tel, tel_country, platform, verifyCode, expire) VALUES (0,?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:02:00'))";
                pst = conn.prepareStatement(insertSql);
                pst.setString(1, ipAddress);
                pst.setString(2, st.getTel());
                pst.setString(3, st.getTelCountry());
                pst.setString(4, st.getPlatform());
                pst.setString(5, verifyCode.toString());
                pst.executeUpdate();
                // TODO
                // sending msg module
                String smsText="【Agiview竞拍平台】您的验证码是" + verifyCode.toString() + "，10分钟内有效，请勿向任何人泄露。";
                res.setAuth(1);
                res.setCode(0);
                res.setSearch(1);
                if (TEXTSWITCH) {
                    res.setVerifyCode(verifyCode.toString());
                    return res;
                } else if (msgSendUtil.sendMsg(st.getTel(), smsText) == 0){
                    return res;
                } else {
                    res.setAuth(-2);
                    res.setCode(1070);
                    return res;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                           // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifySearchTicketParams (searchTicketParam st) {
        try {
            return st.getTel() != null && st.getTelCountry() != null && st.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
