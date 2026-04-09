<template>
	<div>
		<SearchResultItem :blogList="blogList"/>
		<Pagination v-if="blogList.length" :getBlogList="getBlogList" :totalPage="totalPage"/>
	</div>
</template>

<script>
	import Pagination from '@/components/blog/Pagination'
	import SearchResultItem from '@/components/search/SearchResultItem'

	export default {
		name: 'SearchResultList',
		components: {Pagination, SearchResultItem},
		props: {
			getBlogList: {
				type: Function,
				required: true
			},
			blogList: {
				type: Array,
				required: true
			},
			totalPage: {
				type: Number,
				required: true
			}
		}
	}
</script>
