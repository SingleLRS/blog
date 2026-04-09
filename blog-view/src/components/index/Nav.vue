<template>
	<div ref="nav" class="ui fixed inverted stackable pointing menu" :class="{'transparent':$route.name==='home' && clientSize.clientWidth>768}">
		<div class="ui container">
			<router-link to="/">
				<h3 class="ui header item m-blue">{{ blogName }}</h3>
			</router-link>
			<router-link to="/home" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='home'}">
				<i class="home icon"></i>首页
			</router-link>
			<el-dropdown trigger="click" @command="categoryRoute">
				<span class="el-dropdown-link item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='category'}">
					<i class="idea icon"></i>分类<i class="caret down icon"></i>
				</span>
				<el-dropdown-menu slot="dropdown">
					<el-dropdown-item :command="category.name" v-for="(category,index) in categoryList" :key="index">{{ category.name }}</el-dropdown-item>
				</el-dropdown-menu>
			</el-dropdown>
			<router-link to="/archives" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='archives'}">
				<i class="clone icon"></i>归档
			</router-link>
			<router-link to="/moments" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='moments'}">
				<i class="comment alternate outline icon"></i>动态
			</router-link>
			<router-link to="/friends" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='friends'}">
				<i class="users icon"></i>友人帐
			</router-link>
			<router-link to="/about" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='about'}">
				<i class="info icon"></i>关于我
			</router-link>
			<el-autocomplete v-model="queryString" :fetch-suggestions="debounceQuery" placeholder="输入标题 / 摘要 / 正文关键词"
			                 :trigger-on-focus="false"
			                 class="right item m-search" :class="{'m-mobile-hide': mobileHide}"
			                 popper-class="m-search-item" @select="handleSelect" @keyup.enter.native="goSearchPage">
				<i class="search icon el-input__icon" slot="suffix"></i>
				<template slot-scope="{ item }">
					<div class="m-search-option">
						<div class="title" v-html="formatSearchPreview(item, 'title')"></div>
						<span class="content" v-html="formatSearchPreview(item, 'content')"></span>
					</div>
				</template>
			</el-autocomplete>
			<el-tooltip :content="isDark ? '切换为明亮模式' : '切换为暗黑模式'" placement="bottom">
				<a class="right item m-mobile-hide m-theme-toggle" @click.prevent="toggleTheme">
					<i :class="isDark ? 'sun outline icon' : 'moon icon'"></i>
				</a>
			</el-tooltip>
			<button class="ui menu black icon button m-right-top m-mobile-show" @click="toggle">
				<i class="sidebar icon"></i>
			</button>
		</div>
	</div>
</template>

