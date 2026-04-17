package top.naccl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.naccl.entity.Category;
import top.naccl.entity.Tag;
import top.naccl.mapper.BlogMapper;
import top.naccl.model.vo.BlogDetail;
import top.naccl.service.BlogService;
import top.naccl.service.CategoryService;
import top.naccl.service.SeoService;
import top.naccl.service.SiteSettingService;
import top.naccl.service.TagService;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeoServiceImpl implements SeoService {

	private static final String SITE_URL = "https://liuruis.cn";
	private static final String SITE_NAME = "Liuruis's Blog";
	private static final String SITE_DESCRIPTION = "技术博客，分享编程学习笔记、Spring、Java、数据库等技术文章";
	private static final Pattern BLOG_PATH_PATTERN = Pattern.compile("^/blog/(\\d+)");

	@Autowired
	private BlogService blogService;
	@Autowired
	private BlogMapper blogMapper;
	@Autowired
	private SiteSettingService siteSettingService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private TagService tagService;

	@Override
	public String generateSitemap() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

		// 静态页面
		appendUrl(sb, "/", "daily", "1.0", null);
		appendUrl(sb, "/home", "daily", "0.9", null);
		appendUrl(sb, "/archives", "weekly", "0.8", null);
		appendUrl(sb, "/moments", "weekly", "0.7", null);
		appendUrl(sb, "/friends", "monthly", "0.6", null);
		appendUrl(sb, "/about", "monthly", "0.6", null);

		// 分类页面（Redis 反序列化可能返回 LinkedHashMap，需要兼容处理）
		List<?> categories = categoryService.getCategoryNameList();
		for (Object c : categories) {
			String name = getNameFromObject(c);
			appendUrl(sb, "/category/" + encodeUrl(name), "weekly", "0.7", null);
		}

		// 标签页面
		List<?> tags = tagService.getTagListNotId();
		for (Object t : tags) {
			String name = getNameFromObject(t);
			appendUrl(sb, "/tag/" + encodeUrl(name), "weekly", "0.7", null);
		}

		// 博客文章页面
		List<Map<String, Object>> blogs = blogMapper.getSitemapBlogList();
		for (Map<String, Object> blog : blogs) {
			String lastmod = blog.get("lastmod") != null ? blog.get("lastmod").toString() : null;
			appendUrl(sb, "/blog/" + blog.get("id"), "monthly", "0.8", lastmod);
		}

		sb.append("</urlset>");
		return sb.toString();
	}

	@Override
	public String renderPage(String path) {
		if (path == null || path.isEmpty()) {
			path = "/";
		}

		// 去掉查询参数
		int queryIndex = path.indexOf('?');
		if (queryIndex > 0) {
			path = path.substring(0, queryIndex);
		}

		try {
			Matcher m = BLOG_PATH_PATTERN.matcher(path);
			if (m.find()) {
				Long blogId = Long.parseLong(m.group(1));
				return renderBlogPage(blogId);
			}
		} catch (Exception ignored) {
		}

		return renderHomePage();
	}

	/**
	 * 渲染博客文章页 —— 最核心的 SEO 页面
	 */
	private String renderBlogPage(Long blogId) {
		BlogDetail blog;
		try {
			blog = blogService.getBlogByIdAndIsPublished(blogId);
		} catch (Exception e) {
			return renderHomePage();
		}
		if (blog == null) {
			return renderHomePage();
		}

		// 密码保护的文章不输出正文
		boolean hasPassword = blog.getPassword() != null && !blog.getPassword().isEmpty();

		// 从 HTML 内容提取纯文本作为 description
		String description;
		if (hasPassword) {
			description = "此文章受密码保护";
		} else {
			String plainText = stripHtml(blog.getContent());
			description = plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText;
		}

		// 拼接关键词：分类 + 标签
		String keywords = "";
		if (blog.getCategory() != null) {
			keywords = blog.getCategory().getName();
		}
		if (blog.getTags() != null && !blog.getTags().isEmpty()) {
			String tagKeywords = blog.getTags().stream()
					.map(Tag::getName)
					.collect(Collectors.joining(","));
			keywords = keywords.isEmpty() ? tagKeywords : keywords + "," + tagKeywords;
		}

		// 页面标题
		String webTitleSuffix = siteSettingService.getWebTitleSuffix();
		String pageTitle = blog.getTitle() + (webTitleSuffix != null ? webTitleSuffix : "");

		// ISO 8601 日期
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		String datePublished = blog.getCreateTime() != null ? isoFormat.format(blog.getCreateTime()) : "";
		String dateModified = blog.getUpdateTime() != null ? isoFormat.format(blog.getUpdateTime()) : "";

		String categoryName = blog.getCategory() != null ? blog.getCategory().getName() : "";

		SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd");
		String displayDate = blog.getCreateTime() != null ? displayFormat.format(blog.getCreateTime()) : "";

		// JSON-LD 结构化数据
		String jsonLd = String.format(
				"{\"@context\":\"https://schema.org\",\"@type\":\"BlogPosting\"," +
				"\"headline\":\"%s\",\"description\":\"%s\"," +
				"\"datePublished\":\"%s\",\"dateModified\":\"%s\"," +
				"\"author\":{\"@type\":\"Person\",\"name\":\"liuruis\"}," +
				"\"publisher\":{\"@type\":\"Organization\",\"name\":\"%s\"}," +
				"\"url\":\"%s/blog/%d\"}",
				escapeJson(blog.getTitle()),
				escapeJson(description),
				datePublished, dateModified,
				SITE_NAME, SITE_URL, blogId);

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n");
		html.append("<meta charset=\"utf-8\">\n");
		html.append("<title>").append(esc(pageTitle)).append("</title>\n");
		html.append("<meta name=\"description\" content=\"").append(esc(description)).append("\">\n");
		html.append("<meta name=\"keywords\" content=\"").append(esc(keywords)).append("\">\n");
		html.append("<meta name=\"author\" content=\"liuruis\">\n");
		html.append("<meta name=\"robots\" content=\"index, follow\">\n");
		html.append("<link rel=\"canonical\" href=\"").append(SITE_URL).append("/blog/").append(blogId).append("\">\n");

		// Open Graph
		html.append("<meta property=\"og:type\" content=\"article\">\n");
		html.append("<meta property=\"og:title\" content=\"").append(esc(blog.getTitle())).append("\">\n");
		html.append("<meta property=\"og:description\" content=\"").append(esc(description)).append("\">\n");
		html.append("<meta property=\"og:url\" content=\"").append(SITE_URL).append("/blog/").append(blogId).append("\">\n");
		html.append("<meta property=\"og:site_name\" content=\"").append(esc(SITE_NAME)).append("\">\n");
		html.append("<meta property=\"article:published_time\" content=\"").append(datePublished).append("\">\n");
		html.append("<meta property=\"article:modified_time\" content=\"").append(dateModified).append("\">\n");
		if (!categoryName.isEmpty()) {
			html.append("<meta property=\"article:section\" content=\"").append(esc(categoryName)).append("\">\n");
		}
		if (blog.getTags() != null) {
			for (Tag tag : blog.getTags()) {
				html.append("<meta property=\"article:tag\" content=\"").append(esc(tag.getName())).append("\">\n");
			}
		}

		// JSON-LD
		html.append("<script type=\"application/ld+json\">").append(jsonLd).append("</script>\n");

		// 基础样式
		html.append("<style>");
		html.append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;");
		html.append("max-width:800px;margin:0 auto;padding:20px;line-height:1.8;color:#333}");
		html.append("h1{font-size:1.8em;margin-bottom:8px}");
		html.append(".meta{color:#666;font-size:14px;margin-bottom:16px}");
		html.append(".tags{margin-bottom:20px}");
		html.append(".tags a{display:inline-block;margin:2px 6px 2px 0;padding:2px 8px;");
		html.append("background:#f0f0f0;border-radius:3px;color:#e67e22;text-decoration:none;font-size:13px}");
		html.append(".content img{max-width:100%;height:auto}");
		html.append(".content pre{background:#f5f5f5;padding:12px;overflow-x:auto;border-radius:4px}");
		html.append(".content code{background:#f5f5f5;padding:2px 4px;border-radius:3px}");
		html.append(".content pre code{background:transparent;padding:0}");
		html.append(".content blockquote{border-left:4px solid #ddd;margin:0;padding:8px 16px;color:#666;background:#f9f9f9}");
		html.append("a{color:#409eff;text-decoration:none}");
		html.append("</style>\n");
		html.append("</head>\n<body>\n");

		html.append("<header>\n");
		html.append("<h1>").append(esc(blog.getTitle())).append("</h1>\n");
		html.append("<div class=\"meta\">");
		html.append("<span>liuruis</span> &middot; ");
		html.append("<time>").append(displayDate).append("</time>");
		if (!categoryName.isEmpty()) {
			html.append(" &middot; <span>").append(esc(categoryName)).append("</span>");
		}
		html.append(" &middot; <span>").append(blog.getViews()).append("次阅读</span>");
		html.append("</div>\n");

		if (blog.getTags() != null && !blog.getTags().isEmpty()) {
			html.append("<div class=\"tags\">");
			for (Tag tag : blog.getTags()) {
				html.append("<a href=\"").append(SITE_URL).append("/tag/")
						.append(encodeUrl(tag.getName())).append("\">")
						.append(esc(tag.getName())).append("</a>");
			}
			html.append("</div>\n");
		}
		html.append("</header>\n");

		html.append("<article class=\"content\">\n");
		if (hasPassword) {
			html.append("<p>此文章受密码保护，请访问原文查看完整内容。</p>\n");
		} else {
			html.append(blog.getContent()).append("\n");
		}
		html.append("</article>\n");

		html.append("</body>\n</html>");
		return html.toString();
	}

	/**
	 * 渲染首页/通用页面 —— 作为兜底
	 */
	private String renderHomePage() {
		String webTitleSuffix = siteSettingService.getWebTitleSuffix();
		String pageTitle = SITE_NAME + (webTitleSuffix != null ? webTitleSuffix : "");

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n");
		html.append("<meta charset=\"utf-8\">\n");
		html.append("<title>").append(esc(pageTitle)).append("</title>\n");
		html.append("<meta name=\"description\" content=\"").append(esc(SITE_DESCRIPTION)).append("\">\n");
		html.append("<meta name=\"keywords\" content=\"博客,技术博客,编程,Java,Spring,学习笔记,liuruis\">\n");
		html.append("<meta name=\"author\" content=\"liuruis\">\n");
		html.append("<meta name=\"robots\" content=\"index, follow\">\n");
		html.append("<link rel=\"canonical\" href=\"").append(SITE_URL).append("/\">\n");
		html.append("<meta property=\"og:type\" content=\"website\">\n");
		html.append("<meta property=\"og:title\" content=\"").append(esc(SITE_NAME)).append("\">\n");
		html.append("<meta property=\"og:description\" content=\"").append(esc(SITE_DESCRIPTION)).append("\">\n");
		html.append("<meta property=\"og:url\" content=\"").append(SITE_URL).append("/\">\n");
		html.append("<meta property=\"og:site_name\" content=\"").append(esc(SITE_NAME)).append("\">\n");
		html.append("<meta property=\"og:locale\" content=\"zh_CN\">\n");
		html.append("<style>");
		html.append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;");
		html.append("max-width:800px;margin:0 auto;padding:20px;line-height:1.8;color:#333}");
		html.append("h1{font-size:2em}");
		html.append("p{color:#666}");
		html.append("a{color:#409eff;text-decoration:none}");
		html.append(".links{margin-top:20px}");
		html.append(".links a{display:inline-block;margin:4px 8px 4px 0;padding:4px 12px;");
		html.append("background:#f0f0f0;border-radius:4px}");
		html.append("</style>\n");
		html.append("</head>\n<body>\n");
		html.append("<h1>").append(esc(SITE_NAME)).append("</h1>\n");
		html.append("<p>").append(esc(SITE_DESCRIPTION)).append("</p>\n");

		// 列出分类
		List<?> categories = categoryService.getCategoryNameList();
		if (!categories.isEmpty()) {
			html.append("<h2>分类</h2>\n<div class=\"links\">");
			for (Object c : categories) {
				String name = getNameFromObject(c);
				html.append("<a href=\"").append(SITE_URL).append("/category/")
						.append(encodeUrl(name)).append("\">")
						.append(esc(name)).append("</a>");
			}
			html.append("</div>\n");
		}

		// 列出标签
		List<?> tags = tagService.getTagListNotId();
		if (!tags.isEmpty()) {
			html.append("<h2>标签</h2>\n<div class=\"links\">");
			for (Object t : tags) {
				String name = getNameFromObject(t);
				html.append("<a href=\"").append(SITE_URL).append("/tag/")
						.append(encodeUrl(name)).append("\">")
						.append(esc(name)).append("</a>");
			}
			html.append("</div>\n");
		}

		html.append("</body>\n</html>");
		return html.toString();
	}

	// ==================== 工具方法 ====================

	private void appendUrl(StringBuilder sb, String path, String freq, String priority, String lastmod) {
		sb.append("  <url>\n");
		sb.append("    <loc>").append(SITE_URL).append(path).append("</loc>\n");
		if (lastmod != null && !lastmod.isEmpty()) {
			sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
		}
		sb.append("    <changefreq>").append(freq).append("</changefreq>\n");
		sb.append("    <priority>").append(priority).append("</priority>\n");
		sb.append("  </url>\n");
	}

	private String encodeUrl(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (Exception e) {
			return s;
		}
	}

	private String esc(String text) {
		if (text == null) return "";
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	private String escapeJson(String text) {
		if (text == null) return "";
		return text.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "")
				.replace("\t", "\\t");
	}

	private String stripHtml(String html) {
		if (html == null) return "";
		return html.replaceAll("<[^>]*>", "")
				.replaceAll("&nbsp;", " ")
				.replaceAll("&lt;", "<")
				.replaceAll("&gt;", ">")
				.replaceAll("&amp;", "&")
				.replaceAll("\\s+", " ")
				.trim();
	}

	/**
	 * 从 Category/Tag 对象或 Redis 反序列化的 LinkedHashMap 中提取 name 字段
	 */
	@SuppressWarnings("unchecked")
	private String getNameFromObject(Object obj) {
		if (obj instanceof Category) {
			return ((Category) obj).getName();
		}
		if (obj instanceof Tag) {
			return ((Tag) obj).getName();
		}
		if (obj instanceof Map) {
			Object name = ((Map<String, Object>) obj).get("name");
			if (name == null) {
				name = ((Map<String, Object>) obj).get("categoryName");
			}
			return name != null ? name.toString() : "";
		}
		return "";
	}
}
