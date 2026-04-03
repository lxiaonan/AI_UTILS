<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.1fr_0.9fr] items-start">
          <div>
            <div class="studio-kicker">Template Center</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">模板中心</h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              统一管理 SQL、图像、视频、语音、商品文案和广告语模板。营销模板已支持直接带入对应页面。
            </p>
          </div>
          <div class="grid gap-3 sm:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">模板总数</div>
              <div class="studio-metric-value">{{ templates.length }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">内置模板</div>
              <div class="studio-metric-value">{{ builtInCount }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">营销模板</div>
              <div class="studio-metric-value">{{ marketingCount }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        <section class="xl:col-span-7 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap mb-6">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Template Library</div>
              <h2 class="mt-2 text-2xl font-bold text-slate-100">模板列表</h2>
            </div>
            <div class="flex gap-3 flex-wrap">
              <input v-model.trim="keyword" type="text" class="min-w-[240px] bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border" placeholder="搜索模板">
              <select v-model="activeModule" class="bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                <option value="all">全部模块</option>
                <option v-for="item in TEMPLATE_MODULES" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </div>
          </div>

          <div class="space-y-5">
            <article v-for="item in filteredTemplates" :key="item.id" class="rounded-[1.6rem] border border-slate-700/70 bg-slate-950/35 p-5">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="text-xs px-2.5 py-1 rounded-full border" :class="moduleBadgeClass(item.module)">{{ item.moduleLabel }}</span>
                    <span class="text-xs px-2.5 py-1 rounded-full border" :class="item.builtIn ? 'text-sky-300 border-sky-500/30 bg-sky-900/20' : 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20'">
                      {{ item.builtIn ? '内置模板' : '自定义模板' }}
                    </span>
                  </div>
                  <div class="mt-3 text-xl font-bold text-slate-100">{{ item.title }}</div>
                  <div class="mt-2 text-sm text-slate-400 leading-7">{{ item.description }}</div>
                </div>
                <div class="text-sm text-slate-400">{{ item.route }}</div>
              </div>

              <pre class="mt-4 rounded-[1.35rem] border border-slate-700/70 bg-slate-950/55 p-4 text-xs text-slate-300 whitespace-pre-wrap overflow-auto">{{ item.previewText }}</pre>

              <div class="mt-4 flex gap-3 flex-wrap">
                <button type="button" class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition" @click="applyTemplate(item.raw)">套用模板</button>
                <button type="button" class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="editTemplate(item.raw)">{{ item.builtIn ? '复制并编辑' : '编辑模板' }}</button>
                <button v-if="!item.builtIn" type="button" class="px-4 py-3 rounded-xl bg-rose-900/20 text-rose-200 border border-rose-500/30 hover:bg-rose-900/35 transition" @click="removeTemplate(item.raw)">删除</button>
              </div>
            </article>

            <div v-if="!filteredTemplates.length" class="studio-empty text-center">
              <div class="text-xl font-bold text-slate-100">当前没有匹配模板</div>
              <div class="mt-2 text-sm text-slate-400">试试切换模块，或在右侧创建一个新模板。</div>
            </div>
          </div>
        </section>

        <aside class="xl:col-span-5 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="flex items-start justify-between gap-4 flex-wrap">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Template Builder</div>
                <h2 class="mt-2 text-2xl font-bold text-slate-100">{{ draft.id ? '编辑模板' : '新建模板' }}</h2>
              </div>
              <button type="button" class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="resetDraft()">重置</button>
            </div>

            <div class="mt-6 space-y-5">
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">模板标题</label>
                <input v-model.trim="draft.title" type="text" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
              </div>
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">所属模块</label>
                <select v-model="draft.module" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border" @change="handleModuleChange">
                  <option v-for="item in TEMPLATE_MODULES" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">模板说明</label>
                <textarea v-model.trim="draft.description" rows="3" class="w-full bg-slate-950/55 border border-slate-700 rounded-[1.4rem] p-4 text-slate-100 leading-7 resize-none outline-none neon-border"></textarea>
              </div>
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">标签</label>
                <input v-model.trim="tagsText" type="text" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border" placeholder="用逗号分隔">
              </div>
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">字段 JSON</label>
                <textarea v-model="fieldsText" rows="14" class="w-full bg-slate-950/55 border border-slate-700 rounded-[1.4rem] p-4 text-slate-100 leading-7 resize-none outline-none neon-border font-mono text-sm"></textarea>
              </div>

              <div v-if="errorMessage" class="rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">{{ errorMessage }}</div>

              <div class="flex gap-3 flex-wrap">
                <button type="button" class="flex-1 min-w-[180px] px-4 py-4 rounded-2xl bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 text-white font-bold transition-all btn-glow" @click="saveTemplate">
                  {{ draft.id ? '更新模板' : '保存模板' }}
                </button>
                <button type="button" class="px-4 py-4 rounded-2xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="applyDraftTemplate">
                  套用当前草稿
                </button>
              </div>
            </div>
          </section>
        </aside>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import {
  TEMPLATE_MODULES,
  createTemplateDraft,
  deleteCustomTemplate,
  duplicateTemplate,
  getModuleRoute,
  listAllTemplates,
  queueTemplateApplication,
  upsertCustomTemplate
} from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const keyword = ref('');
const activeModule = ref('all');
const templates = ref([]);
const draft = ref(createTemplateDraft('marketing'));
const tagsText = ref('');
const fieldsText = ref(JSON.stringify(draft.value.fields, null, 2));
const errorMessage = ref('');

const filteredTemplates = computed(() => {
  const term = keyword.value.trim().toLowerCase();
  return templates.value.filter((item) => {
    if (activeModule.value !== 'all' && item.module !== activeModule.value) return false;
    if (!term) return true;
    return item.searchText.includes(term);
  });
});

const builtInCount = computed(() => templates.value.filter((item) => item.builtIn).length);
const marketingCount = computed(() => templates.value.filter((item) => ['marketing', 'slogan'].includes(item.module)).length);

function moduleBadgeClass(module) {
  const map = {
    sql: 'text-cyan-300 border-cyan-500/30 bg-cyan-900/20',
    image: 'text-fuchsia-300 border-fuchsia-500/30 bg-fuchsia-900/20',
    marketing: 'text-violet-300 border-violet-500/30 bg-violet-900/20',
    slogan: 'text-pink-300 border-pink-500/30 bg-pink-900/20',
    video: 'text-sky-300 border-sky-500/30 bg-sky-900/20',
    voice: 'text-amber-300 border-amber-500/30 bg-amber-900/20',
    camera: 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20'
  };
  return map[module] || 'text-slate-300 border-slate-500/30 bg-slate-900/20';
}

function normalizeTemplates(list) {
  return list.map((item) => ({
    ...item,
    moduleLabel: TEMPLATE_MODULES.find((module) => module.value === item.module)?.label || item.module,
    route: getModuleRoute(item.module),
    previewText: JSON.stringify(item.fields || {}, null, 2),
    searchText: [item.title, item.description, item.module, ...(item.tags || []), JSON.stringify(item.fields || {})].join(' ').toLowerCase(),
    raw: item
  }));
}

function refreshTemplates() {
  templates.value = normalizeTemplates(listAllTemplates());
}

function resetDraft(module = 'marketing') {
  draft.value = createTemplateDraft(module);
  tagsText.value = '';
  fieldsText.value = JSON.stringify(draft.value.fields, null, 2);
  errorMessage.value = '';
}

function handleModuleChange() {
  draft.value.fields = createTemplateDraft(draft.value.module).fields;
  fieldsText.value = JSON.stringify(draft.value.fields, null, 2);
}

function editTemplate(template) {
  const source = template.builtIn ? duplicateTemplate(template) : JSON.parse(JSON.stringify(template));
  draft.value = source;
  tagsText.value = (source.tags || []).join(', ');
  fieldsText.value = JSON.stringify(source.fields || {}, null, 2);
  errorMessage.value = '';
}

function parseDraft() {
  const fields = JSON.parse(fieldsText.value || '{}');
  const nextDraft = {
    ...draft.value,
    title: draft.value.title.trim(),
    description: draft.value.description.trim(),
    tags: tagsText.value.split(',').map((item) => item.trim()).filter(Boolean),
    fields
  };
  return nextDraft;
}

function validateDraft(template) {
  if (!template.title) return '请输入模板标题。';
  if (!template.module) return '请选择所属模块。';
  return '';
}

function saveTemplate() {
  try {
    const template = parseDraft();
    const message = validateDraft(template);
    if (message) {
      errorMessage.value = message;
      return;
    }
    const saved = upsertCustomTemplate(template);
    editTemplate(saved);
    refreshTemplates();
  } catch (error) {
    errorMessage.value = `字段 JSON 解析失败：${error.message}`;
  }
}

function removeTemplate(template) {
  deleteCustomTemplate(template.id);
  refreshTemplates();
  if (draft.value.id === template.id) {
    resetDraft(template.module);
  }
}

function applyTemplate(template) {
  queueTemplateApplication(template);
  router.push(getModuleRoute(template.module));
}

function applyDraftTemplate() {
  try {
    const template = parseDraft();
    const message = validateDraft(template);
    if (message) {
      errorMessage.value = message;
      return;
    }
    queueTemplateApplication(template);
    router.push(getModuleRoute(template.module));
  } catch (error) {
    errorMessage.value = `字段 JSON 解析失败：${error.message}`;
  }
}

onMounted(async () => {
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    refreshTemplates();
  }
});
</script>
