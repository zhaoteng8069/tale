package com.tale.controller;

import com.tale.bootstrap.TaleConst;
import com.tale.model.dto.Types;
import com.tale.model.entity.Contents;
import com.tale.model.entity.Metas;
import com.tale.service.ContentsService;
import com.tale.service.MetasService;
import io.github.biezhi.anima.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分类、标签控制器
 *
 * @author biezhi
 * @date 2017/9/17
 */
@Controller
public class CategoryController extends BaseController {

    @Autowired
    private ContentsService contentsService;

    @Autowired
    private MetasService metasService;

    /**
     * 分类列表页
     *
     * @since 1.3.1
     */
    @GetMapping(value = {"categories", "categories.html"})
    public String categories(HttpServletRequest request) {
        Map<String, List<Contents>> mapping    = metasService.getMetaMapping(Types.CATEGORY);
        Set<String>                 categories = mapping.keySet();
        request.setAttribute("categories", categories);
        request.setAttribute("mapping", mapping);
        return this.render("categories");
    }

    /**
     * 某个分类详情页
     */
    @GetMapping(value = {"category/:keyword", "category/:keyword.html"})
    public String categories(HttpServletRequest request, @PathParam("keyword") String keyword, @RequestParam(defaultValue = "12") int limit) {
        return this.categories(request, keyword, 1, limit);
    }

    /**
     * 某个分类详情页分页
     */
    @GetMapping(value = {"category/:keyword/:page", "category/:keyword/:page.html"})
    public String categories(HttpServletRequest request, @PathParam("keyword") String keyword,
                             @PathParam("page") int page, @RequestParam(defaultValue = "12") int limit) {

        page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
        Metas metaDto = metasService.getMeta(Types.CATEGORY, keyword);
        if (null == metaDto) {
            return this.render_404();
        }

        Page<Contents> contentsPage = contentsService.getArticles(metaDto.getMid(), page, limit);
        request.setAttribute("articles", contentsPage);
        request.setAttribute("meta", metaDto);
        request.setAttribute("type", "分类");
        request.setAttribute("keyword", keyword);
        request.setAttribute("is_category", true);
        request.setAttribute("page_prefix", "/category/" + keyword);

        return this.render("page-category");
    }

    /**
     * 标签列表页面
     * <p>
     * 渲染所有的标签和文章映射
     *
     * @since 1.3.1
     */
    @GetMapping(value = {"tags", "tags.html"})
    public String tags(HttpServletRequest request) {
        Map<String, List<Contents>> mapping = metasService.getMetaMapping(Types.TAG);
        Set<String>                 tags    = mapping.keySet();
        request.setAttribute("tags", tags);
        request.setAttribute("mapping", mapping);
        return this.render("tags");
    }

    /**
     * 标签详情页
     *
     * @param name 标签名
     */
    @GetMapping(value = {"tag/:name", "tag/:name.html"})
    public String tagPage(HttpServletRequest request, @PathParam("name") String name,
                          @RequestParam(defaultValue = "12") int limit) {
        return this.tags(request, name, 1, limit);
    }

    /**
     * 标签下文章分页
     */
    @GetMapping(value = {"tag/:name/:page", "tag/:name/:page.html"})
    public String tags(HttpServletRequest request, @PathParam("name") String name, @PathParam("page") int page,
                       @RequestParam(defaultValue = "12") int limit) {
        page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
        Metas metaDto = metasService.getMeta(Types.TAG, name);
        if (null == metaDto) {
            return this.render_404();
        }

        Page<Contents> contentsPage = contentsService.getArticles(metaDto.getMid(), page, limit);
        request.setAttribute("articles", contentsPage);
        request.setAttribute("meta", metaDto);
        request.setAttribute("type", "标签");
        request.setAttribute("keyword", name);
        request.setAttribute("is_tag", true);
        request.setAttribute("page_prefix", "/tag/" + name);

        return this.render("page-category");
    }

}