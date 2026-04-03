<template>
  <div class="app-shell min-h-screen flex flex-col text-slate-100">
    <header class="app-workspace-nav">
      <div class="app-workspace-wrap">
        <div class="app-workspace-grid">
          <RouterLink to="/" class="app-brand-card" title="AI Platform Workspace">
            <div class="app-brand-mark">
              <img :src="logoUrl" class="w-11 h-11 rounded-2xl object-cover" alt="AI">
            </div>
            <div class="min-w-0 flex-1">
              <div class="app-brand-eyebrow">AI WORKSPACE</div>
              <div class="app-brand-title">多模态工作台</div>
              <div class="app-brand-subtitle">统一串联 SQL、图像、视频、语音与营销生成，让分析和创作在同一条流程里完成。</div>
            </div>
          </RouterLink>

          <div class="app-nav-card">
            <div class="app-nav-top desktop-only">
              <div class="app-nav-tabs">
                <button
                  v-for="group in navGroups"
                  :key="group.key"
                  type="button"
                  class="app-nav-tab"
                  :class="{ 'is-active': openGroupKey === group.key }"
                  @click="toggleGroup(group.key)"
                >
                  <span class="app-nav-tab-title">{{ group.label }}</span>
                  <span class="app-nav-tab-subtitle">{{ group.hint }}</span>
                </button>
              </div>

              <div class="app-nav-summary">
                <div class="app-nav-summary-label">Current</div>
                <div class="app-nav-summary-title">{{ activeNavItem.label }}</div>
              </div>
            </div>

            <div class="app-nav-fold desktop-only">
              <Transition name="nav-fold" mode="out-in">
                <div v-if="useCompactGroupNav" :key="`${openGroup.key}-compact`" class="app-nav-fold-inner app-nav-fold-inner--compact">
                  <div class="app-nav-fold-head app-nav-fold-head--compact">
                    <div>
                      <div class="app-nav-fold-label">{{ openGroup.label }}</div>
                      <div class="app-nav-fold-desc">{{ compactGroupDesc }}</div>
                    </div>
                    <div class="app-nav-fold-hint">{{ openGroup.hint }}</div>
                  </div>

                  <div class="app-nav-compact-row">
                    <RouterLink
                      v-for="item in openGroup.items"
                      :key="item.to"
                      :to="item.to"
                      class="app-nav-compact-link"
                      :class="{ 'is-active': isActive(item.to) }"
                    >
                      <span class="app-nav-compact-kicker">{{ item.hint }}</span>
                      <span class="app-nav-compact-title">{{ item.label }}</span>
                    </RouterLink>
                  </div>
                </div>

                <div v-else :key="openGroup.key" class="app-nav-fold-inner">
                  <div class="app-nav-fold-head">
                    <div>
                      <div class="app-nav-fold-label">{{ openGroup.label }}</div>
                      <div class="app-nav-fold-desc">{{ openGroupDesc }}</div>
                    </div>
                    <div class="app-nav-fold-hint">{{ openGroup.hint }}</div>
                  </div>

                  <div class="app-nav-links-row">
                    <RouterLink
                      v-for="item in openGroup.items"
                      :key="item.to"
                      :to="item.to"
                      class="app-nav-link-card"
                      :class="{ 'is-active': isActive(item.to) }"
                    >
                      <span class="app-nav-link-kicker">{{ item.hint }}</span>
                      <span class="app-nav-link-title">{{ item.label }}</span>
                      <span class="app-nav-link-desc">{{ item.desc }}</span>
                    </RouterLink>
                  </div>
                </div>
              </Transition>
            </div>

            <div class="app-nav-scroll mobile-only">
              <RouterLink
                v-for="item in flatNavItems"
                :key="item.to"
                :to="item.to"
                class="app-nav-pill"
                :class="{ 'is-active': isActive(item.to) }"
              >
                <span class="app-nav-pill-title">{{ item.label }}</span>
                <span class="app-nav-pill-subtitle">{{ item.hint }}</span>
              </RouterLink>
            </div>
          </div>

          <div class="app-account-card">
            <template v-if="auth.state.loggedIn">
              <div class="app-account-topline">
                <span class="app-account-badge">Connected</span>
                <span class="app-account-route">{{ activeNavItem.hint }}</span>
              </div>

              <div class="app-account-meta">
                <div class="app-account-name">{{ auth.state.username }}</div>
                <div class="app-account-subtitle">当前工作台已同步，可继续查看历史记录、模板和生成结果。</div>
              </div>

              <div class="app-account-row">
                <div class="app-account-stat">
                  <span class="app-account-stat-label">AI 次数</span>
                  <span class="app-account-stat-value">{{ auth.state.aiUseCount }}</span>
                </div>
                <button class="app-account-action" @click="doLogout">
                  退出登录
                </button>
              </div>
            </template>

            <template v-else>
              <div class="app-account-topline">
                <span class="app-account-badge app-account-badge--muted">Guest</span>
                <span class="app-account-route">SYNC OFF</span>
              </div>

              <div class="app-account-meta">
                <div class="app-account-name">登录后继续</div>
                <div class="app-account-subtitle">解锁历史记录、素材沉淀和模板复用，让整个工作流连续起来。</div>
              </div>

              <div class="app-account-row">
                <RouterLink to="/login" class="app-account-action w-full text-center">登录</RouterLink>
                <RouterLink to="/register" class="app-account-action app-account-action--primary w-full text-center">注册</RouterLink>
              </div>
            </template>
          </div>
        </div>
      </div>
    </header>

    <main class="app-shell-main flex-1">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const logoUrl = '/logo/ai-logo.png';

