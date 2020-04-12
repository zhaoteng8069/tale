package com.tale.controller.admin;

import com.tale.annotation.SysLog;
import com.tale.bootstrap.TaleConst;
import com.tale.bootstrap.TaleLoader;
import com.tale.constants.Const;
import com.tale.constants.Environment;
import com.tale.controller.BaseController;
import com.tale.extension.Commons;
import com.tale.kits.JsonKit;
import com.tale.kits.StringKit;
import com.tale.kits.WebKit;
import com.tale.model.dto.ThemeDto;
import com.tale.model.dto.Types;
import com.tale.model.entity.*;
import com.tale.model.params.*;
import com.tale.service.*;
import com.tale.ui.RestResponse;
import com.tale.validators.CommonValidator;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.enums.OrderBy;
import io.github.biezhi.anima.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.tale.bootstrap.TaleConst.*;
import static io.github.biezhi.anima.Anima.delete;
import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/9
 */
@Slf4j
@RestController
@RequestMapping("admin/api")
public class AdminApiController extends BaseController {

    @Autowired
    private MetasService metasService;

    @Autowired
    private ContentsService contentsService;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private SiteService siteService;

    @GetMapping("logs")
    public RestResponse sysLogs(PageParam pageParam) {
        return RestResponse.ok(select().from(Logs.class).order(Logs::getId, OrderBy.DESC).page(pageParam.getPage(), pageParam.getLimit()));
    }

