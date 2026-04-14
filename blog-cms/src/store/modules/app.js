const SIDEBAR_OPENED_KEY = 'cms_sidebar_opened'

const getDefaultSidebarOpened = () => {
  const savedSidebarOpened = window.localStorage.getItem(SIDEBAR_OPENED_KEY)
  if (savedSidebarOpened === null) {
    return true
  }
  return savedSidebarOpened === 'true'
}

const state = {
  sidebar: {
    opened: getDefaultSidebarOpened(),
    withoutAnimation: false
  },
  device: 'desktop'
}

const mutations = {
  TOGGLE_SIDEBAR: state => {
    state.sidebar.opened = !state.sidebar.opened
    window.localStorage.setItem(SIDEBAR_OPENED_KEY, state.sidebar.opened)
    state.sidebar.withoutAnimation = false
  },
  CLOSE_SIDEBAR: (state, withoutAnimation) => {
    state.sidebar.opened = false
    window.localStorage.setItem(SIDEBAR_OPENED_KEY, false)
    state.sidebar.withoutAnimation = withoutAnimation
  },
  RESET_SIDEBAR: state => {
    state.sidebar.opened = true
    window.localStorage.removeItem(SIDEBAR_OPENED_KEY)
    state.sidebar.withoutAnimation = false
  },
  TOGGLE_DEVICE: (state, device) => {
    state.device = device
  }
}

const actions = {
  toggleSideBar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  },
  closeSideBar({ commit }, { withoutAnimation }) {
    commit('CLOSE_SIDEBAR', withoutAnimation)
  },
  resetSideBar({ commit }) {
    commit('RESET_SIDEBAR')
  },
  toggleDevice({ commit }, device) {
    commit('TOGGLE_DEVICE', device)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