const navGroups = [
  {
    key: 'analysis',
    label: '分析中心',
    hint: 'ANALYSIS',
    desc: '聚焦 SQL 诊断、优化记录与问题追踪。',
    items: [
      { to: '/', label: 'SQL 优化', hint: 'QUERY', desc: '面向数据库查询性能、规范与可维护性的智能分析。' },
      { to: '/history', label: '优化历史', hint: 'HISTORY', desc: '回看 SQL 优化记录，快速比对每次分析结果。' }
    ]
  },
  {
    key: 'creation',
    label: '创作中心',
    hint: 'CREATION',
    desc: '统一管理图像、视频、语音与 3D 控图能力。',
    items: [
      { to: '/image', label: '图像生成', hint: 'IMAGE', desc: '文本生图、参考图生成与多轮视觉创作。' },
      { to: '/video', label: '视频生成', hint: 'VIDEO', desc: '从脚本到成片，集中管理视频生产流程。' },
      { to: '/voice', label: '语音生成', hint: 'VOICE', desc: '支持多语言语音合成与输出结果留存。' },
      { to: '/camera', label: '3D 控图', hint: 'CAMERA', desc: '控制镜头角度、视角和画面构图细节。' }
    ]
  },
  {
    key: 'marketing',
    label: '营销中心',
    hint: 'MARKETING',
    desc: '围绕商品信息快速生成平台文案、广告语和可复用记录。',
    items: [
      { to: '/marketing', label: '商品文案', hint: 'COPY', desc: '输入商品信息，生成适合小红书、抖音和 B 站的营销内容。' },
      { to: '/slogan', label: '广告语', hint: 'SLOGAN', desc: '快速产出 slogan、广告语、电商主图文案和标题。' },
      { to: '/marketing-history', label: '营销历史', hint: 'HISTORY', desc: '统一查看商品文案和广告语记录，支持复制和删除。' }
    ]
  },
  {
    key: 'management',
    label: '管理中心',
    hint: 'MANAGEMENT',
    desc: '把任务、素材和模板沉淀成可复用资产。',
    items: [
      { to: '/tasks', label: '任务中心', hint: 'TASKS', desc: '统一查看任务进度、状态与最终结果。' },
      { to: '/assets', label: '素材中心', hint: 'ASSETS', desc: '集中沉淀与管理生成后的素材资产。' },
      { to: '/templates', label: '模板中心', hint: 'TEMPLATES', desc: '保存高频模板，让常用流程一键复用。' }
    ]
  }
];

const flatNavItems = navGroups.flatMap((group) => group.items);

function isActive(path) {
  if (path === '/') {
    return route.path === '/';
  }
  return route.path.startsWith(path);
}

function findActiveGroupKey() {
  return navGroups.find((group) => group.items.some((item) => isActive(item.to)))?.key ?? navGroups[0].key;
}

const openGroupKey = ref(findActiveGroupKey());
const activeNavItem = computed(() => flatNavItems.find((item) => isActive(item.to)) ?? flatNavItems[0]);
const openGroup = computed(() => navGroups.find((group) => group.key === openGroupKey.value) ?? navGroups[0]);
const openGroupDesc = computed(() => openGroup.value.desc ?? '');
const activeGroup = computed(() => navGroups.find((group) => group.items.some((item) => isActive(item.to))) ?? navGroups[0]);
const useCompactGroupNav = computed(() => activeGroup.value.key === 'marketing' && openGroup.value.key === activeGroup.value.key);
const compactGroupDesc = computed(() => {
  if (openGroup.value.key === 'marketing') {
    return '聚焦当前营销任务，直接在文案、广告语和历史之间切换，不再重复展示一整层营销概览。';
  }
  return openGroupDesc.value;
});

watch(
  () => route.path,
  () => {
    openGroupKey.value = findActiveGroupKey();
  }
);

function toggleGroup(key) {
  openGroupKey.value = key;
}

async function doLogout() {
  await auth.logout(router);
}
</script>
