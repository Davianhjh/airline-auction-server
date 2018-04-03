package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;

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

@Path("/member/verifySetTel")
public class verifySetTel {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public verifySetTelRes verify (@Context HttpHeaders hh, verifySetTelParam vt) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        verifySetTelRes res = new verifySetTelRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        boolean verifyResult = verifyParam(vt);
        if (AgiToken == null || !verifyResult) {
            res.setAuth(-1);
            res.setCode(1000);                                   // parameters not correct
            return res;
        }

        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                   // fail to get mysql connection
            return res;
        }
        try {
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String verifySql = "SELECT id, tel, tel_country FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                if (ret.getString(2) != null || ret.getString(3) != null) {
                    res.setAuth(-1);
                    res.setCode(1022);                           // tel has been bind (by yourself)
                    return res;
                } else {
                    String searchSql = "SELECT uid, tel, tel_country FROM preRegister WHERE verifyCode=? AND platform=? AND expire > ?;";
                    pst = conn.prepareStatement(searchSql);
                    pst.setString(1, vt.getVerifyCode());
                    pst.setString(2, vt.getPlatform());
                    pst.setString(3, utcTimeStr);
                    ret = pst.executeQuery();
                    if (ret.next()) {
                        String tel = ret.getString(2);
                        String telCountry = ret.getString(3);
                        if (ret.getInt(1) == uid && tel != null && telCountry != null) {
                            String sql1 = "SELECT id FROM customerAccount WHERE tel=? AND tel_country=?";
                            pst = conn.prepareStatement(sql1);
                            pst.setString(1, tel);
                            pst.setString(2, telCountry);
                            ret2 = pst.executeQuery();
                            if (ret2.next()) {
                                res.setAuth(-1);
                                res.setCode(1022);               // tel has been bind (by someone else)
                                return res;
                            } else {
                                String sql2 = "UPDATE customerAccount set tel=?, tel_country=? WHERE id=?";
                                pst = conn.prepareStatement(sql2);
                                pst.setString(1, tel);
                                pst.setString(2, telCountry);
                                pst.setInt(3, uid);
                                pst.executeUpdate();

                                String sql3 = "DELETE FROM preRegister WHERE verifyCode=? AND platform=?;";
                                pst = conn.prepareStatement(sql3);
                                pst.setString(1, vt.getVerifyCode());
                                pst.setString(2, vt.getPlatform());
                                pst.executeUpdate();

                                res.setAuth(1);
                                res.setCode(0);
                                res.setBind(1);
                                return res;
                            }
                        } else {
                            res.setAuth(-1);
                            res.setCode(1026);                   // verify not correct
                            return res;
                        }
                    } else {
                        res.setAuth(-1);
                        res.setCode(1026);                       // verifyCode not correct
                        return res;
                    }
                }
            } else {
                res.setAuth(-1);
                res.setCode(1020);                               // user not found
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                   // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyParam (verifySetTelParam vt) {
        try {
            return vt.getVerifyCode() != null && vt.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
