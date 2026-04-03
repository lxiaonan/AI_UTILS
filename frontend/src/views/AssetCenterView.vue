<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.08fr_0.92fr] items-start">
          <div>
            <div class="studio-kicker">素材中心</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">
              持久化素材库
            </h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              把图片、视频、语音和 3D 结果集中管理起来。支持快速筛选、预览、下载和回到原工作台继续复用。
            </p>

            <div class="mt-6 flex flex-wrap gap-3">
              <button
                v-for="item in typeFilters"
                :key="item.value"
                type="button"
                class="studio-chip transition"
                :class="activeType === item.value ? 'ring-1 ring-sky-400/40 bg-sky-500/15 text-sky-100' : ''"
                @click="activeType = item.value"
              >
                {{ item.label }}
              </button>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <div class="studio-metric">
              <div class="studio-metric-label">素材总数</div>
              <div class="studio-metric-value">{{ assetItems.length }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">本地保存</div>
              <div class="studio-metric-value">{{ localAssetCount }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">今日新增</div>
              <div class="studio-metric-value">{{ todayAssetCount }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">来源模块</div>
              <div class="studio-metric-value">{{ activeSourceCount }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        <section class="xl:col-span-8 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap mb-6">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Asset Feed</div>
              <h2 class="mt-2 text-2xl font-bold text-slate-100">可检索结果流</h2>
              <p class="mt-2 text-sm text-slate-400">
                这里只展示可直接复用的完成结果，处理中任务建议去任务中心查看。
              </p>
            </div>

            <div class="w-full lg:w-auto flex gap-3 flex-wrap">
              <div class="relative flex-1 min-w-[240px]">
                <input
                  v-model.trim="keyword"
                  type="text"
                  class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border placeholder-slate-500"
                  placeholder="搜索提示词、模型、来源模块或备注"
                >
              </div>
              <label class="rounded-2xl border border-slate-700/70 bg-slate-950/35 px-4 py-3 flex items-center gap-3 text-sm text-slate-200">
                <input v-model="localOnly" type="checkbox" class="h-4 w-4">
                仅看本地文件
              </label>
            </div>
          </div>

          <div v-if="loading" class="py-16 text-center text-slate-500">正在加载素材库...</div>
          <div v-else-if="errorMessage" class="rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
            {{ errorMessage }}
          </div>
          <div v-else-if="!filteredItems.length" class="studio-empty text-center">
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Empty</div>
            <div class="mt-3 text-xl font-bold text-slate-100">当前筛选条件下没有素材</div>
            <div class="mt-2 text-sm text-slate-400">
              可以切换分类、清空关键词，或者先去图像/视频/语音工作台生成一些内容。
            </div>
          </div>

          <div v-else class="space-y-6">
            <div class="grid gap-5 md:grid-cols-2">
              <article
                v-for="item in pagedItems"
                :key="item.key"
                class="rounded-[1.6rem] border border-slate-700/70 bg-slate-950/35 p-4 md:p-5 flex flex-col"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="min-w-0">
                    <div class="flex items-center gap-2 flex-wrap">
                      <span class="text-xs px-2.5 py-1 rounded-full border" :class="item.typeBadgeClass">{{ item.typeLabel }}</span>
                      <span class="text-xs px-2.5 py-1 rounded-full border" :class="item.storageBadgeClass">{{ item.storageLabel }}</span>
                      <span class="text-xs text-slate-500">{{ formatDate(item.createTime) }}</span>
                    </div>
                    <div class="mt-3 text-lg font-bold text-slate-100 break-words">{{ item.title }}</div>
                    <div class="mt-2 text-sm text-slate-400 break-words">{{ item.subtitle }}</div>
                  </div>
                  <div
                    class="w-12 h-12 rounded-2xl flex items-center justify-center text-xs font-black tracking-[0.2em] shrink-0"
                    :class="item.iconClass"
                  >
                    {{ item.shortLabel }}
                  </div>
                </div>

                <div class="mt-4 rounded-[1.35rem] border border-slate-700/70 bg-slate-950/55 overflow-hidden">
                  <img
                    v-if="item.previewType === 'image'"
                    :src="item.assetUrl"
                    class="w-full h-64 object-cover cursor-pointer"
                    alt=""
                    loading="lazy"
                    decoding="async"
                    @click="openAsset(item.assetUrl)"
                  >
                  <video
                    v-else-if="item.previewType === 'video'"
                    :src="item.assetUrl"
                    class="w-full h-64 object-cover bg-black"
                    controls
                    preload="none"
                  ></video>
                  <div v-else class="p-5">
                    <div class="rounded-[1.1rem] border border-slate-700/60 bg-slate-900/70 p-4">
                      <div class="flex items-center justify-between gap-3">
                        <div>
                          <div class="text-sm font-semibold text-slate-100">音频预览</div>
                          <div class="mt-1 text-xs text-slate-500">可直接试听并继续复用</div>
                        </div>
                        <span class="text-[11px] px-2 py-1 rounded-full border border-amber-500/30 bg-amber-900/20 text-amber-300">
                          TTS
                        </span>
                      </div>
                      <audio :src="item.assetUrl" controls preload="none" class="w-full mt-4"></audio>
                    </div>
                  </div>
                </div>

                <div class="mt-4 text-sm text-slate-300 leading-7 break-words">{{ item.description }}</div>

                <div class="mt-4 flex flex-wrap gap-2">
                  <span
                    v-for="tag in item.tags"
                    :key="`${item.key}-${tag}`"
                    class="text-[11px] px-2.5 py-1 rounded-full border border-slate-700 bg-slate-900/60 text-slate-300"
                  >
                    {{ tag }}
                  </span>
                </div>

                <div class="mt-5 pt-4 border-t border-slate-700/60 flex items-center justify-between gap-3 flex-wrap">
                  <div class="text-xs text-slate-500 break-all">{{ item.assetUrl }}</div>
                  <div class="flex gap-3 flex-wrap">
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                      @click="openAsset(item.assetUrl)"
                    >
                      打开
                    </button>
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                      @click="downloadAsset(item)"
                    >
                      下载
                    </button>
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                      @click="copyAssetUrl(item.assetUrl)"
                    >
                      复制链接
                    </button>
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                      @click="goToSource(item)"
                    >
                      回到来源页
                    </button>
                  </div>
                </div>
              </article>
            </div>

            <div class="app-pagination">
              <div class="app-pagination-summary">
                第 {{ currentPage }} / {{ totalPages }} 页，共 {{ filteredItems.length }} 条素材，每页 {{ pageSize }}
              </div>
              <div class="app-pagination-controls">
                <label class="app-pagination-size">
                  <span>每页</span>
                  <select v-model.number="pageSize" class="app-pagination-select">
                    <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}</option>
                  </select>
                </label>
                <button
                  type="button"
                  class="app-pagination-btn"
                  :class="{ 'is-disabled': currentPage === 1 }"
                  :disabled="currentPage === 1"
                  @click="setPage(currentPage - 1)"
                >
                  上一页
                </button>
                <template v-for="page in pageItems" :key="page">
                  <span v-if="typeof page !== 'number'" class="app-pagination-ellipsis">...</span>
                  <button
                    v-else
                    type="button"
                    class="app-pagination-btn"
                    :class="{ 'is-active': currentPage === page }"
                    @click="setPage(page)"
                  >
                    {{ page }}
                  </button>
                </template>
                <button
                  type="button"
                  class="app-pagination-btn"
                  :class="{ 'is-disabled': currentPage === totalPages }"
                  :disabled="currentPage === totalPages"
                  @click="setPage(currentPage + 1)"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>
        </section>

        <aside class="xl:col-span-4 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Breakdown</div>
            <h2 class="mt-2 text-2xl font-bold text-slate-100">素材分布</h2>
            <div class="mt-5 space-y-4">
              <div
                v-for="item in typeStats"
                :key="item.value"
                class="rounded-[1.35rem] border border-slate-700/70 bg-slate-950/35 p-4"
              >
                <div class="flex items-center justify-between gap-3">
                  <div class="text-sm font-semibold text-slate-100">{{ item.label }}</div>
                  <div class="text-sm text-slate-300">{{ item.count }}</div>
                </div>
                <div class="mt-3 h-2 rounded-full bg-slate-900/80 overflow-hidden">
                  <div class="h-full rounded-full bg-gradient-to-r from-sky-400 to-cyan-300" :style="{ width: item.width }"></div>
                </div>
              </div>
            </div>
          </section>

          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Highlights</div>
            <h2 class="mt-2 text-2xl font-bold text-slate-100">快速观察</h2>
            <div class="mt-5 grid gap-4">
              <div class="studio-tip-card">
                <div class="text-sm font-bold text-slate-100">最新素材</div>
                <div class="mt-2 text-sm text-slate-400">{{ latestAssetText }}</div>
              </div>
              <div class="studio-tip-card">
                <div class="text-sm font-bold text-slate-100">高频来源</div>
                <div class="mt-2 text-sm text-slate-400">{{ topSourceText }}</div>
              </div>
              <div class="studio-tip-card">
                <div class="text-sm font-bold text-slate-100">持久化状态</div>
                <div class="mt-2 text-sm text-slate-400">{{ persistenceText }}</div>
              </div>
            </div>
          </section>

          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Recent Stack</div>
            <h2 class="mt-2 text-2xl font-bold text-slate-100">最近交付</h2>
            <div class="mt-5 space-y-4">
              <article
                v-for="item in latestItems"
                :key="`latest-${item.key}`"
                class="rounded-[1.35rem] border border-slate-700/70 bg-slate-950/35 p-4"
              >
                <div class="flex items-center justify-between gap-3">
                  <div class="text-sm font-semibold text-slate-100">{{ item.typeLabel }}</div>
                  <div class="text-xs text-slate-500">{{ formatDate(item.createTime) }}</div>
                </div>
                <div class="mt-2 text-sm text-slate-300 break-words">{{ item.title }}</div>
                <button
                  type="button"
                  class="mt-3 text-sm text-sky-300 hover:text-sky-200 transition"
                  @click="openAsset(item.assetUrl)"
                >
                  打开素材
                </button>
              </article>
            </div>
          </section>
        </aside>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api, resolveAssetUrl } from '@/lib/api';
import { PAGE_SIZE_OPTIONS, buildPagination, clampPage, slicePage } from '@/lib/pagination';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const loading = ref(true);
const errorMessage = ref('');
const keyword = ref('');
const localOnly = ref(false);
const activeType = ref('all');
const currentPage = ref(1);
const assetItems = ref([]);
const pageSize = ref(5);
const pageSizeOptions = PAGE_SIZE_OPTIONS;

const typeFilters = [
  { value: 'all', label: '全部' },
  { value: 'image', label: '图片' },
  { value: 'video', label: '视频' },
  { value: 'audio', label: '语音' },
  { value: 'camera', label: '3D 结果' }
];

const filteredItems = computed(() => {
  const term = keyword.value.trim().toLowerCase();

  return assetItems.value.filter((item) => {
    if (activeType.value !== 'all' && item.type !== activeType.value) {
      return false;
    }

    if (localOnly.value && !item.isLocal) {
      return false;
    }

    if (!term) {
      return true;
    }

    return item.searchText.toLowerCase().includes(term);
  });
});

const totalPages = computed(() => Math.max(Math.ceil(filteredItems.value.length / pageSize.value), 1));
const pageItems = computed(() => buildPagination(currentPage.value, totalPages.value));
const pagedItems = computed(() => slicePage(filteredItems.value, currentPage.value, pageSize.value));
const localAssetCount = computed(() => assetItems.value.filter((item) => item.isLocal).length);
const todayAssetCount = computed(() => {
  const today = new Date();
  return assetItems.value.filter((item) => {
    const date = new Date(item.createTime || 0);
    return date.getFullYear() === today.getFullYear()
      && date.getMonth() === today.getMonth()
      && date.getDate() === today.getDate();
  }).length;
});
const activeSourceCount = computed(() => new Set(assetItems.value.map((item) => item.sourceLabel)).size);
const latestItems = computed(() => assetItems.value.slice(0, 5));
const latestAssetText = computed(() => {
  const item = assetItems.value[0];
  return item ? `${item.typeLabel}，来自${item.sourceLabel}` : '还没有已保存的素材。';
});
const topSourceText = computed(() => {
  const summary = assetItems.value.reduce((acc, item) => {
    acc[item.sourceLabel] = (acc[item.sourceLabel] || 0) + 1;
    return acc;
  }, {});

  const [source, count] = Object.entries(summary).sort((a, b) => b[1] - a[1])[0] || [];
  return source ? `${source} 当前累计 ${count} 条可复用素材。` : '等待第一批素材进入素材库。';
});
const persistenceText = computed(() => {
  if (!assetItems.value.length) {
    return '当你生成并保存结果后，这里会逐步沉淀成可复用的素材库。';
  }
  if (localAssetCount.value === assetItems.value.length) {
    return '当前素材都已经指向本地保存文件，适合长期复用。';
  }
  return `当前有 ${localAssetCount.value} / ${assetItems.value.length} 条素材已经转为本地地址。`;
});

const typeStats = computed(() => {
  const total = assetItems.value.length || 1;
  return typeFilters
    .filter((item) => item.value !== 'all')
    .map((item) => {
      const count = assetItems.value.filter((asset) => asset.type === item.value).length;
      return {
        ...item,
        count,
        width: `${Math.max((count / total) * 100, count ? 8 : 0)}%`
      };
    });
});

watch([keyword, localOnly, activeType, pageSize], () => {
  currentPage.value = 1;
});

watch(filteredItems, () => {
  currentPage.value = clampPage(currentPage.value, totalPages.value);
});

function formatDate(value) {
  return value ? new Date(value).toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '--';
}

function shortText(value, max = 96) {
  if (!value) return '暂无描述';
  return value.length > max ? `${value.slice(0, max)}...` : value;
}

function safeJsonParse(value) {
  try {
    return JSON.parse(value);
  } catch (error) {
    return null;
  }
}

function normalizeImagePrompt(value) {
  const parsed = safeJsonParse(value);
  return parsed?.input?.prompt
    || parsed?.input?.messages?.[0]?.content?.find?.((content) => content.text)?.text
    || value
    || '图像生成结果';
}

function normalizeVideoPrompt(value) {
  const parsed = safeJsonParse(value);
  return parsed?.input?.prompt || value || '视频生成结果';
}

function typeStyle(type) {
  const map = {
    image: ['IMG', '图片', 'bg-fuchsia-500/12 text-fuchsia-200 ring-1 ring-fuchsia-400/20', 'text-fuchsia-300 border-fuchsia-500/30 bg-fuchsia-900/20'],
    video: ['VID', '视频', 'bg-sky-500/12 text-sky-200 ring-1 ring-sky-400/20', 'text-sky-300 border-sky-500/30 bg-sky-900/20'],
    audio: ['VOC', '语音', 'bg-amber-500/12 text-amber-100 ring-1 ring-amber-400/20', 'text-amber-300 border-amber-500/30 bg-amber-900/20'],
    camera: ['3D', '3D 结果', 'bg-emerald-500/12 text-emerald-100 ring-1 ring-emerald-400/20', 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20']
  };

  return map[type] || ['AI', '素材', 'bg-slate-500/12 text-slate-100', 'text-slate-300 border-slate-500/30 bg-slate-900/20'];
}

function buildStorageMeta(assetUrl) {
  const isLocal = String(assetUrl || '').includes('/uploads/');
  return {
    isLocal,
    storageLabel: isLocal ? '本地文件' : '远程地址',
    storageBadgeClass: isLocal
      ? 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20'
      : 'text-slate-300 border-slate-500/30 bg-slate-900/20'
  };
}

function splitImageUrls(value) {
  return String(value || '')
    .split(',')
    .map((item) => resolveAssetUrl(item.trim()))
    .filter(Boolean);
}

function enrichSearchText(parts) {
  return parts.filter(Boolean).join(' ');
}

function buildImageAssets(records = []) {
  return records.flatMap((item) => {
    const prompt = normalizeImagePrompt(item.userPrompt);
    const imageUrls = splitImageUrls(item.imageUrls);

    return imageUrls.map((assetUrl, index) => {
      const [shortLabel, typeLabel, iconClass, typeBadgeClass] = typeStyle('image');
      const storage = buildStorageMeta(assetUrl);
      const subtitle = item.aiModel || '图像工作台';
      const description = imageUrls.length > 1
        ? `这是同一轮请求中的第 ${index + 1} 张结果图，可直接继续复用。`
        : '单张图像结果，已可直接复用。';

      return {
        key: `image-${item.id}-${index}`,
        type: 'image',
        typeLabel,
        shortLabel,
        iconClass,
        typeBadgeClass,
        ...storage,
        createTime: item.createTime,
        title: shortText(prompt, 72),
        subtitle,
        description,
        tags: [item.aiModel || 'image-model', `图像 ${index + 1}`],
        assetUrl,
        previewType: 'image',
        sourceLabel: '图像工作台',
        sourceRoute: { path: '/image' },
        model: item.aiModel || '',
        searchText: enrichSearchText([prompt, subtitle, description, item.aiModel])
      };
    });
  });
}

function buildVideoAssets(records = []) {
  return records
    .filter((item) => item.videoUrl)
    .map((item) => {
      const prompt = normalizeVideoPrompt(item.userPrompt);
      const assetUrl = resolveAssetUrl(item.videoUrl);
      const [shortLabel, typeLabel, iconClass, typeBadgeClass] = typeStyle('video');
      const storage = buildStorageMeta(assetUrl);
      const subtitle = item.aiModel || '视频工作台';
      const description = item.taskId ? `任务 ${item.taskId} 已完成，可直接播放或下载。` : '视频结果已完成，可直接播放。';

      return {
        key: `video-${item.id}`,
        type: 'video',
        typeLabel,
        shortLabel,
        iconClass,
        typeBadgeClass,
        ...storage,
        createTime: item.createTime,
        title: shortText(prompt, 72),
        subtitle,
        description,
        tags: [item.aiModel || 'video-model', item.taskStatus || 'SUCCEEDED'],
        assetUrl,
        previewType: 'video',
        sourceLabel: '视频工作台',
        sourceRoute: { path: '/video' },
        model: item.aiModel || '',
        searchText: enrichSearchText([prompt, subtitle, description, item.taskId, item.aiModel])
      };
    });
}

function buildVoiceAssets(records = []) {
  return records
    .filter((item) => item.audioUrl)
    .map((item) => {
      const assetUrl = resolveAssetUrl(item.audioUrl);
      const [shortLabel, typeLabel, iconClass, typeBadgeClass] = typeStyle('audio');
      const storage = buildStorageMeta(assetUrl);
      const subtitle = `${item.voiceCode || 'Cherry'} · ${item.languageType || 'Chinese'}`;
      const description = '语音结果已可直接试听，也可以继续用于配音、短视频或讲解内容。';

      return {
        key: `audio-${item.id}`,
        type: 'audio',
        typeLabel,
        shortLabel,
        iconClass,
        typeBadgeClass,
        ...storage,
        createTime: item.createTime,
        title: shortText(item.userText || '语音生成结果', 72),
        subtitle,
        description,
        tags: [item.aiModel || 'qwen3-tts-flash', item.voiceCode || 'voice', item.languageType || 'Chinese'],
        assetUrl,
        previewType: 'audio',
        sourceLabel: '语音工作台',
        sourceRoute: { path: '/voice' },
        model: item.aiModel || '',
        searchText: enrichSearchText([item.userText, subtitle, description, item.aiModel, item.voiceCode, item.languageType])
      };
    });
}

function buildCameraAssets(records = []) {
  return records
    .filter((item) => item.generatedImageUrl)
    .map((item) => {
      const assetUrl = resolveAssetUrl(item.generatedImageUrl);
      const [shortLabel, typeLabel, iconClass, typeBadgeClass] = typeStyle('camera');
      const storage = buildStorageMeta(assetUrl);
      const subtitle = `${item.aiModel || '3D 控图'} · 方位 ${item.azimuth ?? '--'} · 俯仰 ${item.elevation ?? '--'}`;
      const description = `镜头距离 ${item.distance ?? '--'}，已完成角度重建结果。`;

      return {
        key: `camera-${item.id}`,
        type: 'camera',
        typeLabel,
        shortLabel,
        iconClass,
        typeBadgeClass,
        ...storage,
        createTime: item.createTime,
        title: shortText(item.promptUsed || '3D 控图结果', 72),
        subtitle,
        description,
        tags: [item.aiModel || 'camera-model', `方位 ${item.azimuth ?? '--'}`, `俯仰 ${item.elevation ?? '--'}`],
        assetUrl,
        previewType: 'image',
        sourceLabel: '3D 控图',
        sourceRoute: { path: '/camera' },
        model: item.aiModel || '',
        searchText: enrichSearchText([item.promptUsed, subtitle, description, item.aiModel, item.azimuth, item.elevation])
      };
    });
}

async function loadAssetCenter() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const results = await Promise.all([
      api.getImageHistory(),
      api.getVideoHistory(),
      api.getVoiceHistory(),
      api.getCameraHistory()
    ]);

    if (results.some(({ response }) => response.status === 401)) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }

    const [imageResult, videoResult, voiceResult, cameraResult] = results;

    assetItems.value = [
      ...buildImageAssets(Array.isArray(imageResult.data) ? imageResult.data : []),
      ...buildVideoAssets(Array.isArray(videoResult.data) ? videoResult.data : []),
      ...buildVoiceAssets(Array.isArray(voiceResult.data) ? voiceResult.data : []),
      ...buildCameraAssets(Array.isArray(cameraResult.data) ? cameraResult.data : [])
    ].sort((a, b) => new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime());
  } catch (error) {
    errorMessage.value = error.message || '素材中心加载失败。';
    assetItems.value = [];
  } finally {
    loading.value = false;
  }
}

function openAsset(url) {
  if (url) {
    window.open(url, '_blank');
  }
}

function setPage(page) {
  currentPage.value = clampPage(page, totalPages.value);
}

function downloadAsset(item) {
  if (!item?.assetUrl) {
    return;
  }

  const link = document.createElement('a');
  link.href = item.assetUrl;
  link.target = '_blank';
  link.rel = 'noopener';
  link.download = `${item.type}-${item.key}`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

async function copyAssetUrl(url) {
  if (!url) {
    return;
  }

  try {
    await navigator.clipboard.writeText(url);
  } catch (error) {
    errorMessage.value = '复制链接失败，当前浏览器可能限制了剪贴板访问。';
  }
}

function goToSource(item) {
  if (item?.sourceRoute) {
    router.push(item.sourceRoute);
  }
}

onMounted(async () => {
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadAssetCenter();
  }
});
</script>
