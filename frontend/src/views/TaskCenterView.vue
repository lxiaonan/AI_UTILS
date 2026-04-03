<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.1fr_0.9fr] items-start">
          <div>
            <div class="studio-kicker">Task Center</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">统一任务中心</h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              聚合 SQL、图像、视频、语音、3D 和营销记录。现在营销文案与广告语也会进入统一任务流。
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
          <div class="grid gap-3 sm:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">任务总数</div>
              <div class="studio-metric-value">{{ items.length }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">营销记录</div>
              <div class="studio-metric-value">{{ marketingCount }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">最近类型</div>
              <div class="studio-metric-value">{{ filteredItems[0]?.typeLabel || '暂无' }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
        <div class="flex items-start justify-between gap-4 flex-wrap mb-6">
          <div>
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Unified Feed</div>
            <h2 class="mt-2 text-2xl font-bold text-slate-100">任务时间线</h2>
          </div>
          <button type="button" class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="loadTaskCenter">
            刷新
          </button>
        </div>

        <div v-if="loading" class="py-16 text-center text-slate-500">正在加载任务中心...</div>
        <div v-else-if="errorMessage" class="rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">{{ errorMessage }}</div>
        <div v-else-if="!filteredItems.length" class="studio-empty text-center">
          <div class="text-xl font-bold text-slate-100">当前筛选下没有任务</div>
          <div class="mt-2 text-sm text-slate-400">去任一工作台生成内容后，这里会自动出现聚合记录。</div>
        </div>

        <div v-else class="space-y-5">
          <article v-for="item in filteredItems" :key="item.key" class="rounded-[1.6rem] border border-slate-700/70 bg-slate-950/35 p-5">
            <div class="flex items-start justify-between gap-4 flex-wrap">
              <div class="flex items-start gap-4 min-w-0">
                <div class="w-14 h-14 rounded-2xl flex items-center justify-center text-sm font-black tracking-[0.2em]" :class="item.iconClass">
                  {{ item.shortLabel }}
                </div>
                <div class="min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="text-xs px-2.5 py-1 rounded-full border" :class="item.typeBadgeClass">{{ item.typeLabel }}</span>
                    <span class="text-xs px-2.5 py-1 rounded-full border" :class="statusPillClass(item.statusKind)">{{ item.statusLabel }}</span>
                    <span class="text-xs text-slate-500">{{ formatDate(item.createTime) }}</span>
                  </div>
                  <div class="mt-3 text-lg font-bold text-slate-100 break-words">{{ item.title }}</div>
                  <div class="mt-1 text-sm text-slate-400 break-words">{{ item.subtitle }}</div>
                  <div class="mt-3 text-sm text-slate-300 leading-7 break-words">{{ item.summary }}</div>
                </div>
              </div>
              <div class="text-xs text-slate-500">{{ item.metaLine }}</div>
            </div>

            <div class="mt-4 flex gap-3 flex-wrap">
              <button
                v-if="item.previewUrl"
                type="button"
                class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                @click="openAsset(item.previewUrl)"
              >
                查看结果
              </button>
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                @click="router.push(item.sourceRoute)"
              >
                前往{{ item.typeLabel }}
              </button>
            </div>
          </article>
        </div>
      </section>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api, resolveAssetUrl } from '@/lib/api';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const loading = ref(true);
const errorMessage = ref('');
const items = ref([]);
const activeType = ref('all');

const typeFilters = [
  { value: 'all', label: '全部' },
  { value: 'sql', label: 'SQL' },
  { value: 'image', label: '图像' },
  { value: 'marketing', label: '营销' },
  { value: 'video', label: '视频' },
  { value: 'voice', label: '语音' },
  { value: 'camera', label: '3D' }
];

const filteredItems = computed(() => items.value.filter((item) => activeType.value === 'all' || item.type === activeType.value));
const marketingCount = computed(() => items.value.filter((item) => item.type === 'marketing').length);

function formatDate(value) {
  return value ? new Date(value).toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '--';
}

function shortText(value, max = 96) {
  if (!value) return '暂无摘要';
  return value.length > max ? `${value.slice(0, max)}...` : value;
}

function safeJsonParse(value) {
  try {
    return JSON.parse(value);
  } catch (error) {
    return null;
  }
}

function statusPillClass(kind) {
  if (kind === 'failed') return 'text-rose-300 border-rose-500/30 bg-rose-900/20';
  if (kind === 'active') return 'text-sky-300 border-sky-500/30 bg-sky-900/20';
  return 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20';
}

function iconStyle(type) {
  const map = {
    sql: ['SQL', 'bg-cyan-500/12 text-cyan-200 ring-1 ring-cyan-400/20', 'text-cyan-300 border-cyan-500/30 bg-cyan-900/20'],
    image: ['IMG', 'bg-fuchsia-500/12 text-fuchsia-200 ring-1 ring-fuchsia-400/20', 'text-fuchsia-300 border-fuchsia-500/30 bg-fuchsia-900/20'],
    marketing: ['MKT', 'bg-violet-500/12 text-violet-200 ring-1 ring-violet-400/20', 'text-violet-300 border-violet-500/30 bg-violet-900/20'],
    video: ['VID', 'bg-sky-500/12 text-sky-200 ring-1 ring-sky-400/20', 'text-sky-300 border-sky-500/30 bg-sky-900/20'],
    voice: ['VOC', 'bg-amber-500/12 text-amber-100 ring-1 ring-amber-400/20', 'text-amber-300 border-amber-500/30 bg-amber-900/20'],
    camera: ['3D', 'bg-emerald-500/12 text-emerald-100 ring-1 ring-emerald-400/20', 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20']
  };
  return map[type] || ['AI', 'bg-slate-500/12 text-slate-100', 'text-slate-300 border-slate-500/30 bg-slate-900/20'];
}

function buildSqlItems(records = []) {
  return records.map((item) => {
    const [shortLabel, iconClass, typeBadgeClass] = iconStyle('sql');
    return {
      key: `sql-${item.id}`,
      type: 'sql',
      typeLabel: 'SQL',
      shortLabel,
      iconClass,
      typeBadgeClass,
      statusKind: 'completed',
      statusLabel: `评分 ${item.score ?? '--'}`,
      createTime: item.createTime,
      title: shortText(item.userSql, 72),
      subtitle: `${item.dbType || '未知数据库'} / ${item.aiModel || '模型未记录'}`,
      summary: '已完成 SQL 诊断，可前往详情查看完整分析结果。',
      metaLine: `记录 ID: ${item.id}`,
      sourceRoute: { path: '/history' }
    };
  });
}

function buildImageItems(records = []) {
  return records.map((item) => {
    const parsed = safeJsonParse(item.userPrompt);
    const prompt = parsed?.input?.prompt || parsed?.input?.messages?.[0]?.content?.find?.((content) => content.text)?.text || item.userPrompt;
    const [shortLabel, iconClass, typeBadgeClass] = iconStyle('image');
    const previewUrl = item.imageUrls ? resolveAssetUrl(String(item.imageUrls).split(',')[0]) : '';
    return {
      key: `image-${item.id}`,
      type: 'image',
      typeLabel: '图像',
      shortLabel,
      iconClass,
      typeBadgeClass,
      statusKind: String(item.status || '').toUpperCase() === 'FAILED' ? 'failed' : 'completed',
      statusLabel: String(item.status || 'SUCCESS').toUpperCase(),
      createTime: item.createTime,
      title: shortText(prompt, 72),
      subtitle: item.aiModel || '图像模型',
      summary: previewUrl ? '已生成图片结果，可前往图像生成页继续调整。' : '本次记录没有可预览图片。',
      metaLine: `记录 ID: ${item.id}`,
      previewUrl,
      sourceRoute: { path: '/image' }
    };
  });
}

function buildMarketingItems(records = []) {
  return records.map((item) => {
    const resultPayload = safeJsonParse(item.resultPayload) || {};
    const [shortLabel, iconClass, typeBadgeClass] = iconStyle('marketing');
    const isCopy = item.recordType === 'marketing_copy';
    const titleHint = isCopy ? resultPayload?.xiaohongshu?.titles?.[0] : resultPayload?.slogan?.[0];
    return {
      key: `marketing-${item.id}`,
      type: 'marketing',
      typeLabel: isCopy ? '商品文案' : '广告语',
      shortLabel,
      iconClass,
      typeBadgeClass,
      statusKind: String(item.status || '').toUpperCase() === 'FAILED' ? 'failed' : 'completed',
      statusLabel: String(item.status || 'SUCCESS').toUpperCase(),
      createTime: item.createTime,
      title: shortText(item.productName || '营销任务', 72),
      subtitle: `${item.targets || '未标注目标'} / ${item.aiModel || 'qwen-plus'}`,
      summary: shortText(titleHint || '已生成营销结果，可前往对应页面或营销历史继续查看。', 96),
      metaLine: `记录 ID: ${item.id}`,
      sourceRoute: { path: isCopy ? '/marketing' : '/slogan' }
    };
  });
}

function buildVideoItems(records = []) {
  return records.map((item) => {
    const parsed = safeJsonParse(item.userPrompt);
    const [shortLabel, iconClass, typeBadgeClass] = iconStyle('video');
    return {
      key: `video-${item.id}`,
      type: 'video',
      typeLabel: '视频',
      shortLabel,
      iconClass,
      typeBadgeClass,
      statusKind: ['PENDING', 'RUNNING', 'PROCESSING'].includes(String(item.taskStatus || '').toUpperCase()) ? 'active' : 'completed',
      statusLabel: String(item.taskStatus || 'COMPLETED').toUpperCase(),
      createTime: item.createTime,
      title: shortText(parsed?.input?.prompt || item.userPrompt, 72),
      subtitle: item.aiModel || '视频模型',
      summary: item.videoUrl ? '视频结果已可播放。' : '视频任务已记录，可继续跟进状态。',
      metaLine: item.taskId ? `任务 ID: ${item.taskId}` : '无任务 ID',
      previewUrl: item.videoUrl || '',
      sourceRoute: { path: '/video' }
    };
  });
}

function buildVoiceItems(records = []) {
  return records.map((item) => {
    const [shortLabel, iconClass, typeBadgeClass] = iconStyle('voice');
    return {
      key: `voice-${item.id}`,
      type: 'voice',
      typeLabel: '语音',
      shortLabel,
      iconClass,
      typeBadgeClass,
      statusKind: item.audioUrl ? 'completed' : 'failed',
      statusLabel: item.audioUrl ? 'SUCCESS' : 'FAILED',
      createTime: item.createTime,
      title: shortText(item.userText, 72),
      subtitle: `${item.languageType || 'Chinese'} / ${item.voiceCode || 'Cherry'}`,
      summary: item.audioUrl ? '语音结果已生成。' : '语音结果缺失，请前往语音生成页重试。',
      metaLine: `记录 ID: ${item.id}`,
      previewUrl: item.audioUrl || '',
      sourceRoute: { path: '/voice' }
    };
  });
}

function buildCameraItems(records = []) {
  return records.map((item) => {
    const [shortLabel, iconClass, typeBadgeClass] = iconStyle('camera');
    return {
      key: `camera-${item.id}`,
      type: 'camera',
      typeLabel: '3D',
      shortLabel,
      iconClass,
      typeBadgeClass,
      statusKind: String(item.status || '').toUpperCase() === 'FAILED' ? 'failed' : 'completed',
      statusLabel: String(item.status || 'SUCCESS').toUpperCase(),
      createTime: item.createTime,
      title: shortText(item.promptUsed || '3D 控图任务', 72),
      subtitle: `${item.aiModel || '图像模型'} / 角度 ${item.azimuth ?? '--'}`,
      summary: item.generatedImageUrl ? '已生成 3D 结果图。' : '3D 任务已记录，可回到控图页继续调整。',
      metaLine: `distance ${item.distance ?? '--'}`,
      previewUrl: item.generatedImageUrl ? resolveAssetUrl(item.generatedImageUrl) : '',
      sourceRoute: { path: '/camera' }
    };
  });
}

async function loadTaskCenter() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const results = await Promise.all([
      api.getSqlHistory(),
      api.getImageHistory(),
      api.getMarketingHistory(),
      api.getVideoHistory(),
      api.getVoiceHistory(),
      api.getCameraHistory()
    ]);

    if (results.some(({ response }) => response.status === 401)) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }

    const [sqlResult, imageResult, marketingResult, videoResult, voiceResult, cameraResult] = results;
    items.value = [
      ...buildSqlItems(Array.isArray(sqlResult.data) ? sqlResult.data : []),
      ...buildImageItems(Array.isArray(imageResult.data) ? imageResult.data : []),
      ...buildMarketingItems(Array.isArray(marketingResult.data) ? marketingResult.data : []),
      ...buildVideoItems(Array.isArray(videoResult.data) ? videoResult.data : []),
      ...buildVoiceItems(Array.isArray(voiceResult.data) ? voiceResult.data : []),
      ...buildCameraItems(Array.isArray(cameraResult.data) ? cameraResult.data : [])
    ].sort((a, b) => new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime());
  } catch (error) {
    items.value = [];
    errorMessage.value = error.message || '任务中心加载失败';
  } finally {
    loading.value = false;
  }
}

function openAsset(url) {
  if (url) window.open(resolveAssetUrl(url), '_blank');
}

onMounted(async () => {
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadTaskCenter();
  }
});
</script>
