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
	transition: opacity 0.22s ease, transform 0.22s ease;
	will-change: opacity, transform;
}

.fade-enter,
.fade-leave-to {
	opacity: 0;
	transform: translateY(6px);
}
</style>
