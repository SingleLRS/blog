<template>
	<div>
		<div class="ui padded attached segment m-padded-tb-large m-margin-bottom-big m-box" v-for="item in blogList" :key="item.id">
			<h2 class="ui header m-margin-bottom-small">
				<a href="javascript:;" @click.prevent="toBlog(item)" class="m-black">{{ item.title }}</a>
			</h2>
			<div class="ui horizontal link list m-margin-bottom-small">
				<div class="item m-datetime">
					<i class="small calendar icon"></i>
					<span>{{ item.createTime | dateFormat('YYYY-MM-DD') }}</span>
				</div>
				<div class="item m-common-black" v-if="item.categoryName">
					<i class="small folder open icon"></i>
					<span>{{ item.categoryName }}</span>
				</div>
			</div>
			<p class="m-search-snippet">{{ item.description }}</p>
			<div class="m-margin-top">
				<a href="javascript:;" @click.prevent="toBlog(item)" class="color-btn">阅读全文</a>
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: 'SearchResultItem',
		props: {
			blogList: {
				type: Array,
				required: true
			}
		},
		methods: {
			toBlog(blog) {
				this.$store.dispatch('goBlogPage', blog)
			}
		}
	}
</script>

<style scoped>
	.m-search-snippet {
		line-height: 1.9;
		color: rgba(0, 0, 0, 0.7);
		white-space: pre-wrap;
	}
</style>
