<template>
  <AppShellLayout>
    <div class="max-w-5xl mx-auto pb-10 px-4 w-full">
      <div v-if="loading" class="text-center py-20 text-slate-400">正在加载详情数据...</div>
      <div v-else-if="errorMessage" class="text-center py-20 text-rose-400">
        <p class="text-xl font-bold mb-2">获取详情失败</p>
        <p>{{ errorMessage }}</p>
      </div>

      <div v-else-if="detail">
        <div class="glass-panel rounded-3xl p-6 relative overflow-hidden mb-8">
          <div class="flex flex-wrap justify-between items-start mb-6">
            <div>
              <h2 class="text-3xl font-extrabold text-slate-100 mb-3">诊断报告</h2>
              <div class="flex flex-wrap gap-4 text-sm text-slate-400">
                <span>{{ formatDate(detail.createTime) }}</span>
                <span class="bg-sky-900/40 border border-sky-500/30 text-sky-300 px-2 py-0.5 rounded">
                  {{ (detail.dbType || 'unknown').toUpperCase() }}
                </span>
                <span>{{ detail.aiModel || '未知模型' }}</span>
              </div>
            </div>
            <div class="text-right mt-4 sm:mt-0 bg-sky-900/30 px-6 py-4 rounded-2xl border border-sky-500/30">
              <p class="text-xs text-sky-300 font-bold mb-1">AI 综合评分</p>
              <div class="text-4xl font-extrabold text-sky-300">{{ detail.score || '--' }}/100</div>
            </div>
          </div>

          <div class="mt-6 border-t border-slate-700/50 pt-6">
            <div class="flex justify-between items-center mb-3">
              <h3 class="text-sm font-bold text-slate-400">原始 SQL</h3>
              <button
                class="text-xs px-3 py-1.5 rounded-lg transition-all border"
                :class="copiedKey === 'userSql' ? 'text-emerald-400 border-emerald-500/50 bg-emerald-900/30' : 'text-sky-300 border-sky-500/30 bg-sky-900/30 hover:text-sky-200'"
                @click="copyText(detail.userSql || '', 'userSql')"
              >
                {{ copiedKey === 'userSql' ? '已复制' : '复制 SQL' }}
              </button>
            </div>
            <pre class="bg-slate-900/80 p-5 rounded-xl border border-slate-700 text-sm font-mono text-slate-300 overflow-auto max-h-[300px]">{{ detail.userSql || '--' }}</pre>
          </div>
        </div>

        <div class="glass-panel rounded-3xl p-6 border border-slate-700/50">
          <h2 class="text-2xl font-bold mb-6 text-slate-100">深度分析</h2>

          <template v-if="analysis.isModular">
            <div class="space-y-6">
              <div v-if="analysis.reasoningHtml" class="markdown-body text-sm bg-slate-900/50 p-5 rounded-2xl border border-slate-700/50" v-html="analysis.reasoningHtml" />
              <div v-if="analysis.scoreHtml" class="bg-slate-800/60 border border-slate-700 p-5 rounded-2xl">
                <h3 class="text-slate-200 font-bold mb-3">评分详情</h3>
                <div class="markdown-body text-sm" v-html="analysis.scoreHtml" />
              </div>
              <div v-if="analysis.analysisHtml" class="bg-rose-900/10 border border-rose-900/30 p-5 rounded-2xl">
                <h3 class="text-rose-400 font-bold mb-3">问题分析</h3>
                <div class="markdown-body text-sm text-rose-200" v-html="analysis.analysisHtml" />
              </div>
              <div v-if="analysis.suggestHtml" class="bg-emerald-900/10 border border-emerald-900/30 p-5 rounded-2xl">
                <h3 class="text-emerald-400 font-bold mb-3">优化建议</h3>
                <div class="markdown-body text-sm text-emerald-200" v-html="analysis.suggestHtml" />
              </div>
              <div v-if="analysis.sqlHtml" class="bg-slate-900 p-5 rounded-2xl border border-slate-800">
                <div class="flex justify-between items-center mb-3">
                  <h3 class="text-sky-300 font-bold">优化后 SQL</h3>
                  <button
                    class="text-xs px-3 py-1.5 rounded-lg transition-all border"
                    :class="copiedKey === 'optimizedSql' ? 'text-emerald-400 border-emerald-500/50 bg-emerald-900/30' : 'text-sky-300 border-sky-500/30 bg-sky-900/30 hover:text-sky-200'"
                    @click="copyText(analysis.sqlRaw, 'optimizedSql')"
                  >
                    {{ copiedKey === 'optimizedSql' ? '已复制' : '复制 SQL' }}
                  </button>
                </div>
                <div class="markdown-body text-sm overflow-auto max-h-[400px]" v-html="analysis.sqlHtml" />
              </div>
            </div>
          </template>

          <div v-else class="markdown-body text-sm text-slate-300 bg-slate-800/60 p-6 rounded-xl border border-slate-700" v-html="analysis.fallbackHtml" />
        </div>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api } from '@/lib/api';
import { buildSqlAnalysis, copyPlainText } from '@/lib/sql-report';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const loading = ref(true);
const errorMessage = ref('');
const detail = ref(null);
const analysis = ref({
  isModular: false,
  reasoningHtml: '',
  scoreHtml: '',
  analysisHtml: '',
  suggestHtml: '',
  sqlHtml: '',
  sqlRaw: '',
  fallbackHtml: ''
});
const copiedKey = ref('');

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : '--';
}

async function loadDetail() {
  const recordId = route.query.id;
  if (!recordId) {
    loading.value = false;
    errorMessage.value = '缺少记录 ID 参数';
    return;
  }

  try {
    const { response, data } = await api.getSqlHistoryDetail(recordId);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (!response.ok || !data) {
      throw new Error('未找到该记录');
    }
    detail.value = data;
    analysis.value = buildSqlAnalysis(data.aiAnalysis || '');
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function copyText(text, key) {
  const value = String(text || '');
  if (!value) {
    return;
  }

  try {
    await copyPlainText(value);
    copiedKey.value = key;
    window.setTimeout(() => {
      if (copiedKey.value === key) {
        copiedKey.value = '';
      }
    }, 2000);
  } catch (error) {
    errorMessage.value = '复制失败，请手动复制';
  }
}

onMounted(async () => {
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadDetail();
  }
});
</script>
