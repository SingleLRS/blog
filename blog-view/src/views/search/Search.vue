<template>
	<div>
		<div class="ui top segment m-search-header">
			<h2 class="m-text-500">搜索 “{{ query }}” 的结果</h2>
			<p class="m-search-summary" v-if="hasQuery && !isEmpty">共找到 {{ resultCountText }} 页结果</p>
		</div>
		<SearchResultList v-if="hasQuery && !isEmpty" :blogList="blogList" :getBlogList="getBlogList" :totalPage="totalPage"/>
		<div v-else class="ui attached segment m-box m-search-empty">
			<div class="ui icon message">
				<i class="search icon"></i>
				<div class="content">
					<div class="header">{{ emptyTitle }}</div>
					<p>{{ emptyDescription }}</p>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	import {getSearchBlogResultList} from '@/api/blog'
	import SearchResultList from '@/components/search/SearchResultList'

	export default {
		name: 'Search',
		components: {SearchResultList},
		data() {
			return {
				blogList: [],
				totalPage: 0
			}
		},
		watch: {
			'$route.fullPath'() {
				if (this.$route.name === 'search') {
					this.getBlogList(this.pageNum)
				}
			}
		},
		created() {
			this.getBlogList(this.pageNum)
		},
		computed: {
			query() {
				return (this.$route.query.query || '').trim()
			},
			pageNum() {
				const pageNum = parseInt(this.$route.query.pageNum || '1', 10)
				return Number.isNaN(pageNum) || pageNum < 1 ? 1 : pageNum
			},
			hasQuery() {
				return this.query !== ''
			},
			isEmpty() {
				return !this.blogList.length
			},
			resultCountText() {
				return this.totalPage
			},
			emptyTitle() {
				return this.hasQuery ? `没有找到与 “${this.query}” 相关的文章` : '请输入关键词开始搜索'
			},
			emptyDescription() {
				return this.hasQuery ? '可以尝试更换关键词，或返回首页查看最新文章。' : '你可以在顶部导航栏输入标题、摘要或正文关键词。'
			}
		},
		methods: {
			getBlogList(pageNum = 1) {
				if (!this.hasQuery) {
					this.blogList = []
					this.totalPage = 0
					return
				}
				if (pageNum !== this.pageNum) {
					this.$router.push({path: '/search', query: {query: this.query, pageNum}})
					return
				}
				getSearchBlogResultList(this.query, pageNum).then(res => {
					if (res.code === 200) {
						this.blogList = res.data.list
						this.totalPage = res.data.totalPage
					} else {
						this.msgError(res.msg)
					}
				}).catch(() => {
					this.msgError('请求失败')
				})
			}
		}
	}
</script>

<style scoped>
	.m-search-header {
		text-align: center;
	}

	.m-search-summary {
		margin-top: 0.8rem;
		color: rgba(0, 0, 0, 0.6);
	}

	.m-search-empty {
		padding: 3rem 2rem;
	}
</style>
