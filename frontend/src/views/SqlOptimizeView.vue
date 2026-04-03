<template>
  <AppShellLayout>
    <div class="max-w-7xl mx-auto px-4 pb-16 w-full">
      <section class="mb-8 grid gap-6 xl:grid-cols-[minmax(0,1.28fr)_minmax(320px,0.72fr)]">
        <div class="studio-hero rounded-[2.4rem] p-8 lg:p-10 overflow-hidden">
          <div class="studio-kicker">SQL Copilot Workspace</div>

          <div class="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_280px] lg:items-start">
            <div>
              <h1 class="text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">
                让慢 SQL 更快，也更规范
              </h1>
              <p class="mt-5 max-w-3xl text-lg text-slate-300/88 leading-8">
                结合 AI 分析与规则引擎，对 SQL 的性能、可维护性和规范性做结构化诊断，并直接给出可复制的优化建议与改写结果。
              </p>

              <div class="mt-8 flex flex-wrap gap-3">
                <div v-for="metric in heroMetrics" :key="metric.label" class="home-hero-chip">
                  <span class="home-hero-chip-label">{{ metric.label }}</span>
                  <span class="home-hero-chip-value">{{ metric.value }}</span>
                </div>
              </div>
            </div>

            <div class="grid gap-3">
              <div v-for="insight in heroInsights" :key="insight.title" class="home-insight-card">
                <div class="home-insight-title">{{ insight.title }}</div>
                <div class="home-insight-text">{{ insight.text }}</div>
              </div>
            </div>
          </div>
        </div>

        <aside class="grid gap-4 sm:grid-cols-2 xl:grid-cols-1">
          <div class="studio-metric">
            <div class="studio-metric-label">支持数据库</div>
            <div class="studio-metric-value">MySQL / PostgreSQL / ClickHouse / StarRocks</div>
          </div>
          <div class="studio-metric">
            <div class="studio-metric-label">分析结果</div>
            <div class="studio-metric-value">评分、问题清单、优化建议、改写 SQL 一次生成</div>
          </div>
          <div class="studio-metric">
            <div class="studio-metric-label">适用场景</div>
            <div class="studio-metric-value">慢查询排查、规范校验、索引建议与复杂 SQL 重写</div>
          </div>
        </aside>
      </section>

      <section class="grid grid-cols-1 gap-8 xl:grid-cols-[minmax(0,0.96fr)_minmax(0,1.04fr)]">
        <div class="glass-panel home-panel rounded-[2rem] p-6 lg:p-7">
          <div class="home-panel-head">
            <div>
              <div class="home-panel-kicker">Input</div>
              <h2 class="home-panel-title">输入待优化的 SQL</h2>
            </div>
            <div class="home-panel-note">支持直接粘贴复杂查询，推荐附带完整条件与排序逻辑。</div>
          </div>

          <div class="grid gap-5 md:grid-cols-2">
            <label class="home-field">
              <span class="home-field-label">AI 模型</span>
              <select
                v-model="form.model"
                class="home-input neon-border"
              >
                <option v-for="option in modelOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>

            <label class="home-field">
              <span class="home-field-label">数据库类型</span>
              <select
                v-model="form.dbType"
                class="home-input neon-border"
              >
                <option v-for="option in dbOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
          </div>

          <label class="home-field mt-5 block">
            <span class="home-field-label">SQL 语句</span>
            <div class="home-textarea-wrap neon-border">
              <textarea
                v-model="form.sql"
                rows="13"
                class="home-textarea"
                placeholder="SELECT * FROM orders WHERE user_id = 123;"
              />
            </div>
          </label>

          <div class="mt-4 flex flex-wrap gap-3">
            <span class="studio-chip">自动识别常见慢 SQL 风险</span>
            <span class="studio-chip">适合排查索引、扫描范围、SELECT *</span>
          </div>

          <div v-if="requestError" class="mt-5 rounded-2xl border border-rose-500/25 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
            {{ requestError }}
          </div>

          <div class="home-form-footer">
            <p class="home-form-tip">
              分析完成后会自动刷新当前账号的 AI 次数，并保留历史记录供后续回看。
            </p>
            <button
              class="home-submit-btn btn-glow"
              :class="{ 'opacity-75 cursor-not-allowed': loading }"
              :disabled="loading"
              @click="optimizeSql"
            >
              {{ loading ? '正在优化...' : '开始优化' }}
            </button>
          </div>
        </div>

        <div class="glass-panel home-panel rounded-[2rem] p-6 lg:p-7 flex flex-col min-h-[42rem]">
          <div class="home-panel-head">
            <div>
              <div class="home-panel-kicker">Report</div>
              <h2 class="home-panel-title">优化分析报告</h2>
            </div>
            <div class="home-panel-note">报告会按评分、问题、建议和最终 SQL 结构化展示。</div>
          </div>

          <template v-if="report">
            <div class="flex-grow overflow-auto pr-2 space-y-5">
              <div class="home-score-card">
                <div>
                  <h3 class="text-sm font-bold text-sky-200 mb-1">AI 综合评分</h3>
                  <p class="text-xs text-sky-100/65">基于性能、规范与可读性生成</p>
                </div>
                <div class="home-score-value">{{ report.score }}/100</div>
              </div>

              <div v-if="report.ruleWarnings.length" class="space-y-3">
                <h3 class="text-base font-bold text-rose-300">规则引擎预警</h3>
                <ul class="rounded-2xl border border-rose-500/18 bg-rose-950/20 p-4 text-sm text-rose-200 list-disc pl-6 space-y-2">
                  <li v-for="warning in report.ruleWarnings" :key="warning">{{ warning }}</li>
                </ul>
              </div>

              <template v-if="report.isModular">
                <div
                  v-if="report.reasoningHtml"
                  class="markdown-body rounded-2xl border border-slate-700/40 bg-slate-950/35 p-5 text-sm"
                  v-html="report.reasoningHtml"
                />

                <div v-if="report.scoreHtml" class="rounded-2xl border border-slate-700/55 bg-slate-900/55 p-5">
                  <h3 class="mb-3 font-bold text-slate-100">评分详情</h3>
                  <div class="markdown-body text-sm" v-html="report.scoreHtml" />
                </div>

                <div v-if="report.analysisHtml" class="rounded-2xl border border-rose-500/15 bg-rose-950/12 p-5">
                  <h3 class="mb-3 font-bold text-rose-300">问题分析</h3>
                  <div class="markdown-body text-sm text-rose-100/90" v-html="report.analysisHtml" />
                </div>

                <div v-if="report.suggestHtml" class="rounded-2xl border border-emerald-500/15 bg-emerald-950/12 p-5">
                  <h3 class="mb-3 font-bold text-emerald-300">优化建议</h3>
                  <div class="markdown-body text-sm text-emerald-100/90" v-html="report.suggestHtml" />
                </div>

                <div v-if="report.sqlHtml" class="rounded-2xl border border-slate-700/60 bg-slate-950/70 p-5">
                  <div class="mb-3 flex items-center justify-between gap-3">
                    <h3 class="font-bold text-sky-200">优化后的 SQL</h3>
                    <button
                      class="rounded-lg border px-3 py-1.5 text-xs transition-all"
                      :class="copiedKey === 'sql' ? 'border-emerald-500/45 bg-emerald-950/30 text-emerald-300' : 'border-sky-500/25 bg-sky-950/25 text-sky-200 hover:text-white'"
                      @click="copySql"
                    >
                      {{ copiedKey === 'sql' ? '已复制' : '复制 SQL' }}
                    </button>
                  </div>
                  <div class="markdown-body max-h-[420px] overflow-auto text-sm" v-html="report.sqlHtml" />
                </div>
              </template>

              <div
                v-else
                class="markdown-body rounded-2xl border border-slate-700/55 bg-slate-900/55 p-5 text-sm"
                v-html="report.fallbackHtml"
              />
            </div>
          </template>

          <div v-else class="home-empty-state flex-grow">
            <div class="home-empty-badge">Waiting for SQL</div>
            <h3 class="mt-5 text-2xl font-bold text-slate-100">报告区已经准备好</h3>
            <p class="mt-3 max-w-md text-center text-slate-400 leading-7">
              在左侧填写 SQL、模型和数据库类型后点击开始优化，这里会生成结构化的分析报告与可复制的改写 SQL。
            </p>
          </div>
        </div>
      </section>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api } from '@/lib/api';
