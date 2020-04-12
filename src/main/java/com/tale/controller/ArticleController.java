package com.tale.controller;

import com.tale.bootstrap.TaleConst;
import com.tale.extension.Commons;
import com.tale.kits.StringKit;
import com.tale.model.dto.ErrorCode;
import com.tale.model.dto.Types;
import com.tale.model.entity.Comments;
import com.tale.model.entity.Contents;
import com.tale.service.CommentsService;
import com.tale.service.ContentsService;
import com.tale.service.SiteService;
import com.tale.ui.RestResponse;
import com.tale.validators.CommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.security.validator.ValidatorException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.net.URLEncoder;

import static com.tale.bootstrap.TaleConst.COMMENT_APPROVED;
import static com.tale.bootstrap.TaleConst.COMMENT_NO_AUDIT;
import static com.tale.bootstrap.TaleConst.OPTION_ALLOW_COMMENT_AUDIT;

/**
 * @author biezhi
 * @date 2018/6/4
 */
@Slf4j
@Controller
public class ArticleController extends BaseController {

    @Autowired
    private ContentsService contentsService;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private SiteService siteService;

    /**
     * 自定义页面
     */
    @GetMapping(value = {"/:cid", "/:cid.html"})
    public String page(@PathParam("cid") String cid, HttpServletRequest request) {
        Contents contents = contentsService.getContents(cid);
        if (null == contents) {
            return this.render_404();
        }
        if (contents.getAllowComment()) {
            int cp = 1;
            String cpStr = request.getParameter("cp");
            if (StringUtils.isNotBlank(cpStr)) {
                cp = Integer.valueOf(cpStr);
            }
            request.setAttribute("cp", cp);
        }
        request.setAttribute("article", contents);
        Contents temp = new Contents();
        temp.setHits(contents.getHits() + 1);
        temp.updateById(contents.getCid());
        if (Types.ARTICLE.equals(contents.getType())) {
            return this.render("post");
        }
        if (Types.PAGE.equals(contents.getType())) {
            return this.render("page");
        }
        return this.render_404();
    }

    /**
     * 文章页
     */
    @GetMapping(value = {"article/:cid", "article/:cid.html"})
    public String post(HttpServletRequest request, @PathParam("cid") String cid) {
        Contents contents = contentsService.getContents(cid);
        if (null == contents) {
            return this.render_404();
        }
        if (Types.DRAFT.equals(contents.getStatus())) {
            return this.render_404();
        }
        request.setAttribute("article", contents);
        request.setAttribute("is_post", true);

        if (contents.getAllowComment()) {
            int cp = 1;
            String cpStr = request.getParameter("cp");
            if (StringUtils.isNotBlank(cpStr)) {
                cp = Integer.valueOf(cpStr);
            }
            request.setAttribute("cp", cp);
        }
        Contents temp = new Contents();
        temp.setHits(contents.getHits() + 1);
        temp.updateById(contents.getCid());
        return this.render("post");
    }

    /**
     * 评论操作
     */
    @ResponseBody
    @PostMapping(value = "comment")
    public RestResponse<?> comment(HttpServletRequest request, HttpServletResponse response,
                                   @RequestBody Comments comments) {


        String referer = request.getHeader("Referer");

        if (StringKit.isBlank(referer)) {
            return RestResponse.fail(ErrorCode.BAD_REQUEST);
        }

        if (!referer.startsWith(Commons.site_url())) {
            return RestResponse.fail("非法评论来源");
        }

        CommonValidator.valid(comments);


        if (TaleConst.OPTIONS.getBoolean(OPTION_ALLOW_COMMENT_AUDIT, true)) {
            comments.setStatus(COMMENT_NO_AUDIT);
        } else {
            comments.setStatus(COMMENT_APPROVED);
        }

        try {
            commentsService.saveComment(comments);

            Cookie cookie = new Cookie("tale_remember_author", URLEncoder.encode(comments.getAuthor()));
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);
            if (StringKit.isNotBlank(comments.getUrl())) {
                Cookie cookie1 = new Cookie("tale_remember_url", URLEncoder.encode(comments.getUrl()));
                cookie1.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(cookie1);
            }

            return RestResponse.ok();
        } catch (Exception e) {
            String msg = "评论发布失败";
            if (e instanceof ValidatorException) {
                msg = e.getMessage();
            } else {
                log.error(msg, e);
            }
            return RestResponse.fail(msg);
        }
    }




}
