import { reactive, readonly } from 'vue';
import { api } from '@/lib/api';

const state = reactive({
  loaded: false,
  loggedIn: false,
  username: '',
  aiUseCount: 0
});

function applyUser(data) {
  state.loaded = true;
  state.loggedIn = true;
  state.username = data.username || '';
  state.aiUseCount = data.aiUseCount ?? 0;
}

function clearUser() {
  state.loaded = true;
  state.loggedIn = false;
  state.username = '';
  state.aiUseCount = 0;
}

async function fetchCurrentUser() {
  try {
    const data = await api.getCurrentUser();
    if (!data || !data.success) {
      clearUser();
      return null;
    }
    applyUser(data);
    return data;
  } catch (error) {
    clearUser();
    return null;
  }
}

function rememberRedirect(path) {
  sessionStorage.setItem('redirectAfterLogin', path || '/');
}

function consumeRedirect() {
  const redirect = sessionStorage.getItem('redirectAfterLogin') || '/';
  sessionStorage.removeItem('redirectAfterLogin');
  return redirect;
}

async function ensureAuthenticated(router, redirectPath = '/') {
  const data = await fetchCurrentUser();
  if (data && data.success) {
    return data;
  }
  rememberRedirect(redirectPath);
  if (router) {
    await router.push('/login');
  }
  return null;
}

async function logout(router) {
  try {
    await api.logout();
  } catch (error) {
  } finally {
    clearUser();
    if (router) {
      await router.push('/login');
    }
  }
}

export function useAuthStore() {
  return {
    state: readonly(state),
    fetchCurrentUser,
    ensureAuthenticated,
    logout,
    rememberRedirect,
    consumeRedirect
  };
}
