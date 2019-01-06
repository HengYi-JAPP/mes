package com.hengyi.japp.mes.auto.interfaces.riamb;

import com.sun.security.auth.UserPrincipal;

import java.security.Principal;

/**
 * 北自所接口
 *
 * @author jzb 2018-06-25
 */
public interface RiambService {
    Principal PRINCIPAL = new UserPrincipal("if_riamb");
}
