package top.naccl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.service.SeoService;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SeoController {

	@Autowired
	private SeoService seoService;

	/**
	 * 动态站点地图，包含所有已公开博客文章、分类、标签的 URL
	 */
	@GetMapping(value = "/seo/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
	public String sitemap() {
		return seoService.generateSitemap();
	}

	/**
	 * 为搜索引擎爬虫渲染服务端 HTML 页面
	 * Nginx 通过 X-Original-URI 头传递原始请求路径
	 */
	@GetMapping(value = "/seo/render", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
	public String render(@RequestParam(value = "url", required = false) String url,
	                     @RequestHeader(value = "X-Original-URI", required = false) String originalUri,
	                     HttpServletRequest request) {
		String path = url;
		if (path == null || path.isEmpty()) {
			path = originalUri;
		}
		if (path == null || path.isEmpty()) {
			path = "/";
		}
		return seoService.renderPage(path);
	}
}
