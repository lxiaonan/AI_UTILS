<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.1fr_0.9fr] items-start">
          <div>
            <div class="studio-kicker">History</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">
              营销历史记录
            </h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              查看商品文案和广告语的生成记录，支持快速回看、复制和删除。
            </p>

            <div class="mt-6 flex flex-wrap gap-3">
              <button
                v-for="item in HISTORY_TYPE_OPTIONS"
                :key="item.value"
                type="button"
                class="voice-chip"
                :class="{ active: typeFilter === item.value }"
                @click="typeFilter = item.value"
              >
                {{ item.label }}
              </button>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">记录总数</div>
              <div class="studio-metric-value">{{ records.length }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">商品文案</div>
              <div class="studio-metric-value">{{ copyCount }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">广告语</div>
              <div class="studio-metric-value">{{ sloganCount }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        <section class="xl:col-span-5 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Timeline</div>
              <h2 class="mt-2 text-2xl font-bold text-slate-100">记录列表</h2>
            </div>
            <button
              type="button"
              class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
              @click="loadHistory"
            >
              刷新
            </button>
          </div>

          <div v-if="errorMessage" class="mt-5 rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
            {{ errorMessage }}
          </div>

          <div v-if="copyMessage" class="mt-5 rounded-2xl border border-emerald-800 bg-emerald-900/25 p-4 text-sm text-emerald-200">
            {{ copyMessage }}
          </div>

          <div class="mt-6 space-y-4">
            <div v-if="loading" class="py-12 text-center text-slate-500">正在加载历史记录...</div>
            <div v-else-if="!records.length" class="studio-empty text-center">
              <div class="text-xl font-bold text-slate-100">当前没有历史记录</div>
              <div class="mt-2 text-sm text-slate-400">生成成功后的营销内容会自动沉淀到这里。</div>
            </div>
            <article
              v-for="item in records"
              :key="item.id"
              class="studio-history-card cursor-pointer"
              :class="{ 'ring-1 ring-sky-400/35': selectedRecord?.id === item.id }"
              @click="selectedRecord = item"
            >
              <div class="flex items-start justify-between gap-4">
                <div>
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="text-[11px] px-2 py-1 rounded-full border border-slate-700 bg-slate-900/60 text-slate-300">
                      {{ item.recordType === 'marketing_copy' ? '商品文案' : '广告语' }}
                    </span>
                    <span class="text-xs text-slate-500">{{ formatDate(item.createTime) }}</span>
                  </div>
                  <div class="mt-3 text-lg font-bold text-slate-100 break-words">{{ item.productName }}</div>
                  <div class="mt-2 text-sm text-slate-400 break-words">{{ item.targets || '未标注目标类型' }}</div>
                </div>
                <div class="text-xs text-slate-500">{{ item.aiModel }}</div>
              </div>
            </article>
          </div>
        </section>

        <section class="xl:col-span-7 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Detail</div>
              <h2 class="mt-2 text-2xl font-bold text-slate-100">记录详情</h2>
            </div>

            <div v-if="selectedRecord" class="flex gap-3 flex-wrap">
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                @click="copySelected"
              >
                复制结果
              </button>
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-emerald-900/20 text-emerald-200 border border-emerald-500/30 hover:bg-emerald-900/35 transition"
                @click="reEditSelected"
              >
                继续编辑
              </button>
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-rose-900/20 text-rose-200 border border-rose-500/30 hover:bg-rose-900/35 transition"
                @click="deleteSelected"
              >
                删除记录
              </button>
            </div>
          </div>

          <div class="mt-6 studio-stage rounded-[1.7rem] p-5 md:p-6 min-h-[640px]">
            <div v-if="!selectedRecord" class="h-full flex items-center justify-center">
              <div class="studio-empty max-w-xl text-center">
                <div class="text-xl font-bold text-slate-100">选择一条记录查看详情</div>
                <div class="mt-2 text-sm text-slate-400">可以查看输入信息和结构化结果，并一键复制或删除。</div>
              </div>
            </div>

            <div v-else class="space-y-5">
              <div class="grid md:grid-cols-3 gap-4">
                <div class="studio-tip-card">
                  <div class="text-xs uppercase tracking-[0.22em] text-slate-500">类型</div>
                  <div class="mt-3 text-lg font-bold text-slate-100">{{ selectedRecord.recordType === 'marketing_copy' ? '商品文案' : '广告语' }}</div>
                </div>
                <div class="studio-tip-card">
                  <div class="text-xs uppercase tracking-[0.22em] text-slate-500">目标</div>
                  <div class="mt-3 text-lg font-bold text-slate-100 break-words">{{ selectedRecord.targets || '未标注' }}</div>
                </div>
                <div class="studio-tip-card">
                  <div class="text-xs uppercase tracking-[0.22em] text-slate-500">模型</div>
                  <div class="mt-3 text-lg font-bold text-slate-100 break-words">{{ selectedRecord.aiModel }}</div>
                </div>
              </div>

              <div class="rounded-[1.35rem] border border-slate-700/70 bg-slate-950/40 p-4">
                <div class="text-lg font-bold text-slate-100">输入参数</div>
                <pre class="mt-4 text-sm leading-7 text-slate-200 whitespace-pre-wrap overflow-auto">{{ formattedInput }}</pre>
              </div>

              <div class="rounded-[1.35rem] border border-slate-700/70 bg-slate-950/40 p-4">
                <div class="text-lg font-bold text-slate-100">生成结果</div>
                <pre class="mt-4 text-sm leading-7 text-slate-200 whitespace-pre-wrap overflow-auto">{{ formattedResult }}</pre>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import AppShellLayout from '@/components/AppShellLayout.vue';
import { api } from '@/lib/api';
import { copyText, HISTORY_TYPE_OPTIONS, safeParseJson } from '@/lib/marketing';
import { queueTemplateApplication } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const loading = ref(false);
const errorMessage = ref('');
const copyMessage = ref('');
const typeFilter = ref('');
const records = ref([]);
const selectedRecord = ref(null);

const copyCount = computed(() => records.value.filter((item) => item.recordType === 'marketing_copy').length);
const sloganCount = computed(() => records.value.filter((item) => item.recordType === 'slogan').length);
const formattedInput = computed(() => JSON.stringify(safeParseJson(selectedRecord.value?.inputPayload, {}), null, 2));
const formattedResult = computed(() => JSON.stringify(safeParseJson(selectedRecord.value?.resultPayload, {}), null, 2));

watch(typeFilter, () => {
  loadHistory();
});

function formatDate(value) {
  return value ? new Date(value).toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '--';
}

async function loadHistory() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const { response, data } = await api.getMarketingHistory(typeFilter.value);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (!response.ok) {
      throw new Error(data?.message || '历史记录加载失败');
    }

    records.value = Array.isArray(data) ? data : [];
    if (!records.value.length) {
      selectedRecord.value = null;
      return;
    }

    if (!selectedRecord.value) {
      selectedRecord.value = records.value[0];
      return;
    }

    selectedRecord.value = records.value.find((item) => item.id === selectedRecord.value.id) || records.value[0];
  } catch (error) {
    records.value = [];
    selectedRecord.value = null;
    errorMessage.value = error.message || '历史记录加载失败';
  } finally {
    loading.value = false;
  }
}

async function copySelected() {
  if (!selectedRecord.value) {
    return;
  }
  try {
    await copyText(formattedResult.value);
    copyMessage.value = '结果 JSON 已复制';
  } catch (error) {
    errorMessage.value = '复制失败，请检查浏览器剪贴板权限';
  }
}

async function deleteSelected() {
  if (!selectedRecord.value) {
    return;
  }

  try {
    const { response, data } = await api.deleteMarketingHistory(selectedRecord.value.id);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (!response.ok) {
      throw new Error(data?.message || '删除失败');
    }
    copyMessage.value = '记录已删除';
    await loadHistory();
  } catch (error) {
    errorMessage.value = error.message || '删除失败';
  }
}

async function reEditSelected() {
  if (!selectedRecord.value) {
    return;
  }

  const fields = safeParseJson(selectedRecord.value.inputPayload, {});
  const isMarketing = selectedRecord.value.recordType === 'marketing_copy';

  queueTemplateApplication({
    id: selectedRecord.value.id,
    title: selectedRecord.value.productName || 'History record',
    module: isMarketing ? 'marketing' : 'slogan',
    fields
  });

  copyMessage.value = 'Loaded into editor';
  errorMessage.value = '';
  await router.push(isMarketing ? '/marketing' : '/slogan');
}

onMounted(async () => {
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadHistory();
  }
});
</script>
