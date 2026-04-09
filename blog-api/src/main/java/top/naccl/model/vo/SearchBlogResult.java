package top.naccl.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @Description: 搜索结果页博客
 * @Author: liuruis
 * @Date: 2026-04-09
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SearchBlogResult {
	private Long id;
	private String title;
	private String description;
	private String content;
	private Date createTime;
	private String categoryName;
}