import { buildSqlAnalysis, copyPlainText } from '@/lib/sql-report';
import { consumePendingTemplate } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const modelOptions = [
  { value: 'gpt-5.4', label: 'GPT-5.4' },
  { value: 'qwen-plus', label: 'Qwen Plus' },
  { value: 'qwen-max', label: 'Qwen Max' },
  { value: 'qwen3-vl-plus', label: 'Qwen3 VL Plus' },
  { value: 'qwen3-vl-flash', label: 'Qwen3 VL Flash' }
];

const dbOptions = [
  { value: 'mysql', label: 'MySQL (OLTP)' },
  { value: 'clickhouse', label: 'ClickHouse (OLAP)' },
  { value: 'pg', label: 'PostgreSQL' },
  { value: 'starrocks', label: 'StarRocks (OLAP)' }
];

const heroMetrics = [
  { label: 'Engine', value: '规则引擎 + AI 分析' },
  { label: 'Output', value: '评分 / 问题 / 建议 / SQL' },
  { label: 'Workflow', value: '输入即生成结构化报告' }
];

const heroInsights = [
  { title: '适合查慢 SQL', text: '快速识别全表扫描、索引缺失、查询条件不合理等常见问题。' },
  { title: '建议可直接执行', text: '输出会包含可复制 SQL，方便你直接验证与回归测试。' },
  { title: '记录自动沉淀', text: '分析完成后可继续在历史记录里回看、比对和复用。' }
];

