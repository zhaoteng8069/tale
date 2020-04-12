package com.tale.controller;


import com.tale.bootstrap.TaleConst;
import com.tale.constants.Environment;
import com.tale.model.entity.Users;
import com.tale.model.params.InstallParam;
import com.tale.service.OptionsService;
import com.tale.service.SiteService;
import com.tale.ui.RestResponse;
import com.tale.utils.TaleUtils;
import com.tale.validators.CommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.tale.bootstrap.TaleConst.CLASSPATH;
import static com.tale.bootstrap.TaleConst.OPTION_ALLOW_INSTALL;

@Slf4j
@Controller
@RequestMapping("install")
public class InstallController extends BaseController {

    @Autowired
    private SiteService siteService;

    @Autowired
    private OptionsService optionsService;

    /**
     * 安装页
     */
    @GetMapping
    public String index(HttpServletRequest request) {
        boolean existInstall   = Files.exists(Paths.get(CLASSPATH + "install.lock"));
        boolean allowReinstall = TaleConst.OPTIONS.getBoolean(OPTION_ALLOW_INSTALL, false);
        request.setAttribute("is_install", !allowReinstall && existInstall);
        return "install";
    }

    @PostMapping
    @ResponseBody
    public RestResponse<?> doInstall(InstallParam installParam) {
        if (isRepeatInstall()) {
            return RestResponse.fail("请勿重复安装");
        }

        CommonValidator.valid(installParam);

        Users temp = new Users();
        temp.setUsername(installParam.getAdminUser());
        temp.setPassword(installParam.getAdminPwd());
        temp.setEmail(installParam.getAdminEmail());

        siteService.initSite(temp);

        String siteUrl = TaleUtils.buildURL(installParam.getSiteUrl());
        optionsService.saveOption("site_title", installParam.getSiteTitle());
        optionsService.saveOption("site_url", siteUrl);

        TaleConst.OPTIONS = Environment.of(optionsService.getOptions());

        return RestResponse.ok();
    }

    private boolean isRepeatInstall() {
        return Files.exists(Paths.get(CLASSPATH + "install.lock"))
                && TaleConst.OPTIONS.getInt("allow_install", 0) != 1;
    }

}
