<template>
  <AppShellLayout>
    <div class="max-w-7xl mx-auto px-4 pb-10 w-full">
      <div class="mb-8 flex items-end justify-between gap-4 flex-wrap">
        <div>
          <div class="text-xs uppercase tracking-[0.22em] text-slate-500 mb-3">Optimization Archive</div>
          <h2 class="text-3xl md:text-4xl font-extrabold text-slate-100">历史优化记录</h2>
          <p class="mt-3 text-slate-400">查看每一次 SQL 优化的时间、数据库类型、模型与得分。</p>
        </div>
      </div>

      <div class="glass-panel rounded-3xl overflow-hidden">
        <div class="overflow-x-auto bg-slate-900/60">
          <table class="min-w-full divide-y divide-slate-700/50">
            <thead class="bg-slate-800/80">
              <tr>
                <th class="px-6 py-5 text-left text-xs font-bold text-slate-400 uppercase tracking-wider">时间</th>
                <th class="px-6 py-5 text-left text-xs font-bold text-slate-400 uppercase tracking-wider">数据库</th>
                <th class="px-6 py-5 text-left text-xs font-bold text-slate-400 uppercase tracking-wider">模型</th>
                <th class="px-6 py-5 text-left text-xs font-bold text-slate-400 uppercase tracking-wider">评分</th>
                <th class="px-6 py-5 text-left text-xs font-bold text-slate-400 uppercase tracking-wider">用户 SQL</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/50">
              <tr v-if="loading">
                <td colspan="5" class="px-6 py-12 text-center text-slate-500">正在加载数据...</td>
              </tr>
              <tr v-else-if="errorMessage">
                <td colspan="5" class="px-6 py-8 text-center text-rose-400">{{ errorMessage }}</td>
              </tr>
              <tr v-else-if="!history.length">
                <td colspan="5" class="px-6 py-12 text-center text-slate-500">暂无优化记录。</td>
              </tr>
              <tr
                v-for="item in history"
                :key="item.id"
                class="hover:bg-slate-800/80 transition duration-300 cursor-pointer"
                @click="goDetail(item.id)"
              >
                <td class="px-6 py-5 whitespace-nowrap text-sm text-slate-400">{{ formatDate(item.createTime) }}</td>
                <td class="px-6 py-5 whitespace-nowrap text-sm font-medium">
                  <span class="bg-sky-900/40 text-sky-300 px-3 py-1.5 rounded-lg border border-sky-500/30">{{ item.dbType || '--' }}</span>
                </td>
                <td class="px-6 py-5 whitespace-nowrap text-sm text-slate-400">{{ item.aiModel || '--' }}</td>
                <td class="px-6 py-5 whitespace-nowrap text-lg font-bold" :class="scoreColor(item.score)">{{ item.score || '--' }}</td>
                <td class="px-6 py-5 text-sm text-slate-300 font-mono truncate max-w-md" :title="item.userSql || ''">
                  {{ sqlSnippet(item.userSql) }}
                </td>
              </tr>
            </tbody>
          </table>
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
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const loading = ref(true);
const errorMessage = ref('');
const history = ref([]);

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : '--';
}

function sqlSnippet(value) {
  if (!value) {
    return '--';
  }
  return value.substring(0, 60) + (value.length > 60 ? '...' : '');
}

function scoreColor(score) {
  if (score >= 80) {
    return 'text-emerald-400';
  }
  if (score >= 60) {
    return 'text-yellow-400';
  }
  return 'text-rose-400';
}

async function loadHistory() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const { response, data } = await api.getSqlHistory();
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    history.value = Array.isArray(data) ? data : [];
  } catch (error) {
    errorMessage.value = `获取历史记录失败：${error.message}`;
  } finally {
    loading.value = false;
  }
}

function goDetail(id) {
  router.push({ path: '/sql-detail', query: { id } });
}

onMounted(async () => {
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadHistory();
  }
});
</script>
