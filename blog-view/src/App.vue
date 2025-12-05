<template>
	<div id="app">
		<transition name="fade" mode="out-in">
			<router-view/>
		</transition>
	</div>
</template>

<script>
	export default {
		name: 'app'
	}
</script>

<style>
/* 页面切换淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
	transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter,
.fade-leave-to {
	opacity: 0;
	transform: translateY(10px);
}
</style>
