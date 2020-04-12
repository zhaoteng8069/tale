package com.tale.controller;

import com.tale.bootstrap.TaleConst;
import com.tale.model.dto.Archive;
import com.tale.model.dto.Types;
import com.tale.model.entity.Contents;
import com.tale.model.params.PageParam;
import com.tale.service.SiteService;
import com.tale.utils.TaleUtils;
import io.github.biezhi.anima.enums.OrderBy;
import io.github.biezhi.anima.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.List;

import static io.github.biezhi.anima.Anima.select;

/**
 * 首页、归档、Feed、评论
 *
 * @author biezhi
 * @since 1.3.1
 */
@Controller
@Slf4j
public class IndexController extends BaseController {

    @Autowired
    private SiteService siteService;

    /**
     * 首页
     *
     * @return
     */
    @GetMapping
    public String index(HttpServletRequest request, PageParam pageParam) {
        return this.index(request, 1, pageParam.getLimit());
    }

    /**
     * 首页分页
     *
     * @param request
     * @param page
     * @param limit
     * @return
     */
    @GetMapping(value = {"page/:page", "page/:page.html"})
    public String index(HttpServletRequest request, @PathParam("page") int page, @RequestParam(defaultValue = "12") int limit) {
        page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
        if (page > 1) {
            this.title(request, "第" + page + "页");
        }
        request.setAttribute("page_num", page);
        request.setAttribute("limit", limit);
        request.setAttribute("is_home", true);
        request.setAttribute("page_prefix", "/page");
        return this.render("index");
    }


    /**
     * 搜索页
     *
     * @param keyword
     * @return
     */
    @GetMapping(value = {"search/:keyword", "search/:keyword.html"})
    public String search(HttpServletRequest request, @PathParam("keyword") String keyword, @RequestParam(defaultValue = "12") int limit) {
        return this.search(request, keyword, 1, limit);
    }

    @GetMapping(value = {"search", "search.html"})
    public String search(HttpServletRequest request, @RequestParam(defaultValue = "12") int limit) {
        String keyword = request.getParameter("s");
        if (StringUtils.isBlank(keyword)) {
            keyword = "";
        }
        return this.search(request, keyword, 1, limit);
    }

    @GetMapping(value = {"search/:keyword/:page", "search/:keyword/:page.html"})
    public String search(HttpServletRequest request, @PathParam("keyword") String keyword, @PathParam("page") int page,
                         @RequestParam(defaultValue = "12") int limit) {

        page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;

        Page<Contents> articles = select().from(Contents.class)
                .where(Contents::getType, Types.ARTICLE)
                .and(Contents::getStatus, Types.PUBLISH)
                .like(Contents::getTitle, "%" + keyword + "%")
                .order(Contents::getCreated, OrderBy.DESC)
                .page(page, limit);

        request.setAttribute("articles", articles);
        request.setAttribute("type", "搜索");
        request.setAttribute("keyword", keyword);
        request.setAttribute("page_prefix", "/search/" + keyword);
        return this.render("page-category");
    }

    /**
     * 归档页
     *
     * @return
     */
    @GetMapping(value = {"archives", "archives.html"})
    public String archives(HttpServletRequest request) {
        List<Archive> archives = siteService.getArchives();
        request.setAttribute("archives", archives);
        request.setAttribute("is_archive", true);
        return this.render("archives");
    }

    /**
     * feed页
     *
     * @return
     */
    @GetMapping(value = {"feed", "feed.xml", "atom.xml"})
    public void feed(HttpServletResponse response) {

        List<Contents> articles = select().from(Contents.class)
                .where(Contents::getType, Types.ARTICLE)
                .and(Contents::getStatus, Types.PUBLISH)
                .and(Contents::getAllowFeed, true)
                .order(Contents::getCreated, OrderBy.DESC)
                .all();

        try {
            String xml = TaleUtils.getRssXml(articles);
            response.setContentType("text/xml; charset=utf-8");
            /*response.body(xml);*/
        } catch (Exception e) {
            log.error("生成 rss 失败", e);
        }
    }

    /**
     * sitemap 站点地图
     *
     * @return
     */
    @GetMapping(value = {"sitemap", "sitemap.xml"})
    public void sitemap(HttpServletResponse response) {
        List<Contents> articles = select().from(Contents.class)
                .where(Contents::getType, Types.ARTICLE)
                .and(Contents::getStatus, Types.PUBLISH)
                .and(Contents::getAllowFeed, true)
                .order(Contents::getCreated, OrderBy.DESC)
                .all();
        try {
            String xml = TaleUtils.getSitemapXml(articles);
            response.setContentType("text/xml; charset=utf-8");
            /*response.body(xml);*/
        } catch (Exception e) {
            log.error("生成 sitemap 失败", e);
        }
    }

    /**
     * 注销
     */
    /*@RequestMapping(value = "logout")
    public void logout(RouteContext context) {
        TaleUtils.logout(context);
    }*/

}