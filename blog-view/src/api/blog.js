import axios from '@/plugins/axios'

export function getBlogById(token, id) {
	return axios({
		url: 'blog',
		method: 'GET',
		headers: {
			Authorization: token,
		},
		params: {
			id
		}
	})
}

export function checkBlogPassword(blogPasswordForm) {
	return axios({
		url: 'checkBlogPassword',
		method: 'POST',
		data: {
			...blogPasswordForm
		}
	})
}

export function getSearchBlogList(query) {
	return axios({
		url: 'searchBlog',
		method: 'GET',
		showProgress: false,
		params: {
			query
		}
	})
}

export function getSearchBlogResultList(query, pageNum) {
	return axios({
		url: 'searchBlogs',
		method: 'GET',
		params: {
			query,
			pageNum
		}
	})
}
