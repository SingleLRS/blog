<template>
	<div :class="{'has-logo':showLogo}" class="sidebar-no-select">
		<logo v-if="showLogo" :collapse="isCollapse"/>
		<el-scrollbar wrap-class="scrollbar-wrapper">
			<el-menu
					:default-openeds="defaultOpeneds"
					:default-active="activeMenu"
					:collapse="isCollapse"
					:background-color="variables.menuBg"
					:text-color="variables.menuText"
					:unique-opened="false"
					:active-text-color="variables.menuActiveText"
					:collapse-transition="false"
					mode="vertical"
					@open="handleOpen"
					@close="handleClose"
			>
				<sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path"/>
			</el-menu>
		</el-scrollbar>
	</div>
</template>

<script>
	import {mapGetters} from 'vuex'
	import Logo from './Logo'
	import SidebarItem from './SidebarItem'
	import variables from '@/assets/styles/variables.scss'

	const SIDEBAR_OPENEDS_KEY = 'cms_sidebar_openeds'

	export default {
		components: {SidebarItem, Logo},
		data() {
			return {
				defaultOpeneds: this.getSavedOpeneds()
			}
		},
		computed: {
			...mapGetters([
				'sidebar'
			]),
			routes() {
				return this.$router.options.routes
			},
			activeMenu() {
				const route = this.$route
				const {meta, path} = route
				if (meta.activeMenu) {
					return meta.activeMenu
				}
				return path
			},
			showLogo() {
				return this.$store.state.settings.sidebarLogo
			},
			variables() {
				return variables
			},
			isCollapse() {
				return !this.sidebar.opened
			}
		},
		methods: {
			getSavedOpeneds() {
				const savedOpeneds = window.localStorage.getItem(SIDEBAR_OPENEDS_KEY)
				if (!savedOpeneds) {
					return []
				}
				try {
					const parsedOpeneds = JSON.parse(savedOpeneds)
					return Array.isArray(parsedOpeneds) ? parsedOpeneds : []
				} catch (e) {
					return []
				}
			},
			saveOpeneds(openeds) {
				window.localStorage.setItem(SIDEBAR_OPENEDS_KEY, JSON.stringify(openeds))
			},
			handleOpen(index) {
				if (!this.defaultOpeneds.includes(index)) {
					this.defaultOpeneds.push(index)
					this.saveOpeneds(this.defaultOpeneds)
				}
			},
			handleClose(index) {
				this.defaultOpeneds = this.defaultOpeneds.filter(item => item !== index)
				this.saveOpeneds(this.defaultOpeneds)
			}
		}
	}
</script>

<style scoped>
	.sidebar-no-select {
		user-select: none;
	}
</style>