<script>
	import {getSearchBlogResultList} from "@/api/blog";
	import {mapState} from 'vuex'

	export default {
		name: "Nav",
		props: {
			blogName: {
				type: String,
				required: true
			},
			categoryList: {
				type: Array,
				required: true
			},
		},
		data() {
			return {
				mobileHide: true,
				queryString: '',
				timer: null,
				isDark: false,
				handleSystemThemeChange: null,
				themeMediaQuery: null
			}
		},
		computed: {
			...mapState(['clientSize'])
		},
		watch: {
			'$route.path'() {
				this.mobileHide = true
			}
		},
		mounted() {
			const savedTheme = window.localStorage.getItem('nblog.theme')
			const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
			this.isDark = savedTheme ? savedTheme === 'dark' : prefersDark
			const mediaQuery = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null
			if (mediaQuery) {
				this.handleSystemThemeChange = event => {
					if (!window.localStorage.getItem('nblog.theme')) {
						this.isDark = event.matches
						document.documentElement.classList.toggle('theme-dark', event.matches)
					}
				}
				if (mediaQuery.addEventListener) {
					mediaQuery.addEventListener('change', this.handleSystemThemeChange)
				} else if (mediaQuery.addListener) {
					mediaQuery.addListener(this.handleSystemThemeChange)
				}
				this.themeMediaQuery = mediaQuery
			}
			window.addEventListener('scroll', () => {
				if (this.$route.name === 'home' && this.clientSize.clientWidth > 768) {
					if (window.scrollY > this.clientSize.clientHeight / 2) {
						this.$refs.nav.classList.remove('transparent')
					} else {
						this.$refs.nav.classList.add('transparent')
					}
				}
			})
			document.addEventListener('click', (e) => {
				let flag = this.$refs.nav.contains(e.target)
				if (!this.mobileHide && !flag) {
					this.mobileHide = true
				}
			})
		},
		beforeDestroy() {
			if (this.themeMediaQuery && this.handleSystemThemeChange) {
				if (this.themeMediaQuery.removeEventListener) {
					this.themeMediaQuery.removeEventListener('change', this.handleSystemThemeChange)
				} else if (this.themeMediaQuery.removeListener) {
					this.themeMediaQuery.removeListener(this.handleSystemThemeChange)
				}
			}
		},
		methods: {
			toggle() {
				this.mobileHide = !this.mobileHide
			},
			toggleTheme() {
				this.isDark = !this.isDark
				window.localStorage.setItem('nblog.theme', this.isDark ? 'dark' : 'light')
				document.documentElement.classList.toggle('theme-dark', this.isDark)
			},
			categoryRoute(name) {
				this.$router.push(`/category/${name}`)
			},
			debounceQuery(queryString, callback) {
				this.timer && clearTimeout(this.timer)
				if (queryString == null || queryString.trim() === '') {
					callback([])
					return
				}
				this.timer = setTimeout(() => this.querySearchAsync(queryString, callback), 300)
			},
			querySearchAsync(queryString, callback) {
				if (queryString == null
						|| queryString.trim() === ''
						|| queryString.indexOf('%') !== -1
						|| queryString.indexOf('_') !== -1
						|| queryString.indexOf('[') !== -1
						|| queryString.indexOf('#') !== -1
						|| queryString.indexOf('*') !== -1
						|| queryString.trim().length > 20) {
					callback([])
					return
				}
				getSearchBlogResultList(queryString, 1, false).then(res => {
					if (res.code === 200) {
						const list = ((res.data && res.data.list) || []).slice(0, 10)
						callback(list.length ? list : [{title: '无相关搜索结果', content: '', description: ''}])
					}
				}).catch(() => {
					callback([])
					this.msgError("请求失败")
				})
			},
			goSearchPage() {
				const query = this.queryString == null ? '' : this.queryString.trim()
				if (query === ''
						|| query.indexOf('%') !== -1
						|| query.indexOf('_') !== -1
						|| query.indexOf('[') !== -1
						|| query.indexOf('#') !== -1
						|| query.indexOf('*') !== -1
						|| query.length > 20) {
					return
				}
				this.$router.push({path: '/search', query: {query, pageNum: 1}})
			},
			handleSelect(item) {
				if (item.id) {
					this.$router.push(`/blog/${item.id}`)
				}
			},
			escapeHtml(text) {
				return String(text == null ? '' : text)
					.replace(/&/g, '&amp;')
					.replace(/</g, '&lt;')
					.replace(/>/g, '&gt;')
					.replace(/"/g, '&quot;')
					.replace(/'/g, '&#39;')
			},
			escapeRegExp(text) {
				return String(text == null ? '' : text).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
			},
			getSearchPreviewText(text, query, maxLength = 80) {
				const source = String(text == null ? '' : text).replace(/\s+/g, ' ').trim()
				if (!source) {
					return ''
				}
				const normalizedQuery = String(query == null ? '' : query).trim()
				if (!normalizedQuery) {
					return source.length > maxLength ? `${source.slice(0, maxLength)}...` : source
				}
				const index = source.toLowerCase().indexOf(normalizedQuery.toLowerCase())
				if (index === -1) {
					return source.length > maxLength ? `${source.slice(0, maxLength)}...` : source
				}
				const queryLength = normalizedQuery.length
				const half = Math.floor((maxLength - queryLength) / 2)
				const start = Math.max(index - half, 0)
				const end = Math.min(source.length, start + maxLength)
				const adjustedStart = Math.max(0, end - maxLength)
				const prefix = adjustedStart > 0 ? '...' : ''
				const suffix = end < source.length ? '...' : ''
				return `${prefix}${source.slice(adjustedStart, end)}${suffix}`
			},
			highlightSearchText(text) {
				const safeText = this.escapeHtml(text)
				const query = (this.queryString || '').trim()
				if (!query) {
					return safeText
				}
				const pattern = new RegExp(`(${this.escapeRegExp(query)})`, 'ig')
				return safeText.replace(pattern, '<mark class="m-search-highlight">$1</mark>')
			},
			formatSearchPreview(item, field) {
				if (!item || item.title === '无相关搜索结果') {
					return field === 'title' ? this.escapeHtml(item && item.title ? item.title : '') : ''
				}
				const query = (this.queryString || '').trim()
				if (field === 'title') {
					return this.highlightSearchText(this.getSearchPreviewText(item.title, query, 36))
				}
				const descriptionPreview = this.getSearchPreviewText(item.description || '', query, 88)
				const contentPreview = this.getSearchPreviewText(item.content || '', query, 88)
				const normalizedQuery = query.toLowerCase()
				const descriptionMatched = normalizedQuery && descriptionPreview.toLowerCase().includes(normalizedQuery)
				const contentMatched = normalizedQuery && contentPreview.toLowerCase().includes(normalizedQuery)
				const previewText = descriptionMatched || !contentMatched ? descriptionPreview : contentPreview
				return this.highlightSearchText(previewText)
			}
		}
	}
</script>

<style>
	.ui.fixed.menu .container {
		width: 1400px !important;
		margin-left: auto !important;
		margin-right: auto !important;
	}

	.ui.fixed.menu {
		transition: .3s ease-out;
	}

	.ui.inverted.pointing.menu.transparent {
		background: transparent !important;
	}

	.ui.inverted.pointing.menu.transparent .active.item:after {
		background: transparent !important;
		transition: .3s ease-out;
	}

	.ui.inverted.pointing.menu.transparent .active.item:hover:after {
		background: transparent !important;
	}

	.el-dropdown-link {
		outline-style: none !important;
		outline-color: unset !important;
		height: 100%;
		cursor: pointer;
	}

	.el-dropdown-menu {
		margin: 7px 0 0 0 !important;
		padding: 0 !important;
		border: 0 !important;
		background: #1b1c1d !important;
	}

	.el-dropdown-menu__item {
		padding: 0 15px !important;
		color: rgba(255, 255, 255, .9) !important;
	}

	.el-dropdown-menu__item:hover {
		background: rgba(255, 255, 255, .08) !important;
	}

	.el-popper .popper__arrow::after {
		content: none !important;
	}

	.popper__arrow {
		display: none !important;
	}

	.m-search {
		min-width: 220px;
		padding: 0 !important;
	}

	.m-search input {
		color: rgba(255, 255, 255, .9);
		border: 0px !important;
		background-color: inherit;
		padding: .67857143em 2.1em .67857143em 1em;
	}

	.m-search i {
		color: rgba(255, 255, 255, .9) !important;
	}

	.m-search-item {
		min-width: 350px !important;
		background: #1f2937 !important;
		border: 1px solid rgba(255, 255, 255, 0.08) !important;
	}

	.m-search-item .m-search-option {
		display: flex;
		flex-direction: column;
		gap: 6px;
		padding: 4px 0;
	}

	.m-search-item li {
		padding: 10px 14px !important;
		border-bottom: 1px solid rgba(255, 255, 255, 0.08);
		background: transparent !important;
	}

	.m-search-item li:last-child {
		border-bottom: 0;
	}

	.m-search-item li:hover {
		background: rgba(255, 255, 255, 0.06) !important;
	}

	.m-search-item .title {
		font-size: 14px;
		font-weight: 600;
		line-height: 1.4;
		color: rgba(255, 255, 255, 0.95);
	}

	.m-search-item .content {
		display: block;
		font-size: 12px;
		line-height: 1.6;
		color: rgba(255, 255, 255, 0.7);
		white-space: normal;
		word-break: break-all;
	}

	.m-search-highlight {
		padding: 0;
		background: rgba(168, 85, 247, 0.3);
		color: #f5edff;
		border-radius: 2px;
	}

	:root:not(.theme-dark) .m-search-item {
		background: #ffffff !important;
		border: 1px solid rgba(15, 23, 42, 0.08) !important;
		box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
	}

	:root:not(.theme-dark) .m-search-item li {
		border-bottom: 1px solid rgba(15, 23, 42, 0.08);
	}

	:root:not(.theme-dark) .m-search-item li:hover {
		background: rgba(59, 130, 246, 0.06) !important;
	}

	:root:not(.theme-dark) .m-search-item .title {
		color: #111827;
	}

	:root:not(.theme-dark) .m-search-item .content {
		color: #6b7280;
	}

	:root:not(.theme-dark) .m-search-highlight {
		background: rgba(250, 204, 21, 0.45);
		color: #111827;
	}
</style>
