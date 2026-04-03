function normalizeBaseUrl(value) {
  return String(value || '').trim().replace(/\/+$/, '');
}

const API_BASE_URL = normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL);
const ASSET_BASE_URL = normalizeBaseUrl(import.meta.env.VITE_ASSET_BASE_URL || API_BASE_URL);

function buildAbsoluteUrl(path, baseUrl = '') {
  const value = String(path || '').trim();
  if (!value) {
    return '';
  }
  if (/^(?:[a-z]+:)?\/\//i.test(value) || value.startsWith('data:') || value.startsWith('blob:')) {
    return value;
  }
  if (!baseUrl) {
    return value;
  }
  return value.startsWith('/') ? `${baseUrl}${value}` : `${baseUrl}/${value}`;
}

async function parseResponseData(response) {
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }

  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch (error) {
    return text;
  }
}

async function request(url, options = {}) {
  const config = { ...options };
  const headers = { ...(config.headers || {}) };

  if (Object.prototype.hasOwnProperty.call(config, 'json')) {
    headers['Content-Type'] = 'application/json';
    config.body = JSON.stringify(config.json);
    delete config.json;
  }

  config.headers = headers;

  const response = await fetch(buildAbsoluteUrl(url, API_BASE_URL), config);
  const data = await parseResponseData(response);
  return { response, data };
}

export function getCurrentPath() {
  return window.location.hash ? window.location.hash.replace(/^#/, '') || '/' : '/';
}

export function resolveAssetUrl(path) {
  return buildAbsoluteUrl(path, ASSET_BASE_URL);
}

export const api = {
  async getCurrentUser() {
    const { data } = await request('/api/auth/me');
    return data;
  },
  async login(payload) {
    const { data } = await request('/api/auth/login', {
      method: 'POST',
      json: payload
    });
    return data;
  },
  async register(payload) {
    const { data } = await request('/api/auth/register', {
      method: 'POST',
      json: payload
    });
    return data;
  },
  logout() {
    return request('/api/auth/logout', { method: 'POST' });
  },
  optimizeSql(payload) {
    return request('/api/sql/optimize', {
      method: 'POST',
      json: payload
    });
  },
  getSqlHistory() {
    return request('/api/sql/history');
  },
  getSqlHistoryDetail(recordId) {
    return request(`/api/sql/history/${recordId}`);
  },
  uploadImage(formData) {
    return request('/api/upload/image', {
      method: 'POST',
      body: formData
    });
  },
  uploadAudio(formData) {
    return request('/api/upload/audio', {
      method: 'POST',
      body: formData
    });
  },
  uploadVideo(formData) {
    return request('/api/upload/video', {
      method: 'POST',
      body: formData
    });
  },
  generateImage(payload, localUrls = []) {
    const query = localUrls.length ? `?localUrls=${encodeURIComponent(localUrls.join(','))}` : '';
    return request(`/api/image/generate${query}`, {
      method: 'POST',
      json: payload
    });
  },
  getImageHistory() {
    return request('/api/image/history');
  },
  generateCamera(payload, params) {
    const queryParams = new URLSearchParams({
      localUrl: params.localUrl || '',
      azimuth: String(params.azimuth),
      elevation: String(params.elevation),
      distance: String(params.distance)
    });
    return request(`/api/camera/generate?${queryParams.toString()}`, {
      method: 'POST',
      json: payload
    });
  },
  getCameraHistory() {
    return request('/api/camera/history');
  },
  generateVoice(payload) {
    return request('/api/voice/generate', {
      method: 'POST',
      json: payload
    });
  },
  generateVideo(payload) {
    return request('/api/video/generate', {
      method: 'POST',
      json: payload
    });
  },
  getVideoTask(taskId) {
    return request(`/api/video/task/${encodeURIComponent(taskId)}`);
  },
  getVideoHistory() {
    return request('/api/video/history');
  },
  getVoiceHistory() {
    return request('/api/voice/history');
  },
  generateMarketingCopy(payload) {
    return request('/api/marketing/copy', {
      method: 'POST',
      json: payload
    });
  },
  generateSlogan(payload) {
    return request('/api/marketing/slogan', {
      method: 'POST',
      json: payload
    });
  },
  getMarketingHistory(type = '') {
    const query = type ? `?type=${encodeURIComponent(type)}` : '';
    return request(`/api/marketing/history${query}`);
  },
  deleteMarketingHistory(recordId) {
    return request(`/api/marketing/history/${recordId}`, {
      method: 'DELETE'
    });
  }
};