const form = reactive({
  model: 'gpt-5.4',
  dbType: 'mysql',
  sql: ''
});
const loading = ref(false);
const requestError = ref('');
const report = ref(null);
const copiedKey = ref('');

function applyPendingSqlTemplate() {
  const pending = consumePendingTemplate('sql');
  if (!pending?.fields) {
    return;
  }

  const fields = pending.fields;
  if (typeof fields.model === 'string' && fields.model) {
    form.model = fields.model;
  }
  if (typeof fields.dbType === 'string' && fields.dbType) {
    form.dbType = fields.dbType;
  }
  if (typeof fields.sql === 'string') {
    form.sql = fields.sql;
  }
  requestError.value = '';
  report.value = null;
}

async function optimizeSql() {
  const sql = form.sql.trim();
  if (!sql) {
    requestError.value = '请输入需要优化的 SQL 语句。';
    return;
  }

  loading.value = true;
  requestError.value = '';
  report.value = null;

  try {
    const { response, data } = await api.optimizeSql({
      sql,
      dbType: form.dbType,
      model: form.model
    });

    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }

    report.value = {
      ...buildSqlAnalysis(data.aiAnalysis || ''),
      ruleWarnings: Array.isArray(data.ruleWarnings) ? data.ruleWarnings : []
    };
    await auth.fetchCurrentUser();
  } catch (error) {
    requestError.value = '请求失败，请检查服务是否正常运行，或确认 API Key 配置是否正确。';
  } finally {
    loading.value = false;
  }
}

async function copySql() {
  if (!report.value?.sqlRaw) {
    return;
  }

  try {
    await copyPlainText(report.value.sqlRaw);
    copiedKey.value = 'sql';
    window.setTimeout(() => {
      if (copiedKey.value === 'sql') {
        copiedKey.value = '';
      }
    }, 2000);
  } catch (error) {
    requestError.value = '复制失败，请手动复制。';
  }
}

onMounted(() => {
  applyPendingSqlTemplate();
  auth.fetchCurrentUser();
});
</script>