    @SysLog("删除页面")
    @PostMapping("page/delete/:cid")
    public RestResponse<?> deletePage(@PathParam("id") Integer cid) {
        contentsService.delete(cid);
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @GetMapping("articles/:cid")
    public RestResponse article(@PathParam("cid") String cid) {
        Contents contents = contentsService.getContents(cid);
        contents.setContent("");
        return RestResponse.ok(contents);
    }

    @GetMapping("articles/content/:cid")
    public void articleContent(@PathParam("id") String cid, HttpServletResponse response) {
        Contents contents = contentsService.getContents(cid);
        /*response.text(contents.getContent());*/
    }

    @PostMapping("article/new")
    public RestResponse newArticle(@RequestBody Contents contents) {
        CommonValidator.valid(contents);

        Users users = this.user();
        contents.setType(Types.ARTICLE);
        contents.setAuthorId(users.getUid());
        //将点击数设初始化为0
        contents.setHits(0);
        //将评论数设初始化为0
        contents.setCommentsNum(0);
        if (StringKit.isBlank(contents.getCategories())) {
            contents.setCategories("默认分类");
        }
        Integer cid = contentsService.publish(contents);
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok(cid);
    }

    @PostMapping("article/delete/:cid")
    public RestResponse<?> deleteArticle(@PathParam("cid") Integer cid) {
        contentsService.delete(cid);
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @PostMapping("article/update")
    public RestResponse updateArticle(@RequestBody Contents contents) {
        if (null == contents || null == contents.getCid()) {
            return RestResponse.fail("缺少参数，请重试");
        }
        CommonValidator.valid(contents);
        Integer cid = contents.getCid();
        contentsService.updateArticle(contents);
        return RestResponse.ok(cid);
    }

    @GetMapping("articles")
    public RestResponse articleList(ArticleParam articleParam) {
        articleParam.setType(Types.ARTICLE);
        articleParam.setOrderBy("created desc");
        Page<Contents> articles = contentsService.findArticles(articleParam);
        return RestResponse.ok(articles);
    }

    @GetMapping("pages")
    public RestResponse pageList(ArticleParam articleParam) {
        articleParam.setType(Types.PAGE);
        articleParam.setOrderBy("created desc");
        Page<Contents> articles = contentsService.findArticles(articleParam);
        return RestResponse.ok(articles);
    }

    @SysLog("发布页面")
    @PostMapping("page/new")
    public RestResponse<?> newPage(@RequestBody Contents contents) {

        CommonValidator.valid(contents);

        Users users = this.user();
        contents.setType(Types.PAGE);
        contents.setAllowPing(true);
        contents.setAuthorId(users.getUid());
        contentsService.publish(contents);
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @SysLog("修改页面")
    @PostMapping("page/update")
    public RestResponse<?> updatePage(@RequestBody Contents contents) {
        CommonValidator.valid(contents);

        if (null == contents.getCid()) {
            return RestResponse.fail("缺少参数，请重试");
        }
        Integer cid = contents.getCid();
        contents.setType(Types.PAGE);
        contentsService.updateArticle(contents);
        return RestResponse.ok(cid);
    }

    @SysLog("保存分类")
    @PostMapping("category/save")
    public RestResponse<?> saveCategory(@RequestBody MetaParam metaParam) {
        metasService.saveMeta(Types.CATEGORY, metaParam.getCname(), metaParam.getMid());
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @SysLog("删除分类/标签")
    @PostMapping("category/delete/:mid")
    public RestResponse<?> deleteMeta(@PathParam("mid") Integer mid) {
        metasService.delete(mid);
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @GetMapping("comments")
    public RestResponse commentList(CommentParam commentParam) {
        Users users = this.user();
        commentParam.setExcludeUID(users.getUid());

        Page<Comments> commentsPage = commentsService.findComments(commentParam);
        return RestResponse.ok(commentsPage);
    }

    @SysLog("删除评论")
    @PostMapping("comment/delete/:coid")
    public RestResponse<?> deleteComment(@PathParam("coid") Integer coid) {
        Comments comments = select().from(Comments.class).byId(coid);
        if (null == comments) {
            return RestResponse.fail("不存在该评论");
        }
        commentsService.delete(coid, comments.getCid());
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @SysLog("修改评论状态")
    @PostMapping("comment/status")
    public RestResponse<?> updateStatus(@RequestBody Comments comments) {
        comments.update();
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @SysLog("回复评论")
    @PostMapping("comment/reply")
    public RestResponse<?> replyComment(@RequestBody Comments comments, HttpServletRequest request) {
        CommonValidator.validAdmin(comments);

        Comments c = select().from(Comments.class).byId(comments.getCoid());
        if (null == c) {
            return RestResponse.fail("不存在该评论");
        }
        Users users = this.user();
        comments.setAuthor(users.getUsername());
        comments.setAuthorId(users.getUid());
        comments.setCid(c.getCid());
        comments.setUrl(users.getHomeUrl());

        if (StringKit.isNotBlank(users.getEmail())) {
            comments.setMail(users.getEmail());
        } else {
            comments.setMail("");
        }
        comments.setStatus(TaleConst.COMMENT_APPROVED);
        comments.setParent(comments.getCoid());
        commentsService.saveComment(comments);
        siteService.cleanCache(Types.SYS_STATISTICS);
        return RestResponse.ok();
    }

    @GetMapping("attaches")
    public RestResponse attachList(PageParam pageParam) {

        Page<Attach> attachPage = select().from(Attach.class)
                .order(Attach::getCreated, OrderBy.DESC)
                .page(pageParam.getPage(), pageParam.getLimit());

        return RestResponse.ok(attachPage);
    }

    @SysLog("删除附件")
    @PostMapping("attach/delete/:id")
    public RestResponse<?> deleteAttach(@PathParam("id") Integer id) throws IOException {
        Attach attach = select().from(Attach.class).byId(id);
        if (null == attach) {
            return RestResponse.fail("不存在该附件");
        }
        String key = attach.getFkey();
        siteService.cleanCache(Types.SYS_STATISTICS);
        String             filePath = CLASSPATH.substring(0, CLASSPATH.length() - 1) + key;
        java.nio.file.Path path     = Paths.get(filePath);
        log.info("Delete attach: [{}]", filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Anima.deleteById(Attach.class, id);
        return RestResponse.ok();
    }

    @GetMapping("categories")
    public RestResponse categoryList() {
        List<Metas> categories = siteService.getMetas(Types.RECENT_META, Types.CATEGORY, TaleConst.MAX_POSTS);
        return RestResponse.ok(categories);
    }

    @GetMapping("tags")
    public RestResponse tagList() {
        List<Metas> tags = siteService.getMetas(Types.RECENT_META, Types.TAG, TaleConst.MAX_POSTS);
        return RestResponse.ok(tags);
    }

    @GetMapping("options")
    public RestResponse options() {
        Map<String, String> options = optionsService.getOptions();
        return RestResponse.ok(options);
    }

    @SysLog("保存系统配置")
    @PostMapping("options/save")
    public RestResponse<?> saveOptions(HttpServletRequest request) {
        Map<String, String[]> querys = request.getParameterMap();
        querys.forEach((k, v) -> optionsService.saveOption(k, v[0]));
        Environment config = Environment.of(optionsService.getOptions());
        TaleConst.OPTIONS = config;
        return RestResponse.ok();
    }

    @SysLog("保存高级选项设置")
    @PostMapping("advanced/save")
    public RestResponse<?> saveAdvance(AdvanceParam advanceParam) {
        // 清除缓存
        if (StringKit.isNotBlank(advanceParam.getCacheKey())) {
            if ("*".equals(advanceParam.getCacheKey())) {
                cache.clean();
            } else {
                cache.del(advanceParam.getCacheKey());
            }
        }
        // 要过过滤的黑名单列表
        if (StringKit.isNotBlank(advanceParam.getBlockIps())) {
            optionsService.saveOption(Types.BLOCK_IPS, advanceParam.getBlockIps());
            TaleConst.BLOCK_IPS.addAll(Arrays.asList(advanceParam.getBlockIps().split(",")));
        } else {
            optionsService.saveOption(Types.BLOCK_IPS, "");
            TaleConst.BLOCK_IPS.clear();
        }
        // 处理卸载插件
        if (StringKit.isNotBlank(advanceParam.getPluginName())) {
            String key = "plugin_";
            // 卸载所有插件
            if (!"*".equals(advanceParam.getPluginName())) {
                key = "plugin_" + advanceParam.getPluginName();
            } else {
                optionsService.saveOption(Types.ATTACH_URL, Commons.site_url());
            }
            optionsService.deleteOption(key);
        }

        if (StringKit.isNotBlank(advanceParam.getCdnURL())) {
            optionsService.saveOption(OPTION_CDN_URL, advanceParam.getCdnURL());
            TaleConst.OPTIONS.set(OPTION_CDN_URL, advanceParam.getCdnURL());
        }

        // 是否允许重新安装
        if (StringKit.isNotBlank(advanceParam.getAllowInstall())) {
            optionsService.saveOption(OPTION_ALLOW_INSTALL, advanceParam.getAllowInstall());
            TaleConst.OPTIONS.set(OPTION_ALLOW_INSTALL, advanceParam.getAllowInstall());
        }

        // 评论是否需要审核
        if (StringKit.isNotBlank(advanceParam.getAllowCommentAudit())) {
            optionsService.saveOption(OPTION_ALLOW_COMMENT_AUDIT, advanceParam.getAllowCommentAudit());
            TaleConst.OPTIONS.set(OPTION_ALLOW_COMMENT_AUDIT, advanceParam.getAllowCommentAudit());
        }

        // 是否允许公共资源CDN
        if (StringKit.isNotBlank(advanceParam.getAllowCloudCDN())) {
            optionsService.saveOption(OPTION_ALLOW_CLOUD_CDN, advanceParam.getAllowCloudCDN());
            TaleConst.OPTIONS.set(OPTION_ALLOW_CLOUD_CDN, advanceParam.getAllowCloudCDN());
        }
        return RestResponse.ok();
    }

    @GetMapping("themes")
    public RestResponse getThemes(HttpServletResponse response) {
        // 读取主题
        String         themesDir  = CLASSPATH + "templates/themes";
        File[]         themesFile = new File(themesDir).listFiles();
        List<ThemeDto> themes     = new ArrayList<>(themesFile.length);
        for (File f : themesFile) {
            if (f.isDirectory()) {
                ThemeDto themeDto = new ThemeDto(f.getName());
                if (Files.exists(Paths.get(f.getPath() + "/setting.html"))) {
                    themeDto.setHasSetting(true);
                }
                themes.add(themeDto);
                try {
                   /* response.addStatics("/templates/themes/" + f.getName() + "/screenshot.png");*/
                } catch (Exception e) {
                }
            }
        }
        return RestResponse.ok(themes);
    }

    @SysLog("保存主题设置")
    @PostMapping("themes/setting")
    public RestResponse<?> saveSetting(HttpServletRequest request) {
        Map<String, String[]> query = request.getParameterMap();

        // theme_milk_options => {  }
        String currentTheme = Commons.site_theme();
        String key          = "theme_" + currentTheme + "_options";

        Map<String, String> options = new HashMap<>();
        query.forEach((k, v) -> options.put(k, v[0]));

        optionsService.saveOption(key, JsonKit.toString(options));

        TaleConst.OPTIONS = Environment.of(optionsService.getOptions());
        return RestResponse.ok();
    }

    @SysLog("激活主题")
    @PostMapping("themes/active")
    public RestResponse<?> activeTheme(@RequestBody ThemeParam themeParam) {
        optionsService.saveOption(OPTION_SITE_THEME, themeParam.getSiteTheme());
        delete().from(Options.class).where(Options::getName).like("theme_option_%").execute();

        TaleConst.OPTIONS.set(OPTION_SITE_THEME, themeParam.getSiteTheme());
        BaseController.THEME = "themes/" + themeParam.getSiteTheme();

        String themePath = "/templates/themes/" + themeParam.getSiteTheme();
        try {
            TaleLoader.loadTheme(themePath);
        } catch (Exception e) {
        }
        return RestResponse.ok();
    }

    @SysLog("保存模板")
    @PostMapping("template/save")
    public RestResponse<?> saveTpl(@RequestBody TemplateParam templateParam) throws IOException {
        if (StringKit.isBlank(templateParam.getFileName())) {
            return RestResponse.fail("缺少参数，请重试");
        }
        String content   = templateParam.getContent();
        String themePath = Const.CLASSPATH + File.separatorChar + "templates" + File.separatorChar + "themes" + File.separatorChar + Commons.site_theme();
        String filePath  = themePath + File.separatorChar + templateParam.getFileName();
        if (Files.exists(Paths.get(filePath))) {
            byte[] rf_wiki_byte = content.getBytes("UTF-8");
            Files.write(Paths.get(filePath), rf_wiki_byte);
        } else {
            Files.createFile(Paths.get(filePath));
            byte[] rf_wiki_byte = content.getBytes("UTF-8");
            Files.write(Paths.get(filePath), rf_wiki_byte);
        }
        return RestResponse.ok();
    }

}
